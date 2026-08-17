package com.mac.expensee.feature.expenses.domain.usecase

import com.mac.expensee.feature.expenses.domain.model.ExpenseCategory
import com.mac.expensee.feature.expenses.domain.repository.ExpenseCategoryLookupRepository
import kotlinx.coroutines.flow.Flow

class ObserveExpenseCategoriesUseCase(private val repository: ExpenseCategoryLookupRepository) {
    operator fun invoke(): Flow<List<ExpenseCategory>> = repository.observeCategories()
}
