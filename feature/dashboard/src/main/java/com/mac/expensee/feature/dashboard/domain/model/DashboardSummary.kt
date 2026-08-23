package com.mac.expensee.feature.dashboard.domain.model

import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.common.money.Money

/**
 * [availableCurrencies] lists every currency present among this month's expenses (unfiltered by
 * [selectedCurrency]) -- the UI uses it to decide whether a currency selector is even needed (see
 * [com.mac.expensee.feature.dashboard.presentation.DashboardScreen]'s `CurrencySelector`: hidden
 * entirely when there's zero or one). Every other field here is already scoped to
 * [selectedCurrency] -- see [com.mac.expensee.feature.dashboard.data.DashboardSummaryBuilder] for
 * how that scoping (and the fallback when nothing matches) is chosen.
 */
data class DashboardSummary(
    val monthlyTotal: Money,
    val expenseCount: Int,
    val categoryBreakdown: List<CategorySpend>,
    val recentExpenses: List<RecentExpense>,
    val dailyTotals: List<DailyTotal>,
    val availableCurrencies: List<CurrencyCode>,
    val selectedCurrency: CurrencyCode,
)

data class CategorySpend(
    val categoryId: String,
    val categoryName: String,
    val colorHex: String,
    val total: Money,
    /** 0f..1f share of [DashboardSummary.monthlyTotal], precomputed so the UI never divides by zero. */
    val fraction: Float,
)

data class RecentExpense(
    val id: String,
    val description: String,
    val categoryName: String,
    val amount: Money,
    val date: Long,
)

/** One bar in the small monthly chart. [dayOfMonth] is 1-based. */
data class DailyTotal(
    val dayOfMonth: Int,
    val amountMinorUnits: Long,
)
