package com.mac.expensee.feature.expenses.domain.usecase

import com.mac.expensee.feature.expenses.domain.model.Expense
import com.mac.expensee.feature.expenses.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

class ObserveExpenseUseCase(private val expenseRepository: ExpenseRepository) {
    operator fun invoke(id: String): Flow<Expense?> = expenseRepository.observeExpenseById(id)
}
