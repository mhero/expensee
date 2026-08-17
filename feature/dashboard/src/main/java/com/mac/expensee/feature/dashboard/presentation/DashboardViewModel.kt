package com.mac.expensee.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mac.expensee.core.ui.components.UiState
import com.mac.expensee.feature.dashboard.domain.usecase.ObserveDashboardSummaryUseCase
import java.time.ZoneId
import java.time.YearMonth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(observeDashboardSummaryUseCase: ObserveDashboardSummaryUseCase) : ViewModel() {

    val state: StateFlow<DashboardUiState> = run {
        val (start, end) = currentMonthRange()
        observeDashboardSummaryUseCase(start, end)
            .map { DashboardUiState(summary = UiState.Content(it)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    private fun currentMonthRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val yearMonth = YearMonth.now(zone)
        val start = yearMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        return start to end
    }
}
