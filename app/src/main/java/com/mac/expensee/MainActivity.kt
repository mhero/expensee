package com.mac.expensee

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import com.mac.expensee.core.ui.theme.ExpenseeTheme
import com.mac.expensee.feature.settings.domain.model.ThemeMode
import com.mac.expensee.navigation.AppNavHost
import com.mac.expensee.theme.AppThemeViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Extends [FragmentActivity] (not plain `ComponentActivity`) because [androidx.biometric.BiometricPrompt],
 * used from the Settings screen, requires a `FragmentActivity` -- see
 * `feature:settings`'s `BiometricSection`. `setContent` still works normally since
 * `FragmentActivity` is itself a `ComponentActivity`.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseeApp()
        }
    }
}

@Composable
private fun ExpenseeApp() {
    val themeViewModel: AppThemeViewModel = koinViewModel()
    val themeMode by themeViewModel.themeMode.collectAsState()
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    ExpenseeTheme(darkTheme = darkTheme) {
        AppNavHost()
    }
}

