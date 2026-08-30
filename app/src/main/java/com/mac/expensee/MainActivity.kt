package com.mac.expensee

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
        // Screens built on Scaffold (Dashboard, Categories, ...) get an opaque background for
        // free -- Scaffold paints one by default. LoginScreen/SetupAccountScreen don't use
        // Scaffold and have no background of their own, so without this Surface they'd fall
        // through to the Activity window's background whenever nothing else covers it. This
        // Surface is what actually paints colorScheme.background for every screen, so dark mode
        // isn't only correct for the screens that happen to use Scaffold.
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            AppNavHost()
        }
    }
}

