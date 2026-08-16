package com.mac.expensee.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Temporary landing screen for an authenticated session. Replaced by the real Dashboard
 * (`feature:dashboard`) in the next phase; its purpose right now is to prove the auth-gated nav
 * graph switch works end-to-end.
 */
@Composable
fun HomeScreen(username: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Welcome, $username", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Expenses, categories, dashboard and settings land in the next phase.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
        Button(onClick = onLogout) {
            Text("Log out")
        }
    }
}
