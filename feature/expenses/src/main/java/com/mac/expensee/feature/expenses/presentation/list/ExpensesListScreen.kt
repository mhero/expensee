package com.mac.expensee.feature.expenses.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mac.expensee.core.ui.components.EmptyState
import com.mac.expensee.core.ui.components.ErrorState
import com.mac.expensee.core.ui.components.FullScreenLoading
import com.mac.expensee.core.ui.components.MoneyText
import com.mac.expensee.core.ui.components.UiState
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.mac.expensee.feature.expenses.domain.model.ExpenseCategory

@Composable
fun ExpensesListRoute(
    onAddExpense: () -> Unit,
    onExpenseClick: (String) -> Unit,
    viewModel: ExpensesListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    ExpensesListScreen(
        state = state,
        onAction = viewModel::onAction,
        onAddExpense = onAddExpense,
        onExpenseClick = onExpenseClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpensesListScreen(
    state: ExpensesListUiState,
    onAction: (ExpensesListAction) -> Unit,
    onAddExpense: () -> Unit,
    onExpenseClick: (String) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Expenses") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpense) {
                Icon(Icons.Filled.Add, contentDescription = "Add expense")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            CategoryFilterRow(
                categories = state.categories,
                selectedCategoryId = state.selectedCategoryId,
                onSelect = { onAction(ExpensesListAction.SelectCategoryFilter(it)) },
            )
            state.total?.let {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text(text = "Total: ", style = MaterialTheme.typography.bodyLarge)
                    MoneyText(money = it, style = MaterialTheme.typography.bodyLarge)
                }
            }
            when (val itemsState = state.items) {
                UiState.Loading -> FullScreenLoading()
                is UiState.Error -> ErrorState(message = itemsState.message)
                is UiState.Content -> {
                    if (itemsState.data.isEmpty()) {
                        EmptyState(
                            title = "No expenses yet",
                            message = "Tap + to record your first expense.",
                        )
                    } else {
                        ExpensesLazyList(
                            items = itemsState.data,
                            onExpenseClick = onExpenseClick,
                            onDelete = { onAction(ExpensesListAction.DeleteExpense(it)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<ExpenseCategory>,
    selectedCategoryId: String?,
    onSelect: (String?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(selected = selectedCategoryId == null, onClick = { onSelect(null) }, label = { Text("All") })
        }
        items(categories, key = { it.id }) { category ->
            FilterChip(
                selected = selectedCategoryId == category.id,
                onClick = { onSelect(category.id) },
                label = { Text(category.name) },
            )
        }
    }
}

@Composable
private fun ExpensesLazyList(
    items: List<ExpenseListItem>,
    onExpenseClick: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(items, key = { it.expense.id }) { item ->
            ExpenseRow(
                item = item,
                onClick = { onExpenseClick(item.expense.id) },
                onDelete = { onDelete(item.expense.id) },
            )
        }
    }
}

@Composable
private fun ExpenseRow(item: ExpenseListItem, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clip(CircleShape)
                    .background(categoryColor(item.categoryColorHex))
                    .size(10.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.expense.description, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${item.categoryName} \u2022 ${formatDate(item.expense.date)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            MoneyText(money = item.expense.amount, style = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete expense")
            }
        }
    }
}

private fun categoryColor(hex: String): Color = runCatching { Color(android.graphics.Color.parseColor(hex)) }
    .getOrDefault(Color.Gray)

private fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(epochMillis))
