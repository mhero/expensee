package com.mac.expensee.feature.expenses.data

import com.mac.expensee.core.database.dao.CategoryDao
import com.mac.expensee.feature.expenses.data.mapper.toExpenseCategory
import com.mac.expensee.feature.expenses.domain.model.ExpenseCategory
import com.mac.expensee.feature.expenses.domain.repository.ExpenseCategoryLookupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExpenseCategoryLookupRepositoryImpl(
    private val categoryDao: CategoryDao,
) : ExpenseCategoryLookupRepository {

    override fun observeCategories(): Flow<List<ExpenseCategory>> =
        categoryDao.observeAll().map { entities -> entities.map { it.toExpenseCategory() } }

    override suspend fun getCategoryById(id: String): ExpenseCategory? =
        categoryDao.getById(id)?.toExpenseCategory()
}
