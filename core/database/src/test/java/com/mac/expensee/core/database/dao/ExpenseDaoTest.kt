package com.mac.expensee.core.database.dao

import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.common.sync.SyncStatus
import com.mac.expensee.core.database.entity.CategoryEntity
import com.mac.expensee.core.database.entity.ExpenseEntity
import com.mac.expensee.core.testing.testFirst
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ExpenseDaoTest : RoomDaoTest() {

    private val expenseDao get() = db.expenseDao()
    private val categoryDao get() = db.categoryDao()

    /** Expenses have a RESTRICT foreign key to categories, so every test needs one to reference. */
    private suspend fun insertCategory(localId: String = "cat-1"): String {
        categoryDao.upsert(
            CategoryEntity(
                localId = localId,
                remoteId = null,
                name = "Food",
                colorHex = "#EF6C00",
                icon = "default",
                isDefault = false,
                createdAt = 0L,
                updatedAt = 0L,
                syncStatus = SyncStatus.SYNCED,
                deletedAt = null,
                version = 1,
            ),
        )
        return localId
    }

    private fun expense(
        localId: String,
        categoryLocalId: String,
        date: Long = 1_000L,
        createdAt: Long = date,
        remoteId: String? = null,
        syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD,
        deletedAt: Long? = null,
        version: Int = 1,
    ) = ExpenseEntity(
        localId = localId,
        remoteId = remoteId,
        categoryLocalId = categoryLocalId,
        amountMinorUnits = 500,
        currencyCode = "USD",
        description = "Coffee",
        notes = null,
        date = date,
        receiptUri = null,
        createdAt = createdAt,
        updatedAt = createdAt,
        syncStatus = syncStatus,
        deletedAt = deletedAt,
        version = version,
    )

    @Test
    fun `upsert then getById returns the inserted row`() = runTest {
        val categoryId = insertCategory()
        expenseDao.upsert(expense("e1", categoryId))

        val result = expenseDao.getById("e1")

        assertThat(result).isNotNull()
        assertThat(result?.description).isEqualTo("Coffee")
    }

    @Test
    fun `getById returns null for an unknown id`() = runTest {
        assertThat(expenseDao.getById("missing")).isNull()
    }

    @Test
    fun `observeAll orders by date then createdAt descending and excludes soft-deleted rows`() = runTest {
        val categoryId = insertCategory()
        expenseDao.upsert(expense("older", categoryId, date = 1_000L))
        expenseDao.upsert(expense("newer", categoryId, date = 3_000L))
        expenseDao.upsert(expense("deleted", categoryId, date = 5_000L, deletedAt = 5_000L))

        expenseDao.observeAll().testFirst { list ->
            assertThat(list.map { it.localId }).containsExactly("newer", "older").inOrder()
        }
    }

    @Test
    fun `observeByCategory only returns expenses for that category`() = runTest {
        val categoryA = insertCategory("cat-a")
        val categoryB = insertCategory("cat-b")
        expenseDao.upsert(expense("in-a", categoryA))
        expenseDao.upsert(expense("in-b", categoryB))

        expenseDao.observeByCategory(categoryA).testFirst { list ->
            assertThat(list.map { it.localId }).containsExactly("in-a")
        }
    }

    @Test
    fun `observeInRange is inclusive of both bounds`() = runTest {
        val categoryId = insertCategory()
        expenseDao.upsert(expense("before", categoryId, date = 999L))
        expenseDao.upsert(expense("start", categoryId, date = 1_000L))
        expenseDao.upsert(expense("end", categoryId, date = 2_000L))
        expenseDao.upsert(expense("after", categoryId, date = 2_001L))

        expenseDao.observeInRange(fromInclusive = 1_000L, toInclusive = 2_000L).testFirst { list ->
            assertThat(list.map { it.localId }).containsExactly("start", "end")
        }
    }

    @Test
    fun `getByRemoteId finds the matching local row`() = runTest {
        val categoryId = insertCategory()
        expenseDao.upsert(expense("local-1", categoryId, remoteId = "server-9"))

        val result = expenseDao.getByRemoteId("server-9")

        assertThat(result?.localId).isEqualTo("local-1")
    }

    @Test
    fun `softDelete tombstones the row instead of removing it`() = runTest {
        val categoryId = insertCategory()
        expenseDao.upsert(expense("e1", categoryId, version = 1))

        expenseDao.softDelete("e1", deletedAt = 9_000L)

        val stored = expenseDao.getById("e1")
        assertThat(stored?.deletedAt).isEqualTo(9_000L)
        assertThat(stored?.syncStatus).isEqualTo(SyncStatus.PENDING_DELETE)
        assertThat(stored?.version).isEqualTo(2)
        expenseDao.observeAll().testFirst { list -> assertThat(list).isEmpty() }
    }

    @Test
    fun `purgeSyncedTombstones only removes rows that are both deleted and synced`() = runTest {
        val categoryId = insertCategory()
        expenseDao.upsert(expense("synced-tombstone", categoryId, deletedAt = 1L, syncStatus = SyncStatus.SYNCED))
        expenseDao.upsert(expense("pending-tombstone", categoryId, deletedAt = 1L, syncStatus = SyncStatus.PENDING_DELETE))
        expenseDao.upsert(expense("live-row", categoryId, syncStatus = SyncStatus.SYNCED))

        expenseDao.purgeSyncedTombstones()

        assertThat(expenseDao.getById("synced-tombstone")).isNull()
        assertThat(expenseDao.getById("pending-tombstone")).isNotNull()
        assertThat(expenseDao.getById("live-row")).isNotNull()
    }

    @Test
    fun `getPendingSync returns every row not yet synced`() = runTest {
        val categoryId = insertCategory()
        expenseDao.upsert(expense("pending", categoryId, syncStatus = SyncStatus.PENDING_UPLOAD))
        expenseDao.upsert(expense("failed", categoryId, syncStatus = SyncStatus.SYNC_FAILED))
        expenseDao.upsert(expense("synced", categoryId, syncStatus = SyncStatus.SYNCED))

        val pending = expenseDao.getPendingSync()

        assertThat(pending.map { it.localId }).containsExactly("pending", "failed")
    }

    @Test
    fun `countByCategory ignores soft-deleted expenses`() = runTest {
        val categoryId = insertCategory()
        expenseDao.upsert(expense("live-1", categoryId))
        expenseDao.upsert(expense("live-2", categoryId))
        expenseDao.upsert(expense("gone", categoryId, deletedAt = 1L))

        assertThat(expenseDao.countByCategory(categoryId)).isEqualTo(2)
    }

    @Test
    fun `update overwrites fields on an existing row`() = runTest {
        val categoryId = insertCategory()
        val original = expense("e1", categoryId)
        expenseDao.upsert(original)

        expenseDao.update(original.copy(description = "Lunch", amountMinorUnits = 1_250))

        val stored = expenseDao.getById("e1")
        assertThat(stored?.description).isEqualTo("Lunch")
        assertThat(stored?.amountMinorUnits).isEqualTo(1_250)
    }
}
