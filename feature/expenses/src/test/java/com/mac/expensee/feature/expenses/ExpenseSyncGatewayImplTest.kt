package com.mac.expensee.feature.expenses

import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.common.sync.SyncStatus
import com.mac.expensee.core.database.entity.ExpenseEntity
import com.mac.expensee.feature.expenses.data.ExpenseSyncGatewayImpl
import com.mac.expensee.feature.expenses.domain.model.RemoteExpenseSnapshot
import kotlinx.coroutines.test.runTest
import org.junit.Test

private fun expense(
    localId: String,
    remoteId: String? = null,
    syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD,
    deletedAt: Long? = null,
    updatedAt: Long = 1_000L,
) = ExpenseEntity(
    localId = localId,
    remoteId = remoteId,
    categoryLocalId = "cat-1",
    amountMinorUnits = 500,
    currencyCode = "USD",
    description = "Coffee",
    notes = null,
    date = 1_000L,
    receiptUri = null,
    createdAt = 1_000L,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    deletedAt = deletedAt,
    version = 1,
)

class ExpenseSyncGatewayImplTest {

    private val dao = FakeExpenseDao()
    private val gateway = ExpenseSyncGatewayImpl(dao)

    @Test
    fun `pending uploads excludes tombstoned and already-synced rows`() = runTest {
        dao.seed(expense("e1", syncStatus = SyncStatus.PENDING_UPLOAD))
        dao.seed(expense("e2", syncStatus = SyncStatus.SYNCED))
        dao.seed(expense("e3", syncStatus = SyncStatus.PENDING_DELETE, deletedAt = 2_000L))

        val uploads = gateway.pendingUploads()

        assertThat(uploads).hasSize(1)
        assertThat(uploads.first().localId).isEqualTo("e1")
    }

    @Test
    fun `pending deletions only includes tombstoned rows still marked pending delete`() = runTest {
        dao.seed(expense("e1", syncStatus = SyncStatus.PENDING_DELETE, deletedAt = 2_000L))
        dao.seed(expense("e2", syncStatus = SyncStatus.PENDING_UPLOAD))

        val deletions = gateway.pendingDeletions()

        assertThat(deletions).hasSize(1)
        assertThat(deletions.first().localId).isEqualTo("e1")
    }

    @Test
    fun `marking uploaded stamps remoteId and flips status to synced`() = runTest {
        dao.seed(expense("e1", syncStatus = SyncStatus.PENDING_UPLOAD))

        gateway.markUploaded(localId = "e1", remoteId = "server-1", syncedAt = 5_000L)

        val updated = dao.all().first { it.localId == "e1" }
        assertThat(updated.remoteId).isEqualTo("server-1")
        assertThat(updated.syncStatus).isEqualTo(SyncStatus.SYNCED)
    }

    @Test
    fun `applying a remote snapshot newer than the local row overwrites it`() = runTest {
        dao.seed(expense("e1", remoteId = "server-1", updatedAt = 1_000L))

        gateway.applyRemoteSnapshot(
            RemoteExpenseSnapshot(
                remoteId = "server-1",
                categoryId = "cat-1",
                amount = com.mac.expensee.core.common.money.Money(999, CurrencyCode.USD),
                description = "Updated on another device",
                notes = null,
                date = 1_000L,
                updatedAt = 5_000L,
                version = 2,
            ),
        )

        val updated = dao.all().first { it.remoteId == "server-1" }
        assertThat(updated.description).isEqualTo("Updated on another device")
        assertThat(updated.syncStatus).isEqualTo(SyncStatus.SYNCED)
    }

    @Test
    fun `applying a remote snapshot older than the local row is dropped -- last write wins`() = runTest {
        dao.seed(expense("e1", remoteId = "server-1", updatedAt = 9_000L, syncStatus = SyncStatus.PENDING_UPLOAD))

        gateway.applyRemoteSnapshot(
            RemoteExpenseSnapshot(
                remoteId = "server-1",
                categoryId = "cat-1",
                amount = com.mac.expensee.core.common.money.Money(1, CurrencyCode.USD),
                description = "Stale remote value",
                notes = null,
                date = 1_000L,
                updatedAt = 1_000L,
                version = 1,
            ),
        )

        val local = dao.all().first { it.localId == "e1" }
        assertThat(local.description).isEqualTo("Coffee")
        assertThat(local.syncStatus).isEqualTo(SyncStatus.PENDING_UPLOAD)
    }

    @Test
    fun `applying a remote snapshot with no matching local row creates a new one`() = runTest {
        gateway.applyRemoteSnapshot(
            RemoteExpenseSnapshot(
                remoteId = "server-9",
                categoryId = "cat-1",
                amount = com.mac.expensee.core.common.money.Money(1_200, CurrencyCode.USD),
                description = "Created elsewhere",
                notes = null,
                date = 1_000L,
                updatedAt = 5_000L,
                version = 1,
            ),
        )

        assertThat(dao.all()).hasSize(1)
        assertThat(dao.all().first().remoteId).isEqualTo("server-9")
        assertThat(dao.all().first().syncStatus).isEqualTo(SyncStatus.SYNCED)
    }

    @Test
    fun `purging tombstones removes only synced, deleted rows`() = runTest {
        dao.seed(expense("e1", syncStatus = SyncStatus.SYNCED, deletedAt = 2_000L))
        dao.seed(expense("e2", syncStatus = SyncStatus.PENDING_DELETE, deletedAt = 2_000L))
        dao.seed(expense("e3", syncStatus = SyncStatus.PENDING_UPLOAD))

        gateway.purgeSyncedTombstones()

        assertThat(dao.all().map { it.localId }).containsExactly("e2", "e3")
    }
}
