package com.mac.expensee.feature.dashboard

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.common.money.Money
import com.mac.expensee.core.testing.MainDispatcherRule
import com.mac.expensee.core.ui.components.UiState
import com.mac.expensee.feature.dashboard.domain.model.DashboardSummary
import com.mac.expensee.feature.dashboard.domain.repository.DashboardRepository
import com.mac.expensee.feature.dashboard.domain.usecase.ObserveDashboardSummaryUseCase
import com.mac.expensee.feature.dashboard.presentation.DashboardAction
import com.mac.expensee.feature.dashboard.presentation.DashboardViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private class FakeDashboardRepository : DashboardRepository {
    /** Records every (start, end, preferredCurrency) this was asked to observe, in call order. */
    val requestedCurrencies = mutableListOf<CurrencyCode?>()

    override fun observeMonthlySummary(
        monthStartInclusive: Long,
        monthEndInclusive: Long,
        preferredCurrency: CurrencyCode?,
    ): Flow<DashboardSummary> {
        requestedCurrencies += preferredCurrency
        val currency = preferredCurrency ?: CurrencyCode.USD
        return flowOf(
            DashboardSummary(
                monthlyTotal = Money(0, currency),
                expenseCount = 0,
                categoryBreakdown = emptyList(),
                recentExpenses = emptyList(),
                dailyTotals = emptyList(),
                availableCurrencies = listOf(CurrencyCode.USD, CurrencyCode.EUR),
                selectedCurrency = currency,
            ),
        )
    }
}

/** Covers the new `DashboardAction.CurrencySelected` wiring -- the resolution logic itself (what
 *  a given preferred/available combination resolves to) is [DashboardSummaryBuilderTest]'s job. */
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `starts with no preferred currency (null) until the user picks one`() = runTest {
        val repository = FakeDashboardRepository()
        val viewModel = DashboardViewModel(ObserveDashboardSummaryUseCase(repository))

        viewModel.state.test { cancelAndIgnoreRemainingEvents() }

        assertThat(repository.requestedCurrencies.first()).isNull()
    }

    @Test
    fun `selecting a currency re-observes the summary scoped to it`() = runTest {
        val repository = FakeDashboardRepository()
        val viewModel = DashboardViewModel(ObserveDashboardSummaryUseCase(repository))

        viewModel.onAction(DashboardAction.CurrencySelected(CurrencyCode.EUR))

        viewModel.state.test {
            val content = awaitItem().summary as UiState.Content
            assertThat(content.data.selectedCurrency).isEqualTo(CurrencyCode.EUR)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(repository.requestedCurrencies).contains(CurrencyCode.EUR)
    }
}
