package com.mac.expensee.feature.expenses.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.common.money.Money
import com.mac.expensee.core.ui.components.ErrorState
import com.mac.expensee.core.ui.components.FullScreenLoading
import com.mac.expensee.core.ui.components.MoneyText
import com.mac.expensee.core.ui.components.UiState
import com.mac.expensee.core.ui.theme.ExpenseeTheme
import com.mac.expensee.core.ui.theme.Spacing
import com.mac.expensee.feature.expenses.domain.model.Expense
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ExpenseDetailRoute(
    expenseId: String,
    onNavigateUp: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit,
    viewModel: ExpenseDetailViewModel = koinViewModel { parametersOf(expenseId) },
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.deleteSucceeded) {
        if (state.deleteSucceeded) onDeleted()
    }

    ExpenseDetailScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateUp = onNavigateUp,
        onEdit = { onEdit(expenseId) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseDetailScreen(
    state: ExpenseDetailUiState,
    onAction: (ExpenseDetailAction) -> Unit,
    onNavigateUp: () -> Unit,
    onEdit: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                },
            )
        },
    ) { padding ->
        when (val expenseState = state.expense) {
            UiState.Loading -> FullScreenLoading(modifier = Modifier.padding(padding))
            is UiState.Error -> ErrorState(message = expenseState.message, modifier = Modifier.padding(padding))
            is UiState.Content -> ExpenseDetailContentView(
                content = expenseState.data,
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this expense?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onAction(ExpenseDetailAction.Delete)
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

private val PREVIEW_CONTENT = ExpenseDetailContent(
    expense = Expense(
        id = "e1",
        categoryId = "c1",
        amount = Money(2499, CurrencyCode.USD),
        description = "Groceries",
        notes = "Weekly shop at the farmers market",
        date = System.currentTimeMillis(),
        receiptPath = null,
    ),
    categoryName = "Food",
)

@Preview(showBackground = true)
@Composable
private fun ExpenseDetailScreenPreview() {
    ExpenseeTheme(dynamicColor = false) {
        ExpenseDetailScreen(
            state = ExpenseDetailUiState(expense = UiState.Content(PREVIEW_CONTENT)),
            onAction = {},
            onNavigateUp = {},
            onEdit = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseDetailScreenLoadingPreview() {
    ExpenseeTheme(dynamicColor = false) {
        ExpenseDetailScreen(state = ExpenseDetailUiState(), onAction = {}, onNavigateUp = {}, onEdit = {})
    }
}

@Composable
private fun ExpenseDetailContentView(content: ExpenseDetailContent, modifier: Modifier = Modifier) {
    val expense = content.expense
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        MoneyText(money = expense.amount, style = MaterialTheme.typography.titleLarge)
        Text(text = expense.description, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = "${content.categoryName} \u2022 ${formatDate(expense.date)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        expense.notes?.takeIf { it.isNotBlank() }?.let {
            Text(text = it, style = MaterialTheme.typography.bodyLarge)
        }
        expense.receiptPath?.let { path ->
            AsyncImage(
                model = File(path),
                contentDescription = "Receipt image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp)),
            )
        }
    }
}

private fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(epochMillis))
