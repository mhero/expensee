package com.mac.expensee.sync

import com.mac.expensee.core.network.dto.SyncPullResponseDto
import com.mac.expensee.core.network.dto.SyncPushRequestDto
import com.mac.expensee.feature.expenses.data.remote.RemoteExpenseDataSource
import com.mac.expensee.feature.expenses.domain.model.PendingExpenseDeletion
import com.mac.expensee.feature.expenses.domain.model.PendingExpenseUpload
import com.mac.expensee.feature.expenses.domain.model.RemoteExpenseSnapshot
import com.mac.expensee.feature.expenses.domain.repository.ExpenseSyncGateway

class FakeRemoteExpenseDataSource : RemoteExpenseDataSource {
    var pushResponse: SyncPullResponseDto = SyncPullResponseDto(emptyList(), emptyList(), 0L)
    var pullResponse: SyncPullResponseDto = SyncPullResponseDto(emptyList(), emptyList(), 0L)
    var lastPushRequest: SyncPushRequestDto? = null
    var lastPullSince: Long? = null
    var throwOnPush: Throwable? = null

    override suspend fun push(request: SyncPushRequestDto): SyncPullResponseDto {
        throwOnPush?.let { throw it }
        lastPushRequest = request
        return pushResponse
    }

    override suspend fun pull(sinceEpochMillis: Long): SyncPullResponseDto {
        lastPullSince = sinceEpochMillis
        return pullResponse
    }
}

class FakeExpenseSyncGateway : ExpenseSyncGateway {
    var uploads: List<PendingExpenseUpload> = emptyList()
    var deletions: List<PendingExpenseDeletion> = emptyList()
    val uploadedMarks = mutableListOf<Triple<String, String, Long>>()
    val confirmedDeletions = mutableListOf<String>()
    val appliedSnapshots = mutableListOf<RemoteExpenseSnapshot>()
    var tombstonesPurged = false

    override suspend fun pendingUploads() = uploads
    override suspend fun pendingDeletions() = deletions

    override suspend fun markUploaded(localId: String, remoteId: String, syncedAt: Long) {
        uploadedMarks += Triple(localId, remoteId, syncedAt)
    }

    override suspend fun markDeletionConfirmed(localId: String) {
        confirmedDeletions += localId
    }

    override suspend fun applyRemoteSnapshot(snapshot: RemoteExpenseSnapshot) {
        appliedSnapshots += snapshot
    }

    override suspend fun purgeSyncedTombstones() {
        tombstonesPurged = true
    }
}
