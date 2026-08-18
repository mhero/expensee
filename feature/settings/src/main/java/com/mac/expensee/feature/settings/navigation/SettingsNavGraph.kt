package com.mac.expensee.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mac.expensee.feature.settings.presentation.SettingsRoute

/**
 * `onLogout` is passed in from the app module (same pattern `dashboardGraph` used for its now-
 * removed logout button) rather than this graph reaching into `feature:auth` itself -- see
 * project README on cross-feature navigation.
 */
fun NavGraphBuilder.settingsGraph(onLogout: () -> Unit, onBack: () -> Unit) {
    composable(SettingsDestinations.HOME_ROUTE) {
        SettingsRoute(onLogout = onLogout, onBack = onBack)
    }
}
