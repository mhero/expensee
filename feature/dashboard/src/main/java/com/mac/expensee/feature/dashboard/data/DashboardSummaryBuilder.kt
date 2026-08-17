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
 */
object DashboardSummaryBuilder {

    fun build(expenses: List<ExpenseEntity>, categories: List<CategoryEntity>): DashboardSummary {
        val currency = expenses.firstOrNull()?.let { CurrencyCode.fromIsoCode(it.currencyCode) } ?: CurrencyCode.USD
        val categoryById = categories.associateBy { it.localId }

        val totalMinorUnits = expenses.sumOf { it.amountMinorUnits }
        val monthlyTotal = Money(totalMinorUnits, currency)

        val breakdown = expenses
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

        val recent = expenses
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

        val daily = expenses
            .groupBy { dayOfMonth(it.date) }
            .map { (day, group) -> DailyTotal(day, group.sumOf { it.amountMinorUnits }) }
            .sortedBy { it.dayOfMonth }

        return DashboardSummary(
            monthlyTotal = monthlyTotal,
            expenseCount = expenses.size,
            categoryBreakdown = breakdown,
            recentExpenses = recent,
            dailyTotals = daily,
        )
    }

    private fun dayOfMonth(epochMillis: Long): Int =
        Calendar.getInstance().apply { timeInMillis = epochMillis }.get(Calendar.DAY_OF_MONTH)
}
