package com.mac.expensee.feature.expenses.data

import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.datastore.UserPreferencesDataSource
import com.mac.expensee.feature.expenses.domain.repository.DefaultCurrencyRepository
import kotlinx.coroutines.flow.first

/**
 * Maps `core:datastore`'s raw stored currency code onto [CurrencyCode], the same
 * unset-falls-back-to-USD rule `feature:settings`'s `SettingsRepositoryImpl` applies -- the two
 * are deliberately kept in sync (both read the same underlying preference) without either feature
 * module depending on the other.
 */
class DefaultCurrencyRepositoryImpl(
    private val preferencesDataSource: UserPreferencesDataSource,
) : DefaultCurrencyRepository {

    override suspend fun getDefaultCurrency(): CurrencyCode =
        preferencesDataSource.currencyCode.first()?.let(CurrencyCode::fromIsoCode) ?: CurrencyCode.USD
}
