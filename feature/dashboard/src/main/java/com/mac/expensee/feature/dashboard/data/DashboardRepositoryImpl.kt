package com.mac.expensee.feature.dashboard.data

import com.mac.expensee.core.database.dao.CategoryDao
import com.mac.expensee.core.database.dao.ExpenseDao
import com.mac.expensee.feature.dashboard.domain.model.DashboardSummary
import com.mac.expensee.feature.dashboard.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class DashboardRepositoryImpl(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
) : DashboardRepository {

    override fun observeMonthlySummary(monthStartInclusive: Long, monthEndInclusive: Long): Flow<DashboardSummary> =
        combine(
            expenseDao.observeInRange(monthStartInclusive, monthEndInclusive),
            categoryDao.observeAll(),
        ) { expenses, categories -> DashboardSummaryBuilder.build(expenses, categories) }
}
