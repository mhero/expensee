package com.mac.expensee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.mac.expensee.core.ui.theme.ExpenseeTheme
import com.mac.expensee.navigation.AppNavHost

class MainActivity : ComponentActivity() {
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
    ExpenseeTheme {
        AppNavHost()
    }
}
