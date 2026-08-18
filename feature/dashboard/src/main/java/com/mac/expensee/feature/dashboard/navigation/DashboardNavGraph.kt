package com.mac.expensee.feature.dashboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mac.expensee.feature.dashboard.presentation.DashboardRoute

/**
 * Unlike `authGraph`/`expensesGraph`/`categoriesGraph`, this graph's navigation callbacks point
 * at *other* features' routes (expenses list, categories list, settings). Those routes are passed
 * in as plain string constants by the app module -- `feature:dashboard` never imports
 * `feature:expenses`/`feature:categories`/`feature:settings` to get them, preserving the
 * no-feature-depends-on-another-feature rule while still letting the app compose them together.
 */
fun NavGraphBuilder.dashboardGraph(
    onManageCategories: () -> Unit,
    onViewAllExpenses: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    composable(DashboardDestinations.HOME_ROUTE) {
        DashboardRoute(onManageCategories = onManageCategories, onViewAllExpenses = onViewAllExpenses, onOpenSettings = onOpenSettings)
    }
}
