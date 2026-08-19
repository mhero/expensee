package com.mac.expensee.sync

import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.common.money.Money
import com.mac.expensee.core.datastore.UserPreferencesDataSource
import com.mac.expensee.core.network.config.ApiConfig
import com.mac.expensee.core.network.dto.ExpenseDto
import com.mac.expensee.core.network.dto.SyncPullResponseDto
import com.mac.expensee.core.testing.MainDispatcherRule
import com.mac.expensee.feature.expenses.domain.model.PendingExpenseDeletion
import com.mac.expensee.feature.expenses.domain.model.PendingExpenseUpload
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SyncManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val remoteDataSource = FakeRemoteExpenseDataSource()
    private val syncGateway = FakeExpenseSyncGateway()
    private val preferences: UserPreferencesDataSource = mockk(relaxed = true)

    init {
        every { preferences.lastSyncTimestamp } returns flowOf(0L)
    }

    private fun buildManager(apiConfig: ApiConfig = ApiConfig(baseUrl = "https://real-backend.example/")) = SyncManager(
        apiConfig = apiConfig,
        expenseSyncGateway = syncGateway,
        remoteExpenseDataSource = remoteDataSource,
        userPreferencesDataSource = preferences,
    )

    @Test
    fun `sync is a genuine no-op when no backend is configured`() = runTest {
        val manager = buildManager(apiConfig = ApiConfig(baseUrl = ApiConfig.PLACEHOLDER_BASE_URL))

        val outcome = manager.sync()

        assertThat(outcome).isEqualTo(SyncManager.SyncOutcome.Skipped)
        assertThat(remoteDataSource.lastPushRequest).isNull()
        assertThat(remoteDataSource.lastPullSince).isNull()
        assertThat(syncGateway.tombstonesPurged).isFalse()
    }

    @Test
    fun `sync with nothing pending still pulls, purges tombstones, and succeeds`() = runTest {
        val manager = buildManager()

        val outcome = manager.sync()

        assertThat(outcome).isEqualTo(SyncManager.SyncOutcome.Success)
        assertThat(remoteDataSource.lastPushRequest).isNull() // nothing pending -- push is skipped entirely
        assertThat(remoteDataSource.lastPullSince).isEqualTo(0L)
        assertThat(syncGateway.tombstonesPurged).isTrue()
    }

    @Test
    fun `pending uploads are pushed and marked uploaded once the server echoes them back`() = runTest {
        syncGateway.uploads = listOf(
            PendingExpenseUpload(
                localId = "e1",
                remoteId = null,
                categoryId = "cat-1",
                amount = Money(500, CurrencyCode.USD),
                description = "Coffee",
                notes = null,
                date = 1_000L,
                updatedAt = 1_000L,
                version = 1,
            ),
        )
        remoteDataSource.pushResponse = SyncPullResponseDto(
            expenses = listOf(
                ExpenseDto(
                    id = "e1",
                    categoryId = "cat-1",
                    amountMinorUnits = 500,
                    currencyCode = "USD",
                    description = "Coffee",
                    date = 1_000L,
                    updatedAt = 2_000L,
                    version = 1,
                ),
            ),
            categories = emptyList(),
            serverTimestamp = 2_000L,
        )
        val manager = buildManager()

        manager.sync()

        assertThat(remoteDataSource.lastPushRequest?.expenses).hasSize(1)
        assertThat(syncGateway.uploadedMarks).containsExactly(Triple("e1", "e1", 2_000L))
    }

    @Test
    fun `pending deletions are pushed and marked confirmed`() = runTest {
        syncGateway.deletions = listOf(PendingExpenseDeletion(localId = "e2", remoteId = "server-2"))
        val manager = buildManager()

        manager.sync()

        assertThat(remoteDataSource.lastPushRequest?.deletedExpenseIds).containsExactly("server-2")
        assertThat(syncGateway.confirmedDeletions).containsExactly("e2")
    }

    @Test
    fun `pulled remote expenses are applied and the last-sync timestamp is advanced`() = runTest {
        remoteDataSource.pullResponse = SyncPullResponseDto(
            expenses = listOf(
                ExpenseDto(
                    id = "server-9",
                    categoryId = "cat-1",
                    amountMinorUnits = 1_200,
                    currencyCode = "USD",
                    description = "From another device",
                    date = 1_000L,
                    updatedAt = 3_000L,
                    version = 1,
                ),
            ),
            categories = emptyList(),
            serverTimestamp = 3_000L,
        )
        val manager = buildManager()

        manager.sync()

        assertThat(syncGateway.appliedSnapshots).hasSize(1)
        assertThat(syncGateway.appliedSnapshots.first().remoteId).isEqualTo("server-9")
        coVerify { preferences.setLastSyncTimestamp(3_000L) }
    }

    @Test
    fun `a thrown exception during sync is reported as a failure, not propagated`() = runTest {
        remoteDataSource.throwOnPush = IllegalStateException("network blip")
        syncGateway.uploads = listOf(
            PendingExpenseUpload("e1", null, "cat-1", Money(100, CurrencyCode.USD), "x", null, 1_000L, 1_000L, 1),
        )
        val manager = buildManager()

        val outcome = manager.sync()

        assertThat(outcome).isInstanceOf(SyncManager.SyncOutcome.Failure::class.java)
    }
}
