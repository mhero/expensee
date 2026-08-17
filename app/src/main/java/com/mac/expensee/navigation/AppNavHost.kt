package com.mac.expensee.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mac.expensee.core.ui.components.FullScreenLoading
import com.mac.expensee.feature.auth.navigation.AuthDestinations
import com.mac.expensee.feature.auth.navigation.authGraph
import com.mac.expensee.feature.expenses.navigation.ExpensesDestinations
import com.mac.expensee.feature.expenses.navigation.expensesGraph
import org.koin.androidx.compose.koinViewModel

/**
 * Root nav graph. Auth state decides the *start* destination once (on first composition after
 * account-existence is known); after that, [RootViewModel.session] flips the two graphs by
 * navigating imperatively, since Navigation Compose doesn't support swapping start destinations
 * after the graph is built.
 */
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val rootViewModel: RootViewModel = koinViewModel()
    val hasAccount by rootViewModel.hasAccount.collectAsState()
    val session by rootViewModel.session.collectAsState()

    val accountKnown = hasAccount
    if (accountKnown == null) {
        FullScreenLoading()
        return
    }

    var hasNavigatedForSession by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = if (session != null) {
            AppDestinations.HOME_ROUTE
        } else if (accountKnown) {
            AuthDestinations.LOGIN_ROUTE
        } else {
            AuthDestinations.SETUP_ROUTE
        },
    ) {
        authGraph(
            navController = navController,
            onAuthenticated = {
                navController.navigate(AppDestinations.HOME_ROUTE) {
                    popUpTo(0) { inclusive = true }
                }
            },
        )
        composable(AppDestinations.HOME_ROUTE) {
            val currentSession = session
            if (currentSession == null) {
                FullScreenLoading()
            } else {
                HomeScreen(
                    username = currentSession.user.username,
                    onLogout = { rootViewModel.logout() },
                    onViewExpenses = { navController.navigate(ExpensesDestinations.LIST_ROUTE) },
                )
            }
        }
        expensesGraph(navController)
    }

    // Session was cleared (logout) while sitting on an authenticated screen: fall back to login.
    LaunchedEffect(session) {
        if (session == null && !hasNavigatedForSession && navController.currentDestination?.route == AppDestinations.HOME_ROUTE) {
            navController.navigate(AuthDestinations.LOGIN_ROUTE) {
                popUpTo(0) { inclusive = true }
            }
        }
        hasNavigatedForSession = session != null
    }
}
