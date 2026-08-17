package com.mac.expensee.feature.expenses.domain.model

/** What subset of expenses a list screen is currently showing. */
sealed interface ExpenseFilter {
    data object All : ExpenseFilter
    data class ByCategory(val categoryId: String) : ExpenseFilter
    data class ByDateRange(val fromInclusive: Long, val toInclusive: Long) : ExpenseFilter
}
