package com.mac.expensee.feature.dashboard.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.common.money.Money
import com.mac.expensee.core.ui.components.EmptyState
import com.mac.expensee.core.ui.components.ErrorState
import com.mac.expensee.core.ui.components.FullScreenLoading
import com.mac.expensee.core.ui.components.MoneyText
import com.mac.expensee.core.ui.components.UiState
import com.mac.expensee.core.ui.theme.ExpenseeTheme
import com.mac.expensee.core.ui.theme.Spacing
import com.mac.expensee.feature.dashboard.domain.model.CategorySpend
import com.mac.expensee.feature.dashboard.domain.model.DailyTotal
import com.mac.expensee.feature.dashboard.domain.model.DashboardSummary
import com.mac.expensee.feature.dashboard.domain.model.RecentExpense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

/** Content wider than this is capped and centered rather than stretched edge-to-edge on tablets/foldables. */
private val MAX_CONTENT_WIDTH = 640.dp

/** Below this, a phone-sized layout is used; at or above it (large phones in landscape, foldables,
 *  tablets), sections get roomier side margins and gaps -- see [DashboardContent]. */
private const val WIDE_SCREEN_BREAKPOINT_DP = 600

@Composable
fun DashboardRoute(
    onManageCategories: () -> Unit,
    onViewAllExpenses: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    DashboardScreen(
        state = state,
        onAction = viewModel::onAction,
        onManageCategories = onManageCategories,
        onViewAllExpenses = onViewAllExpenses,
        onOpenSettings = onOpenSettings,
    )
}

/** `internal`, not `private`, so an instrumented UI test can drive it directly with a fixed [DashboardUiState]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DashboardScreen(
    state: DashboardUiState,
    onAction: (DashboardAction) -> Unit,
    onManageCategories: () -> Unit,
    onViewAllExpenses: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
    ) { padding ->
        when (val summaryState = state.summary) {
            UiState.Loading -> FullScreenLoading(modifier = Modifier.padding(padding))
            is UiState.Error -> ErrorState(message = summaryState.message, modifier = Modifier.padding(padding))
            is UiState.Content -> DashboardContent(
                summary = summaryState.data,
                onAction = onAction,
                onManageCategories = onManageCategories,
                onViewAllExpenses = onViewAllExpenses,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

/**
 * Each top-level section (summary card, currency selector, chart, category breakdown, recent
 * expenses) is a single `LazyColumn` item, with [Arrangement.spacedBy] controlling the gap
 * *between* them -- deliberately more generous than the rhythm *within* a section (see
 * [CategoryBreakdownRow]/[RecentExpenseRow], spaced by [Spacing.medium]), so sections read as
 * distinct cards rather than one continuous list. Grouping a section's header and rows into one
 * nested, non-lazy `Column` inside its own `Card` (rather than feeding every row into the outer
 * `LazyColumn` via `items(...)`) is also what makes that per-section spacing -- and a shared card
 * surface across every section -- possible in the first place: `Arrangement.spacedBy` has no way
 * to tell "a new section started" from "the next row in this one" otherwise.
 *
 * Screen width, not just orientation, decides the spacing/margins: [LocalConfiguration]'s
 * `screenWidthDp` covers phones in landscape and foldables the same way it covers tablets, which a
 * plain portrait/landscape check wouldn't. Content is also capped at [MAX_CONTENT_WIDTH] and
 * centered on wide screens, rather than letting a summary card or chart stretch edge-to-edge.
 */
@Composable
private fun DashboardContent(
    summary: DashboardSummary,
    onAction: (DashboardAction) -> Unit,
    onManageCategories: () -> Unit,
    onViewAllExpenses: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isWideScreen = LocalConfiguration.current.screenWidthDp >= WIDE_SCREEN_BREAKPOINT_DP
    val horizontalPadding = if (isWideScreen) Spacing.xxl else Spacing.large
    val sectionSpacing = if (isWideScreen) Spacing.xxl else Spacing.extraLarge

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = Spacing.extraLarge),
        verticalArrangement = Arrangement.spacedBy(sectionSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item { DashboardSection { SummaryHeader(summary) } }
        if (summary.availableCurrencies.size > 1) {
            item { DashboardSection { CurrencySelector(summary = summary, onAction = onAction) } }
        }
        item { DashboardSection { MonthlyChart(summary.dailyTotals) } }
        item {
            DashboardSection {
                CategoryBreakdownSection(breakdown = summary.categoryBreakdown, onManageCategories = onManageCategories)
            }
        }
        item {
            DashboardSection {
                RecentExpensesSection(recentExpenses = summary.recentExpenses, onViewAllExpenses = onViewAllExpenses)
            }
        }
    }
}

/** Caps and centers one section's content on wide screens -- see [DashboardContent]'s KDoc. */
@Composable
private fun DashboardSection(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().widthIn(max = MAX_CONTENT_WIDTH)) {
        content()
    }
}

@Composable
private fun SummaryHeader(summary: DashboardSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            Text(text = "This month", style = MaterialTheme.typography.labelSmall)
            MoneyText(money = summary.monthlyTotal, style = MaterialTheme.typography.titleLarge)
            Text(
                text = "${summary.expenseCount} expense${if (summary.expenseCount == 1) "" else "s"}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Only ever shown when [DashboardSummary.availableCurrencies] has more than one entry (see
 * [DashboardContent]) -- with a single currency in play there's nothing to choose between, so no
 * selector is rendered at all, per the project's spending-tracker scope (no cross-currency
 * conversion or a combined multi-currency total -- see [DashboardSummary]'s KDoc).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencySelector(summary: DashboardSummary, onAction: (DashboardAction) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
        items(summary.availableCurrencies, key = { it.isoCode }) { currency ->
            FilterChip(
                selected = currency == summary.selectedCurrency,
                onClick = { onAction(DashboardAction.CurrencySelected(currency)) },
                label = { Text(currency.isoCode) },
            )
        }
    }
}

@Composable
private fun MonthlyChart(dailyTotals: List<DailyTotal>) {
    if (dailyTotals.isEmpty()) return
    val maxAmount = dailyTotals.maxOf { it.amountMinorUnits }.coerceAtLeast(1L)
    val barColor = MaterialTheme.colorScheme.primary

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            Text(text = "Daily spending", style = MaterialTheme.typography.labelSmall)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(top = Spacing.small),
            ) {
                val barCount = dailyTotals.size
                val gap = size.width * 0.02f / barCount.coerceAtLeast(1)
                val barWidth = (size.width / barCount) - gap
                dailyTotals.forEachIndexed { index, day ->
                    val barHeight = (day.amountMinorUnits.toFloat() / maxAmount.toFloat()) * size.height
                    drawRect(
                        color = barColor,
                        topLeft = Offset(x = index * (barWidth + gap), y = size.height - barHeight),
                        size = Size(width = barWidth, height = barHeight),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onAction),
        )
    }
}

/** Wrapped in a [Card] (same as [SummaryHeader]/[MonthlyChart]) so every section shares one
 *  consistent surface style, instead of the header-and-rows floating directly on the screen
 *  background the way it used to. */
@Composable
private fun CategoryBreakdownSection(
    breakdown: List<CategorySpend>,
    onManageCategories: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            SectionHeader(title = "Spending by category", actionLabel = "Manage", onAction = onManageCategories)
            breakdown.forEachIndexed { index, spend ->
                CategoryBreakdownRow(spend, modifier = Modifier.padding(top = if (index == 0) 0.dp else Spacing.medium))
            }
        }
    }
}

@Composable
private fun CategoryBreakdownRow(spend: CategorySpend, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .padding(end = Spacing.small)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(categoryColor(spend.colorHex)),
                )
                Text(text = spend.categoryName, style = MaterialTheme.typography.bodyLarge)
            }
            MoneyText(money = spend.total, style = MaterialTheme.typography.bodyLarge)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .padding(top = Spacing.extraSmall)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = spend.fraction.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(categoryColor(spend.colorHex)),
            )
        }
    }
}

/** Wrapped in a [Card], same reasoning as [CategoryBreakdownSection]'s KDoc. */
@Composable
private fun RecentExpensesSection(
    recentExpenses: List<RecentExpense>,
    onViewAllExpenses: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            SectionHeader(title = "Recent expenses", actionLabel = "View all", onAction = onViewAllExpenses)
            if (recentExpenses.isEmpty()) {
                EmptyState(title = "No expenses this month", message = "Add one to see it here.")
            } else {
                recentExpenses.forEachIndexed { index, expense ->
                    RecentExpenseRow(expense, modifier = Modifier.padding(top = if (index == 0) 0.dp else Spacing.medium))
                }
            }
        }
    }
}

@Composable
private fun RecentExpenseRow(expense: RecentExpense, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(text = expense.description, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${expense.categoryName} \u2022 ${formatDate(expense.date)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        MoneyText(money = expense.amount, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun categoryColor(hex: String): Color = runCatching { Color(android.graphics.Color.parseColor(hex)) }
    .getOrDefault(Color.Gray)

private fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(epochMillis))

private val PREVIEW_BREAKDOWN = listOf(
    CategorySpend(categoryId = "c1", categoryName = "Food", colorHex = "#EF6C00", total = Money(8500, CurrencyCode.USD), fraction = 0.55f),
    CategorySpend(categoryId = "c2", categoryName = "Transport", colorHex = "#1E88E5", total = Money(4200, CurrencyCode.USD), fraction = 0.27f),
    CategorySpend(categoryId = "c3", categoryName = "Shopping", colorHex = "#8E24AA", total = Money(2800, CurrencyCode.USD), fraction = 0.18f),
)

private val PREVIEW_RECENT = listOf(
    RecentExpense(id = "e1", description = "Groceries", categoryName = "Food", amount = Money(2499, CurrencyCode.USD), date = System.currentTimeMillis()),
    RecentExpense(id = "e2", description = "Train ticket", categoryName = "Transport", amount = Money(1899, CurrencyCode.USD), date = System.currentTimeMillis()),
)

private val PREVIEW_DAILY_TOTALS = (1..10).map { day -> DailyTotal(dayOfMonth = day, amountMinorUnits = (day * 731L) % 4000) }

private val PREVIEW_SUMMARY_SINGLE_CURRENCY = DashboardSummary(
    monthlyTotal = Money(15500, CurrencyCode.USD),
    expenseCount = 12,
    categoryBreakdown = PREVIEW_BREAKDOWN,
    recentExpenses = PREVIEW_RECENT,
    dailyTotals = PREVIEW_DAILY_TOTALS,
    availableCurrencies = listOf(CurrencyCode.USD),
    selectedCurrency = CurrencyCode.USD,
)

private val PREVIEW_SUMMARY_MULTI_CURRENCY = PREVIEW_SUMMARY_SINGLE_CURRENCY.copy(
    availableCurrencies = listOf(CurrencyCode.EUR, CurrencyCode.USD),
)

private val PREVIEW_SUMMARY_EMPTY = DashboardSummary(
    monthlyTotal = Money(0, CurrencyCode.USD),
    expenseCount = 0,
    categoryBreakdown = emptyList(),
    recentExpenses = emptyList(),
    dailyTotals = emptyList(),
    availableCurrencies = emptyList(),
    selectedCurrency = CurrencyCode.USD,
)

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    ExpenseeTheme(dynamicColor = false) {
        DashboardScreen(
            state = DashboardUiState(summary = UiState.Content(PREVIEW_SUMMARY_SINGLE_CURRENCY)),
            onAction = {},
            onManageCategories = {},
            onViewAllExpenses = {},
            onOpenSettings = {},
        )
    }
}

/** With more than one currency present this month, `CurrencySelector`'s chip row appears above the chart. */
@Preview(showBackground = true)
@Composable
private fun DashboardScreenMultiCurrencyPreview() {
    ExpenseeTheme(dynamicColor = false) {
        DashboardScreen(
            state = DashboardUiState(summary = UiState.Content(PREVIEW_SUMMARY_MULTI_CURRENCY)),
            onAction = {},
            onManageCategories = {},
            onViewAllExpenses = {},
            onOpenSettings = {},
        )
    }
}

/** A wide preview (e.g. tablet width) to sanity-check the capped, centered content -- see [DashboardContent]'s KDoc. */
@Preview(showBackground = true, widthDp = 840, heightDp = 600)
@Composable
private fun DashboardScreenWideScreenPreview() {
    ExpenseeTheme(dynamicColor = false) {
        DashboardScreen(
            state = DashboardUiState(summary = UiState.Content(PREVIEW_SUMMARY_MULTI_CURRENCY)),
            onAction = {},
            onManageCategories = {},
            onViewAllExpenses = {},
            onOpenSettings = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenEmptyPreview() {
    ExpenseeTheme(dynamicColor = false) {
        DashboardScreen(
            state = DashboardUiState(summary = UiState.Content(PREVIEW_SUMMARY_EMPTY)),
            onAction = {},
            onManageCategories = {},
            onViewAllExpenses = {},
            onOpenSettings = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenLoadingPreview() {
    ExpenseeTheme(dynamicColor = false) {
        DashboardScreen(
            state = DashboardUiState(),
            onAction = {},
            onManageCategories = {},
            onViewAllExpenses = {},
            onOpenSettings = {},
        )
    }
}
