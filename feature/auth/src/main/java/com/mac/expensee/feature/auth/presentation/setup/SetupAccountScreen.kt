package com.mac.expensee.feature.auth.presentation.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.mac.expensee.core.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun SetupAccountRoute(
    onSetupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SetupAccountViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.setupSucceeded) {
        if (state.setupSucceeded) onSetupSuccess()
    }

    SetupAccountScreen(state = state, onAction = viewModel::onAction, onNavigateToLogin = onNavigateToLogin)
}

@Composable
private fun SetupAccountScreen(
    state: SetupAccountUiState,
    onAction: (SetupAccountAction) -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PaddingValues(Spacing.extraLarge)),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Create your account", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Expensee stores everything on this device.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = Spacing.extraLarge, top = Spacing.extraSmall),
        )

        OutlinedTextField(
            value = state.username,
            onValueChange = { onAction(SetupAccountAction.UsernameChanged(it)) },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = { onAction(SetupAccountAction.PasswordChanged(it)) },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.small),
        )
        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = { onAction(SetupAccountAction.ConfirmPasswordChanged(it)) },
            label = { Text("Confirm password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.small),
        )

        state.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = Spacing.small),
            )
        }

        Button(
            onClick = { onAction(SetupAccountAction.Submit) },
            enabled = !state.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.large),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.padding(Spacing.tiny))
            } else {
                Text("Create account")
            }
        }

        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.extraSmall),
        ) {
            Text("Already have an account? Sign in")
        }
    }
}
