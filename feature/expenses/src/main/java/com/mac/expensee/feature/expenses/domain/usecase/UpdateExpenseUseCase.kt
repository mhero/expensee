package com.mac.expensee.feature.expenses.domain.usecase

import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.common.result.asError
import com.mac.expensee.feature.expenses.domain.model.Expense
import com.mac.expensee.feature.expenses.domain.repository.ExpenseRepository
import com.mac.expensee.feature.expenses.domain.validation.ExpenseValidator

class UpdateExpenseUseCase(private val expenseRepository: ExpenseRepository) {
    suspend operator fun invoke(expense: Expense): AppResult<Expense> {
        ExpenseValidator.validate(expense)?.let { return it.asError() }
        return expenseRepository.updateExpense(expense)
    }
}
