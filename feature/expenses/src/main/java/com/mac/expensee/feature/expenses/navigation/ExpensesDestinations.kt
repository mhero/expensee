package com.mac.expensee.feature.expenses.navigation

object ExpensesDestinations {
    const val LIST_ROUTE = "expenses/list"
    const val ADD_ROUTE = "expenses/add"
    const val EDIT_ROUTE = "expenses/edit/{expenseId}"
    const val DETAIL_ROUTE = "expenses/detail/{expenseId}"

    fun editRoute(expenseId: String) = "expenses/edit/$expenseId"
    fun detailRoute(expenseId: String) = "expenses/detail/$expenseId"

    const val ARG_EXPENSE_ID = "expenseId"
}
