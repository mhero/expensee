package com.mac.expensee.feature.expenses

import com.mac.expensee.core.common.sync.SyncStatus
import com.mac.expensee.core.database.dao.ExpenseDao
import com.mac.expensee.core.database.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Implements only what [com.mac.expensee.feature.expenses.data.ExpenseSyncGatewayImpl] actually
 * calls with real in-memory semantics; the `observe*`/`countByCategory` methods return a fixed
 * snapshot since sync-gateway tests never exercise them.
 */
class FakeExpenseDao : ExpenseDao {

    private val rows = mutableMapOf<String, ExpenseEntity>()

    fun seed(entity: ExpenseEntity) {
        rows[entity.localId] = entity
    }

    fun all(): List<ExpenseEntity> = rows.values.toList()

    override fun observeAll(): Flow<List<ExpenseEntity>> = flowOf(rows.values.filter { it.deletedAt == null })
    override fun observeByCategory(categoryLocalId: String): Flow<List<ExpenseEntity>> = flowOf(emptyList())
    override fun observeInRange(fromInclusive: Long, toInclusive: Long): Flow<List<ExpenseEntity>> = flowOf(emptyList())
    override fun observeById(localId: String): Flow<ExpenseEntity?> = flowOf(rows[localId])
    override fun observeRecent(fromInclusive: Long, toInclusive: Long, limit: Int): Flow<List<ExpenseEntity>> = flowOf(emptyList())

    override suspend fun getById(localId: String): ExpenseEntity? = rows[localId]

    override suspend fun getByRemoteId(remoteId: String): ExpenseEntity? = rows.values.find { it.remoteId == remoteId }

    override suspend fun upsert(expense: ExpenseEntity) {
        rows[expense.localId] = expense
    }

    override suspend fun update(expense: ExpenseEntity) {
        rows[expense.localId] = expense
    }

    override suspend fun softDelete(localId: String, deletedAt: Long, syncStatus: SyncStatus) {
        rows[localId]?.let { rows[localId] = it.copy(deletedAt = deletedAt, syncStatus = syncStatus, updatedAt = deletedAt) }
    }

    override suspend fun purgeSyncedTombstones(synced: SyncStatus) {
        rows.values.removeAll { it.deletedAt != null && it.syncStatus == synced }
    }

    override suspend fun getPendingSync(synced: SyncStatus): List<ExpenseEntity> =
        rows.values.filter { it.syncStatus != synced }

    override suspend fun countByCategory(categoryLocalId: String): Int = 0
}
