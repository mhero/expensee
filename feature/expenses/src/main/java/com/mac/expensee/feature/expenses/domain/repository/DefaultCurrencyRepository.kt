package com.mac.expensee.feature.expenses.domain.repository

import com.mac.expensee.core.common.money.CurrencyCode

/**
 * Read-only access to the user's default-currency setting, for defaulting a *new* expense's
 * currency -- deliberately not a full `SettingsRepository` dependency (that lives in
 * `feature:settings`, and no feature module depends on another, per the project README) and
 * deliberately not observed reactively: a new expense's currency is decided once, when the form
 * opens, from whatever the setting was at that moment. If it changed mid-edit, the currency the
 * user started the entry in should not silently shift underneath them.
 */
interface DefaultCurrencyRepository {
    suspend fun getDefaultCurrency(): CurrencyCode
}
