package com.mac.expensee.feature.expenses.domain.usecase

import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.feature.expenses.domain.repository.DefaultCurrencyRepository

class GetDefaultCurrencyUseCase(private val repository: DefaultCurrencyRepository) {
    suspend operator fun invoke(): CurrencyCode = repository.getDefaultCurrency()
}
