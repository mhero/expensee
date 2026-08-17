package com.mac.expensee.feature.expenses.domain.repository

import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.feature.expenses.domain.model.Expense
import com.mac.expensee.feature.expenses.domain.model.ExpenseFilter
import kotlinx.coroutines.flow.Flow

/**
 * The UI/use-case layer's only view of expense persistence. Backed today purely by Room
 * (`ExpenseRepositoryImpl`); a `RemoteExpenseDataSource` already exists (see
 * `data.remote.RemoteExpenseDataSource`) but nothing calls it yet -- wiring it in means adding
 * push/pull calls inside the repository impl, not changing this interface or anything above it.
 */
interface ExpenseRepository {

    fun observeExpenses(filter: ExpenseFilter = ExpenseFilter.All): Flow<List<Expense>>

    fun observeExpenseById(id: String): Flow<Expense?>

    suspend fun getExpenseById(id: String): AppResult<Expense>

    suspend fun addExpense(expense: Expense): AppResult<Expense>

    suspend fun updateExpense(expense: Expense): AppResult<Expense>

    suspend fun deleteExpense(id: String): AppResult<Unit>
}
