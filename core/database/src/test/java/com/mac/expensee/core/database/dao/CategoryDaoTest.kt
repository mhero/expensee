package com.mac.expensee.core.database.dao

import android.database.sqlite.SQLiteConstraintException
import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.common.sync.SyncStatus
import com.mac.expensee.core.database.entity.CategoryEntity
import com.mac.expensee.core.database.entity.ExpenseEntity
import com.mac.expensee.core.testing.testFirst
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test

class CategoryDaoTest : RoomDaoTest() {

    private val categoryDao get() = db.categoryDao()
    private val expenseDao get() = db.expenseDao()

    private fun category(
        localId: String,
        name: String = "Food",
        syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD,
        deletedAt: Long? = null,
        version: Int = 1,
    ) = CategoryEntity(
        localId = localId,
        remoteId = null,
        name = name,
        colorHex = "#EF6C00",
        icon = "default",
        isDefault = false,
        createdAt = 0L,
        updatedAt = 0L,
        syncStatus = syncStatus,
        deletedAt = deletedAt,
        version = version,
    )

    @Test
    fun `observeAll excludes soft-deleted categories and orders by name`() = runTest {
        categoryDao.upsert(category("c1", name = "Transport"))
        categoryDao.upsert(category("c2", name = "Food"))
        categoryDao.upsert(category("c3", name = "Gone", deletedAt = 1L))

        categoryDao.observeAll().testFirst { list ->
            assertThat(list.map { it.name }).containsExactly("Food", "Transport").inOrder()
        }
    }

    @Test
    fun `insertAll ignores a row whose localId already exists`() = runTest {
        categoryDao.upsert(category("c1", name = "Original"))

        categoryDao.insertAll(listOf(category("c1", name = "Should be ignored")))

        assertThat(categoryDao.getById("c1")?.name).isEqualTo("Original")
    }

    @Test
    fun `upsert replaces an existing row with the same localId`() = runTest {
        categoryDao.upsert(category("c1", name = "Original"))

        categoryDao.upsert(category("c1", name = "Replaced"))

        assertThat(categoryDao.getById("c1")?.name).isEqualTo("Replaced")
    }

    @Test
    fun `count includes soft-deleted rows (used only to decide whether to seed defaults)`() = runTest {
        categoryDao.upsert(category("c1"))
        categoryDao.upsert(category("c2", deletedAt = 1L))

        assertThat(categoryDao.count()).isEqualTo(2)
    }

    @Test
    fun `softDelete tombstones the row and bumps its version`() = runTest {
        categoryDao.upsert(category("c1", version = 1))

        categoryDao.softDelete("c1", deletedAt = 5_000L)

        val stored = categoryDao.getById("c1")
        assertThat(stored?.deletedAt).isEqualTo(5_000L)
        assertThat(stored?.syncStatus).isEqualTo(SyncStatus.PENDING_DELETE)
        assertThat(stored?.version).isEqualTo(2)
    }

    @Test
    fun `hardDelete removes the row entirely`() = runTest {
        categoryDao.upsert(category("c1"))

        categoryDao.hardDelete("c1")

        assertThat(categoryDao.getById("c1")).isNull()
    }

    @Test
    fun `purgeSyncedTombstones only removes rows that are both deleted and synced`() = runTest {
        categoryDao.upsert(category("synced-tombstone", deletedAt = 1L, syncStatus = SyncStatus.SYNCED))
        categoryDao.upsert(category("pending-tombstone", deletedAt = 1L, syncStatus = SyncStatus.PENDING_DELETE))

        categoryDao.purgeSyncedTombstones()

        assertThat(categoryDao.getById("synced-tombstone")).isNull()
        assertThat(categoryDao.getById("pending-tombstone")).isNotNull()
    }

    @Test
    fun `a category still referenced by an expense cannot be hard-deleted`() = runTest {
        categoryDao.upsert(category("c1"))
        expenseDao.upsert(
            ExpenseEntity(
                localId = "e1",
                remoteId = null,
                categoryLocalId = "c1",
                amountMinorUnits = 500,
                currencyCode = "USD",
                description = "Coffee",
                notes = null,
                date = 1_000L,
                receiptUri = null,
                createdAt = 1_000L,
                updatedAt = 1_000L,
                syncStatus = SyncStatus.PENDING_UPLOAD,
                deletedAt = null,
                version = 1,
            ),
        )

        // The ForeignKey.RESTRICT on ExpenseEntity.categoryLocalId is the DB-level backstop behind
        // feature:categories' DeleteCategoryUseCase reference check -- this proves it's real, not
        // just enforced in application code.
        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking { categoryDao.hardDelete("c1") }
        }
    }
}
