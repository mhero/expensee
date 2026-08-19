package com.mac.expensee.sync

import com.mac.expensee.core.datastore.UserPreferencesDataSource
import com.mac.expensee.core.network.config.ApiConfig
import com.mac.expensee.core.network.dto.SyncPushRequestDto
import com.mac.expensee.feature.expenses.data.mapper.toDto
import com.mac.expensee.feature.expenses.data.mapper.toRemoteSnapshot
import com.mac.expensee.feature.expenses.data.remote.RemoteExpenseDataSource
import com.mac.expensee.feature.expenses.domain.repository.ExpenseSyncGateway
import kotlinx.coroutines.flow.first

/**
 * Orchestrates expense sync (`Local change -> local db -> SyncWorker -> remote API`, and the
 * reverse pull direction -- see project README's "Sync model"). Lives at the app level, not in
 * `feature:expenses`, because it also depends on `core:network`'s [ApiConfig] and
 * `core:datastore` -- composing a feature abstraction with cross-cutting infrastructure, the same
 * reason `app/notification`'s `ReminderCoordinator` isn't in `feature:settings`.
 *
 * A genuine no-op whenever [ApiConfig.isConfigured] is false, which today is always true -- there
 * is no backend. [sync] returns [SyncOutcome.Skipped] before making any network call or touching
 * the database, rather than pretending to sync against the placeholder host. Once a real API
 * ships (`API_BASE_URL` pointed at it), this same code path starts running for real with no
 * changes needed here.
 *
 * **Category sync is a deliberately deferred, mechanical repeat of this exact pattern.** Every
 * push already sends an (empty) `categories` list -- see [SyncPushRequestDto] -- and
 * `CategoryEntity` already carries the same sync metadata as `ExpenseEntity`. Wiring it up is
 * "add a `CategorySyncGateway` mirroring `ExpenseSyncGateway`", not new architecture; it's left
 * out to keep this phase reviewable (see project README).
 *
 * Conflict handling is intentionally simple last-write-wins by `updatedAt`, applied per-record in
 * [ExpenseSyncGateway.applyRemoteSnapshot] -- deliberately not an "elaborate distributed
 * synchronization system" (see original project brief).
 */
class SyncManager(
    private val apiConfig: ApiConfig,
    private val expenseSyncGateway: ExpenseSyncGateway,
    private val remoteExpenseDataSource: RemoteExpenseDataSource,
    private val userPreferencesDataSource: UserPreferencesDataSource,
) {
    sealed interface SyncOutcome {
        data object Skipped : SyncOutcome
        data object Success : SyncOutcome
        data class Failure(val cause: Throwable) : SyncOutcome
    }

    suspend fun sync(): SyncOutcome {
        if (!apiConfig.isConfigured) return SyncOutcome.Skipped

        return try {
            pushLocalChanges()
            pullRemoteChanges()
            expenseSyncGateway.purgeSyncedTombstones()
            SyncOutcome.Success
        } catch (error: Exception) {
            // A background sync job must never crash the app over a flaky network or a server
            // hiccup -- WorkManager (via SyncWorker) decides whether/when to retry; this class's
            // only job is to report that something went wrong.
            SyncOutcome.Failure(error)
        }
    }

    private suspend fun pushLocalChanges() {
        val uploads = expenseSyncGateway.pendingUploads()
        val deletions = expenseSyncGateway.pendingDeletions()
        if (uploads.isEmpty() && deletions.isEmpty()) return

        val request = SyncPushRequestDto(
            expenses = uploads.map { it.toDto() },
            categories = emptyList(), // see class KDoc: category sync isn't wired up yet.
            deletedExpenseIds = deletions.mapNotNull { it.remoteId },
        )
        val response = remoteExpenseDataSource.push(request)

        response.expenses.forEach { dto ->
            val upload = uploads.find { (it.remoteId ?: it.localId) == dto.id }
            if (upload != null) expenseSyncGateway.markUploaded(upload.localId, dto.id, dto.updatedAt)
        }
        deletions.forEach { expenseSyncGateway.markDeletionConfirmed(it.localId) }
    }

    private suspend fun pullRemoteChanges() {
        val since = userPreferencesDataSource.lastSyncTimestamp.first() ?: 0L
        val response = remoteExpenseDataSource.pull(since)
        response.expenses.forEach { dto -> expenseSyncGateway.applyRemoteSnapshot(dto.toRemoteSnapshot()) }
        userPreferencesDataSource.setLastSyncTimestamp(response.serverTimestamp)
    }
}
