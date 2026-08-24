package com.mac.expensee.feature.dashboard.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mac.expensee.core.ui.components.EmptyState
import com.mac.expensee.core.ui.components.ErrorState
import com.mac.expensee.core.ui.components.FullScreenLoading
import com.mac.expensee.core.ui.components.MoneyText
import com.mac.expensee.core.ui.components.UiState
import com.mac.expensee.core.ui.theme.Spacing
import com.mac.expensee.feature.dashboard.domain.model.CategorySpend
import com.mac.expensee.feature.dashboard.domain.model.DailyTotal
import com.mac.expensee.feature.dashboard.domain.model.DashboardSummary
import com.mac.expensee.feature.dashboard.domain.model.RecentExpense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

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

@Composable
private fun DashboardContent(
    summary: DashboardSummary,
    onAction: (DashboardAction) -> Unit,
    onManageCategories: () -> Unit,
    onViewAllExpenses: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(Spacing.large)) {
        item { SummaryHeader(summary) }
        if (summary.availableCurrencies.size > 1) {
            item { CurrencySelector(summary = summary, onAction = onAction, modifier = Modifier.padding(top = Spacing.small)) }
        }
        item { MonthlyChart(summary.dailyTotals, modifier = Modifier.padding(top = Spacing.large)) }
        item { SectionHeader(title = "Spending by category", actionLabel = "Manage", onAction = onManageCategories) }
        items(summary.categoryBreakdown, key = { it.categoryId }) { CategoryBreakdownRow(it) }
        item {
            SectionHeader(
                title = "Recent expenses",
                actionLabel = "View all",
                onAction = onViewAllExpenses,
                modifier = Modifier.padding(top = Spacing.large),
            )
        }
        if (summary.recentExpenses.isEmpty()) {
            item { EmptyState(title = "No expenses this month", message = "Add one to see it here.") }
        } else {
            items(summary.recentExpenses, key = { it.id }) { RecentExpenseRow(it) }
        }
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
private fun CurrencySelector(
    summary: DashboardSummary,
    onAction: (DashboardAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
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
private fun MonthlyChart(dailyTotals: List<DailyTotal>, modifier: Modifier = Modifier) {
    if (dailyTotals.isEmpty()) return
    val maxAmount = dailyTotals.maxOf { it.amountMinorUnits }.coerceAtLeast(1L)
    val barColor = MaterialTheme.colorScheme.primary

    Card(modifier = modifier.fillMaxWidth()) {
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
                        topLeft = androidx.compose.ui.geometry.Offset(
                            x = index * (barWidth + gap),
                            y = size.height - barHeight,
                        ),
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
            .padding(vertical = Spacing.small),
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

@Composable
private fun CategoryBreakdownRow(spend: CategorySpend) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.tight)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.layout.Box(
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
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .padding(top = Spacing.extraSmall)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = spend.fraction.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(categoryColor(spend.colorHex)),
            )
        }
    }
}

@Composable
private fun RecentExpenseRow(expense: RecentExpense) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.tight),
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
