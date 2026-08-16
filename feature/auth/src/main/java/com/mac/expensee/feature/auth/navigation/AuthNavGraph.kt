package com.mac.expensee.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.mac.expensee.feature.auth.presentation.login.LoginRoute
import com.mac.expensee.feature.auth.presentation.setup.SetupAccountRoute

/**
 * Feature modules own their own nav graph; the app module only knows about route string
 * constants ([AuthDestinations]) and calls this builder extension. This keeps
 * `feature:expenses`, `feature:dashboard`, etc. from ever needing a compile-time dependency on
 * `feature:auth`'s internal screens.
 */
fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    onAuthenticated: () -> Unit,
) {
    composable(AuthDestinations.LOGIN_ROUTE) {
        LoginRoute(
            onLoginSuccess = onAuthenticated,
            onNavigateToSetup = {
                navController.navigate(AuthDestinations.SETUP_ROUTE) {
                    launchSingleTop = true
                }
            },
        )
    }
    composable(AuthDestinations.SETUP_ROUTE) {
        SetupAccountRoute(
            onSetupSuccess = onAuthenticated,
            onNavigateToLogin = { navController.popBackStack() },
        )
    }
}
