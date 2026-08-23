package com.mac.expensee.feature.dashboard.data

import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.common.money.Money
import com.mac.expensee.core.database.entity.CategoryEntity
import com.mac.expensee.core.database.entity.ExpenseEntity
import com.mac.expensee.feature.dashboard.domain.model.CategorySpend
import com.mac.expensee.feature.dashboard.domain.model.DailyTotal
import com.mac.expensee.feature.dashboard.domain.model.DashboardSummary
import com.mac.expensee.feature.dashboard.domain.model.RecentExpense
import java.util.Calendar

private const val RECENT_EXPENSES_LIMIT = 5

/**
 * Pure aggregation logic, deliberately split out of [DashboardRepositoryImpl] so it can be unit
 * tested against plain entity lists -- no Room, no Flow, no Android framework classes involved.
 *
 * Every expense carries its own currency (see `ExpenseEntity.currencyCode`), so a month can
 * legitimately contain more than one -- adding two different currencies' minor units together
 * would silently produce a meaningless number, not an error, so this never happens. Instead the
 * whole summary is scoped to a single [DashboardSummary.selectedCurrency] at a time:
 * - [preferredCurrency] is what the UI has explicitly picked via `DashboardAction.CurrencySelected`
 *   (null until the user picks one, or if a previous pick no longer matches anything this month).
 * - If [preferredCurrency] isn't set or doesn't appear in this month's expenses, this falls back
 *   to the sole currency present, and only reaches [fallbackCurrency] (the user's default-currency
 *   setting) when there's nothing to infer from at all -- an empty month.
 */
object DashboardSummaryBuilder {

    fun build(
        expenses: List<ExpenseEntity>,
        categories: List<CategoryEntity>,
        preferredCurrency: CurrencyCode? = null,
        fallbackCurrency: CurrencyCode = CurrencyCode.USD,
    ): DashboardSummary {
        val availableCurrencies = expenses
            .map { CurrencyCode.fromIsoCode(it.currencyCode) }
            .distinct()
            .sortedBy { it.isoCode }

        val currency = preferredCurrency
            ?.takeIf { it in availableCurrencies }
            ?: availableCurrencies.singleOrNull()
            ?: availableCurrencies.firstOrNull()
            ?: fallbackCurrency

        val scoped = expenses.filter { CurrencyCode.fromIsoCode(it.currencyCode) == currency }
        val categoryById = categories.associateBy { it.localId }

        val totalMinorUnits = scoped.sumOf { it.amountMinorUnits }
        val monthlyTotal = Money(totalMinorUnits, currency)

        val breakdown = scoped
            .groupBy { it.categoryLocalId }
            .map { (categoryId, group) ->
                val categoryTotal = group.sumOf { it.amountMinorUnits }
                val category = categoryById[categoryId]
                CategorySpend(
                    categoryId = categoryId,
                    categoryName = category?.name ?: "Uncategorized",
                    colorHex = category?.colorHex ?: "#9E9E9E",
                    total = Money(categoryTotal, currency),
                    fraction = if (totalMinorUnits == 0L) 0f else categoryTotal.toFloat() / totalMinorUnits.toFloat(),
                )
            }
            .sortedByDescending { it.total.amountMinorUnits }

        val recent = scoped
            .sortedByDescending { it.date }
            .take(RECENT_EXPENSES_LIMIT)
            .map { expense ->
                RecentExpense(
                    id = expense.localId,
                    description = expense.description,
                    categoryName = categoryById[expense.categoryLocalId]?.name ?: "Uncategorized",
                    amount = Money(expense.amountMinorUnits, currency),
                    date = expense.date,
                )
            }

        val daily = scoped
            .groupBy { dayOfMonth(it.date) }
            .map { (day, group) -> DailyTotal(day, group.sumOf { it.amountMinorUnits }) }
            .sortedBy { it.dayOfMonth }

        return DashboardSummary(
            monthlyTotal = monthlyTotal,
            expenseCount = scoped.size,
            categoryBreakdown = breakdown,
            recentExpenses = recent,
            dailyTotals = daily,
            availableCurrencies = availableCurrencies,
            selectedCurrency = currency,
        )
    }

    private fun dayOfMonth(epochMillis: Long): Int =
        Calendar.getInstance().apply { timeInMillis = epochMillis }.get(Calendar.DAY_OF_MONTH)
}
