package com.mac.expensee.feature.expenses.data

import com.mac.expensee.core.common.dispatcher.DispatcherProvider
import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.common.result.DataError
import com.mac.expensee.core.common.result.asError
import com.mac.expensee.core.common.result.asSuccess
import com.mac.expensee.core.database.dao.ExpenseDao
import com.mac.expensee.feature.expenses.data.mapper.toDomain
import com.mac.expensee.feature.expenses.data.mapper.toEntity
import com.mac.expensee.feature.expenses.domain.model.Expense
import com.mac.expensee.feature.expenses.domain.model.ExpenseFilter
import com.mac.expensee.feature.expenses.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Room is the only source of truth queried here -- see class docs on [ExpenseRepository] for how
 * a remote push/pull would slot in later. The UI never knows this; it only sees [Expense] and
 * [AppResult].
 */
class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao,
    private val dispatcherProvider: DispatcherProvider,
) : ExpenseRepository {

    override fun observeExpenses(filter: ExpenseFilter): Flow<List<Expense>> {
        val entitiesFlow = when (filter) {
            ExpenseFilter.All -> expenseDao.observeAll()
            is ExpenseFilter.ByCategory -> expenseDao.observeByCategory(filter.categoryId)
            is ExpenseFilter.ByDateRange -> expenseDao.observeInRange(filter.fromInclusive, filter.toInclusive)
        }
        return entitiesFlow.map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeExpenseById(id: String): Flow<Expense?> =
        expenseDao.observeById(id).map { it?.toDomain() }

    override suspend fun getExpenseById(id: String): AppResult<Expense> = withContext(dispatcherProvider.io) {
        val entity = expenseDao.getById(id) ?: return@withContext DataError.Local.NotFound.asError()
        entity.toDomain().asSuccess()
    }

    override suspend fun addExpense(expense: Expense): AppResult<Expense> = withContext(dispatcherProvider.io) {
        val entity = expense.toEntity(existing = null, now = System.currentTimeMillis())
        expenseDao.upsert(entity)
        entity.toDomain().asSuccess()
    }

    override suspend fun updateExpense(expense: Expense): AppResult<Expense> = withContext(dispatcherProvider.io) {
        val existing = expenseDao.getById(expense.id)
            ?: return@withContext DataError.Local.NotFound.asError()
        val entity = expense.toEntity(existing = existing, now = System.currentTimeMillis())
        expenseDao.update(entity)
        entity.toDomain().asSuccess()
    }

    override suspend fun deleteExpense(id: String): AppResult<Unit> = withContext(dispatcherProvider.io) {
        val existing = expenseDao.getById(id) ?: return@withContext DataError.Local.NotFound.asError()
        expenseDao.softDelete(localId = existing.localId, deletedAt = System.currentTimeMillis())
        Unit.asSuccess()
    }
}
