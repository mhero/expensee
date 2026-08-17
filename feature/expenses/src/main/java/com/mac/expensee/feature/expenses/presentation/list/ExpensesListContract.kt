package com.mac.expensee.feature.expenses.presentation.list

import com.mac.expensee.core.common.money.Money
import com.mac.expensee.core.ui.components.UiState
import com.mac.expensee.feature.expenses.domain.model.Expense
import com.mac.expensee.feature.expenses.domain.model.ExpenseCategory

data class ExpenseListItem(
    val expense: Expense,
    val categoryName: String,
    val categoryColorHex: String,
)

data class ExpensesListUiState(
    val items: UiState<List<ExpenseListItem>> = UiState.Loading,
    val categories: List<ExpenseCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val total: Money? = null,
)

sealed interface ExpensesListAction {
    data class SelectCategoryFilter(val categoryId: String?) : ExpensesListAction
    data class DeleteExpense(val expenseId: String) : ExpensesListAction
}
