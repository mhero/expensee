package com.mac.expensee.feature.dashboard

import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.common.sync.SyncStatus
import com.mac.expensee.core.database.entity.CategoryEntity
import com.mac.expensee.core.database.entity.ExpenseEntity
import com.mac.expensee.feature.dashboard.data.DashboardSummaryBuilder
import org.junit.Test

class DashboardSummaryBuilderTest {

    private fun category(id: String, name: String) = CategoryEntity(
        localId = id,
        remoteId = null,
        name = name,
        colorHex = "#EF6C00",
        icon = "default",
        isDefault = false,
        createdAt = 0,
        updatedAt = 0,
        syncStatus = SyncStatus.SYNCED,
        deletedAt = null,
        version = 1,
    )

    private fun expense(id: String, categoryId: String, amount: Long, date: Long, currencyCode: String = "USD") =
        ExpenseEntity(
            localId = id,
            remoteId = null,
            categoryLocalId = categoryId,
            amountMinorUnits = amount,
            currencyCode = currencyCode,
            description = "Expense $id",
            notes = null,
            date = date,
            receiptUri = null,
            createdAt = date,
            updatedAt = date,
            syncStatus = SyncStatus.SYNCED,
            deletedAt = null,
            version = 1,
        )

    @Test
    fun `empty expenses produce a zero summary without dividing by zero`() {
        val summary = DashboardSummaryBuilder.build(expenses = emptyList(), categories = emptyList())

        assertThat(summary.monthlyTotal.amountMinorUnits).isEqualTo(0)
        assertThat(summary.expenseCount).isEqualTo(0)
        assertThat(summary.categoryBreakdown).isEmpty()
        assertThat(summary.recentExpenses).isEmpty()
    }

    @Test
    fun `an empty month falls back to the given fallback currency`() {
        val summary = DashboardSummaryBuilder.build(
            expenses = emptyList(),
            categories = emptyList(),
            fallbackCurrency = CurrencyCode.EUR,
        )

        assertThat(summary.selectedCurrency).isEqualTo(CurrencyCode.EUR)
        assertThat(summary.monthlyTotal.currency).isEqualTo(CurrencyCode.EUR)
        assertThat(summary.availableCurrencies).isEmpty()
    }

    @Test
    fun `total is the sum of all expenses`() {
        val summary = DashboardSummaryBuilder.build(
            expenses = listOf(
                expense("e1", "cat-1", 1000, 1_000L),
                expense("e2", "cat-1", 500, 2_000L),
            ),
            categories = listOf(category("cat-1", "Food")),
        )

        assertThat(summary.monthlyTotal.amountMinorUnits).isEqualTo(1500)
        assertThat(summary.expenseCount).isEqualTo(2)
    }

    @Test
    fun `category breakdown fractions sum to 1 and are sorted by spend descending`() {
        val summary = DashboardSummaryBuilder.build(
            expenses = listOf(
                expense("e1", "cat-food", 3000, 1_000L),
                expense("e2", "cat-transport", 1000, 1_000L),
            ),
            categories = listOf(category("cat-food", "Food"), category("cat-transport", "Transport")),
        )

        assertThat(summary.categoryBreakdown).hasSize(2)
        assertThat(summary.categoryBreakdown.first().categoryName).isEqualTo("Food")
        assertThat(summary.categoryBreakdown.sumOf { it.fraction.toDouble() }).isWithin(0.001).of(1.0)
    }

    @Test
    fun `an expense referencing a deleted category falls back to Uncategorized`() {
        val summary = DashboardSummaryBuilder.build(
            expenses = listOf(expense("e1", "missing-cat", 500, 1_000L)),
            categories = emptyList(),
        )

        assertThat(summary.categoryBreakdown.single().categoryName).isEqualTo("Uncategorized")
    }

    @Test
    fun `recent expenses are capped at 5 and sorted newest first`() {
        val expenses = (1..8).map { expense("e$it", "cat-1", 100, it.toLong() * 1000) }

        val summary = DashboardSummaryBuilder.build(expenses = expenses, categories = listOf(category("cat-1", "Food")))

        assertThat(summary.recentExpenses).hasSize(5)
        assertThat(summary.recentExpenses.first().id).isEqualTo("e8")
    }

    @Test
    fun `a single currency present needs no selector -- it is used automatically`() {
        val summary = DashboardSummaryBuilder.build(
            expenses = listOf(expense("e1", "cat-1", 1000, 1_000L, currencyCode = "EUR")),
            categories = listOf(category("cat-1", "Food")),
        )

        assertThat(summary.availableCurrencies).containsExactly(CurrencyCode.EUR)
        assertThat(summary.selectedCurrency).isEqualTo(CurrencyCode.EUR)
        assertThat(summary.monthlyTotal.currency).isEqualTo(CurrencyCode.EUR)
    }

    @Test
    fun `multiple currencies are all listed as available but the summary is scoped to one`() {
        val summary = DashboardSummaryBuilder.build(
            expenses = listOf(
                expense("usd-1", "cat-1", 1000, 1_000L, currencyCode = "USD"),
                expense("eur-1", "cat-1", 2000, 1_000L, currencyCode = "EUR"),
            ),
            categories = listOf(category("cat-1", "Food")),
            preferredCurrency = CurrencyCode.EUR,
        )

        assertThat(summary.availableCurrencies).containsExactly(CurrencyCode.EUR, CurrencyCode.USD).inOrder()
        assertThat(summary.selectedCurrency).isEqualTo(CurrencyCode.EUR)
        assertThat(summary.monthlyTotal.amountMinorUnits).isEqualTo(2000)
        assertThat(summary.expenseCount).isEqualTo(1) // only the EUR expense is in scope
        assertThat(summary.recentExpenses).hasSize(1)
        assertThat(summary.recentExpenses.single().id).isEqualTo("eur-1")
    }

    @Test
    fun `with no preferred currency and multiple present, the summary defaults to the first available`() {
        val summary = DashboardSummaryBuilder.build(
            expenses = listOf(
                expense("usd-1", "cat-1", 1000, 1_000L, currencyCode = "USD"),
                expense("eur-1", "cat-1", 2000, 1_000L, currencyCode = "EUR"),
            ),
            categories = listOf(category("cat-1", "Food")),
            preferredCurrency = null,
        )

        // availableCurrencies is sorted by ISO code, so EUR sorts before USD -- this is simply
        // "some deterministic default", not a claim that EUR is more relevant than USD.
        assertThat(summary.selectedCurrency).isEqualTo(CurrencyCode.EUR)
    }

    @Test
    fun `a preferred currency no longer present this month falls back instead of showing nothing`() {
        val summary = DashboardSummaryBuilder.build(
            expenses = listOf(expense("usd-1", "cat-1", 1000, 1_000L, currencyCode = "USD")),
            categories = listOf(category("cat-1", "Food")),
            preferredCurrency = CurrencyCode.EUR, // e.g. user picked EUR last month; none exist this month
        )

        assertThat(summary.selectedCurrency).isEqualTo(CurrencyCode.USD)
        assertThat(summary.expenseCount).isEqualTo(1)
    }
}
