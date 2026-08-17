package com.mac.expensee.feature.categories.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.mac.expensee.feature.categories.presentation.list.CategoriesRoute

fun NavGraphBuilder.categoriesGraph(navController: NavHostController) {
    composable(CategoriesDestinations.LIST_ROUTE) {
        CategoriesRoute(onNavigateUp = { navController.popBackStack() })
    }
}
