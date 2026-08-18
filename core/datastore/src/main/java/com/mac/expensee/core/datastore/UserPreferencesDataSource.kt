package com.mac.expensee.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "expensee_preferences")

/**
 * Thin wrapper around Jetpack (Preferences) DataStore, deliberately kept at the raw key/value
 * level -- like `core:database`'s DAOs, it has no business logic of its own (default values,
 * enum parsing, validation). `feature:settings`'s `SettingsRepositoryImpl` maps these raw values
 * onto the `AppSettings` domain model, the same way `ExpenseRepositoryImpl` maps Room entities
 * onto `Expense`.
 *
 * Room is deliberately NOT used for these values: they're small, unstructured, singleton-per-app
 * settings with no query/relational needs, which is exactly what DataStore is for.
 */
class UserPreferencesDataSource(private val context: Context) {

    val currencyCode: Flow<String?> = context.dataStore.data.map { it[CURRENCY_CODE] }
    val themeMode: Flow<String?> = context.dataStore.data.map { it[THEME_MODE] }
    val notificationsEnabled: Flow<Boolean?> = context.dataStore.data.map { it[NOTIFICATIONS_ENABLED] }

    suspend fun setCurrencyCode(isoCode: String) {
        context.dataStore.edit { prefs -> prefs[CURRENCY_CODE] = isoCode }
    }

    suspend fun setThemeMode(name: String) {
        context.dataStore.edit { prefs -> prefs[THEME_MODE] = name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[NOTIFICATIONS_ENABLED] = enabled }
    }

    private companion object {
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }
}
