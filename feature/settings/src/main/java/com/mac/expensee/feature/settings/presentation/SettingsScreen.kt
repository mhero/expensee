package com.mac.expensee.feature.settings.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.security.biometric.BiometricAuthenticator
import com.mac.expensee.core.security.biometric.BiometricAvailability
import com.mac.expensee.core.security.biometric.BiometricResult
import com.mac.expensee.core.ui.theme.Spacing
import com.mac.expensee.feature.settings.domain.model.ThemeMode
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun SettingsRoute(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    SettingsScreen(
        state = state,
        onAction = viewModel::onAction,
        onLogout = onLogout,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.large),
        ) {
            CurrencySection(selected = state.currency, onSelected = { onAction(SettingsAction.CurrencyChanged(it)) })
            ThemeSection(selected = state.theme, onSelected = { onAction(SettingsAction.ThemeChanged(it)) })
            NotificationsSection(
                enabled = state.notificationsEnabled,
                onToggle = { onAction(SettingsAction.NotificationsToggled(it)) },
            )
            BiometricSection(
                enabled = state.biometricUnlockEnabled,
                onToggle = { onAction(SettingsAction.BiometricToggled(it)) },
            )
            LogoutSection(onClick = { showLogoutConfirm = true })
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log out?") },
            text = { Text("You'll need your password (or biometric unlock) to sign back in.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    onLogout()
                }) { Text("Log out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SettingsSectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.large), verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencySection(selected: CurrencyCode, onSelected: (CurrencyCode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    SettingsSectionCard(title = "Currency") {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
                readOnly = true,
                value = "${selected.isoCode} (${selected.symbol})",
                onValueChange = {},
                label = { Text("Default currency") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                CurrencyCode.entries.forEach { currency ->
                    DropdownMenuItem(
                        text = { Text("${currency.isoCode} (${currency.symbol})") },
                        onClick = {
                            expanded = false
                            onSelected(currency)
                        },
                    )
                }
            }
        }
        Text(
            text = "Used as the default when adding new expenses.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ThemeSection(selected: ThemeMode, onSelected: (ThemeMode) -> Unit) {
    SettingsSectionCard(title = "Theme") {
        ThemeMode.entries.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(mode) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = selected == mode, onClick = { onSelected(mode) })
                Text(text = mode.label(), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
    ThemeMode.SYSTEM -> "Follow system"
}

@Composable
private fun NotificationsSection(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    val context = LocalContext.current
    val needsRuntimePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> onToggle(granted) }

    SettingsSectionCard(title = "Notifications") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Expense reminders", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "An occasional nudge to log today's spending.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = { target ->
                    val permissionAlreadyGranted = !needsRuntimePermission || hasNotificationPermission(context)
                    if (target && !permissionAlreadyGranted) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onToggle(target)
                    }
                },
            )
        }
    }
}

@Composable
private fun BiometricSection(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val biometricAuthenticator: BiometricAuthenticator = koinInject()
    val scope = rememberCoroutineScope()

    val availability = remember(activity) {
        activity?.let { biometricAuthenticator.availability(BiometricManager.from(it)) }
            ?: BiometricAvailability.Unavailable
    }
    val available = availability == BiometricAvailability.Available

    SettingsSectionCard(title = "Biometric unlock") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Unlock with fingerprint or face", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = if (available) {
                        "Skip your password next time using device biometrics."
                    } else {
                        "Set up a fingerprint or face unlock in your device settings first."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = enabled,
                enabled = available,
                onCheckedChange = { target ->
                    if (!target || activity == null) {
                        onToggle(target)
                        return@Switch
                    }
                    // Enabling requires proving the user can actually authenticate first --
                    // otherwise they could toggle on an unlock method that never worked.
                    // Disabling never needs that proof.
                    scope.launch {
                        biometricAuthenticator.authenticate(
                            activity = activity,
                            title = "Confirm it's you",
                            subtitle = "Enable biometric unlock for Expensee",
                        ).collect { result ->
                            if (result is BiometricResult.Success) onToggle(true)
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun LogoutSection(onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(Spacing.large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Text(text = "Log out", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun hasNotificationPermission(context: android.content.Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
