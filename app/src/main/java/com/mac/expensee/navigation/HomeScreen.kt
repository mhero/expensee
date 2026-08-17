package com.mac.expensee.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Temporary landing screen for an authenticated session. Replaced by the real Dashboard
 * (`feature:dashboard`) in a later phase; for now it's the hub that routes into the features
 * that do exist (`feature:expenses`) and exposes logout, which will move to `feature:settings`
 * once that module lands.
 */
@Composable
fun HomeScreen(username: String, onLogout: () -> Unit, onViewExpenses: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Welcome, $username", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Categories, dashboard and settings land in a later phase.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
        Button(onClick = onViewExpenses, modifier = Modifier.fillMaxWidth()) {
            Text("View expenses")
        }
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Log out")
        }
    }
}
