package com.mac.expensee.feature.expenses

import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.common.result.DataError
import com.mac.expensee.core.common.result.asError
import com.mac.expensee.core.common.result.asSuccess
import com.mac.expensee.feature.expenses.domain.model.Expense
import com.mac.expensee.feature.expenses.domain.model.ExpenseFilter
import com.mac.expensee.feature.expenses.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory fake -- no Room -- used across `feature:expenses` unit tests. */
class FakeExpenseRepository : ExpenseRepository {
    private val expenses = MutableStateFlow<Map<String, Expense>>(emptyMap())

    override fun observeExpenses(filter: ExpenseFilter) = expenses.map { map ->
        val all = map.values.toList()
        when (filter) {
            ExpenseFilter.All -> all
            is ExpenseFilter.ByCategory -> all.filter { it.categoryId == filter.categoryId }
            is ExpenseFilter.ByDateRange -> all.filter { it.date in filter.fromInclusive..filter.toInclusive }
        }
    }

    override fun observeExpenseById(id: String) = expenses.map { it[id] }

    override suspend fun getExpenseById(id: String): AppResult<Expense> =
        expenses.value[id]?.asSuccess() ?: DataError.Local.NotFound.asError()

    override suspend fun addExpense(expense: Expense): AppResult<Expense> {
        expenses.value = expenses.value + (expense.id to expense)
        return expense.asSuccess()
    }

    override suspend fun updateExpense(expense: Expense): AppResult<Expense> {
        if (!expenses.value.containsKey(expense.id)) return DataError.Local.NotFound.asError()
        expenses.value = expenses.value + (expense.id to expense)
        return expense.asSuccess()
    }

    override suspend fun deleteExpense(id: String): AppResult<Unit> {
        if (!expenses.value.containsKey(id)) return DataError.Local.NotFound.asError()
        expenses.value = expenses.value - id
        return Unit.asSuccess()
    }

    /** Test-only synchronous snapshot -- avoids collecting the Flow just to assert on state. */
    fun currentExpenses(): List<Expense> = expenses.value.values.toList()
}
