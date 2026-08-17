package com.mac.expensee.feature.expenses.domain.usecase

import com.mac.expensee.feature.expenses.domain.model.Expense
import com.mac.expensee.feature.expenses.domain.model.ExpenseFilter
import com.mac.expensee.feature.expenses.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

class ObserveExpensesUseCase(private val expenseRepository: ExpenseRepository) {
    operator fun invoke(filter: ExpenseFilter = ExpenseFilter.All): Flow<List<Expense>> =
        expenseRepository.observeExpenses(filter)
}
