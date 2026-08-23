package com.mac.expensee.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.ui.components.UiState
import com.mac.expensee.feature.dashboard.domain.usecase.ObserveDashboardSummaryUseCase
import java.time.ZoneId
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class DashboardViewModel(observeDashboardSummaryUseCase: ObserveDashboardSummaryUseCase) : ViewModel() {

    /** Null until the user picks a currency via [onAction] -- see [DashboardSummary][com.mac.expensee.feature.dashboard.domain.model.DashboardSummary]'s KDoc for how that's resolved. */
    private val selectedCurrency = MutableStateFlow<CurrencyCode?>(null)

    val state: StateFlow<DashboardUiState> = run {
        val (start, end) = currentMonthRange()
        selectedCurrency
            .flatMapLatest { preferred -> observeDashboardSummaryUseCase(start, end, preferred) }
            .map { DashboardUiState(summary = UiState.Content(it)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun onAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.CurrencySelected -> selectedCurrency.update { action.currency }
        }
    }

    private fun currentMonthRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val yearMonth = YearMonth.now(zone)
        val start = yearMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        return start to end
    }
}
