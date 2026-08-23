package com.mac.expensee.feature.dashboard.data

import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.database.dao.CategoryDao
import com.mac.expensee.core.database.dao.ExpenseDao
import com.mac.expensee.core.datastore.UserPreferencesDataSource
import com.mac.expensee.feature.dashboard.domain.model.DashboardSummary
import com.mac.expensee.feature.dashboard.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class DashboardRepositoryImpl(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val preferencesDataSource: UserPreferencesDataSource,
) : DashboardRepository {

    override fun observeMonthlySummary(
        monthStartInclusive: Long,
        monthEndInclusive: Long,
        preferredCurrency: CurrencyCode?,
    ): Flow<DashboardSummary> = combine(
        expenseDao.observeInRange(monthStartInclusive, monthEndInclusive),
        categoryDao.observeAll(),
        preferencesDataSource.currencyCode,
    ) { expenses, categories, defaultCurrencyCode ->
        DashboardSummaryBuilder.build(
            expenses = expenses,
            categories = categories,
            preferredCurrency = preferredCurrency,
            // Only reached for a month with zero expenses (see DashboardSummaryBuilder's KDoc) --
            // the user's default-currency setting is a better empty-state guess than a hardcoded USD.
            fallbackCurrency = defaultCurrencyCode?.let(CurrencyCode::fromIsoCode) ?: CurrencyCode.USD,
        )
    }
}
