package com.mac.expensee.feature.expenses.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mac.expensee.feature.expenses.presentation.addedit.AddEditExpenseRoute
import com.mac.expensee.feature.expenses.presentation.detail.ExpenseDetailRoute
import com.mac.expensee.feature.expenses.presentation.list.ExpensesListRoute

/** Same modular-navigation pattern as `feature:auth`'s `authGraph` -- see that file's KDoc. */
fun NavGraphBuilder.expensesGraph(navController: NavHostController) {
    composable(ExpensesDestinations.LIST_ROUTE) {
        ExpensesListRoute(
            onAddExpense = { navController.navigate(ExpensesDestinations.ADD_ROUTE) },
            onExpenseClick = { navController.navigate(ExpensesDestinations.detailRoute(it)) },
        )
    }
    composable(ExpensesDestinations.ADD_ROUTE) {
        AddEditExpenseRoute(
            expenseId = null,
            onDone = { navController.popBackStack() },
            onNavigateUp = { navController.popBackStack() },
        )
    }
    composable(
        route = ExpensesDestinations.EDIT_ROUTE,
        arguments = listOf(navArgument(ExpensesDestinations.ARG_EXPENSE_ID) { type = NavType.StringType }),
    ) { backStackEntry ->
        val expenseId = backStackEntry.arguments?.getString(ExpensesDestinations.ARG_EXPENSE_ID)
        AddEditExpenseRoute(
            expenseId = expenseId,
            onDone = { navController.popBackStack() },
            onNavigateUp = { navController.popBackStack() },
        )
    }
    composable(
        route = ExpensesDestinations.DETAIL_ROUTE,
        arguments = listOf(navArgument(ExpensesDestinations.ARG_EXPENSE_ID) { type = NavType.StringType }),
    ) { backStackEntry ->
        val expenseId = backStackEntry.arguments?.getString(ExpensesDestinations.ARG_EXPENSE_ID).orEmpty()
        ExpenseDetailRoute(
            expenseId = expenseId,
            onNavigateUp = { navController.popBackStack() },
            onEdit = { navController.navigate(ExpensesDestinations.editRoute(it)) },
            onDeleted = { navController.popBackStack() },
        )
    }
}
