package com.mac.expensee.feature.expenses.domain.repository

import com.mac.expensee.feature.expenses.domain.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow

/** Read-only category access for the expense picker/list -- see [ExpenseCategory] for why this
 *  is separate from `feature:categories`' own (future) CRUD repository. */
interface ExpenseCategoryLookupRepository {
    fun observeCategories(): Flow<List<ExpenseCategory>>
    suspend fun getCategoryById(id: String): ExpenseCategory?
}
