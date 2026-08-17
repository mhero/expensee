package com.mac.expensee.feature.expenses.presentation.detail

import com.mac.expensee.core.ui.components.UiState
import com.mac.expensee.feature.expenses.domain.model.Expense

data class ExpenseDetailUiState(
    val expense: UiState<ExpenseDetailContent> = UiState.Loading,
    val deleteSucceeded: Boolean = false,
)

data class ExpenseDetailContent(
    val expense: Expense,
    val categoryName: String,
)

sealed interface ExpenseDetailAction {
    data object Delete : ExpenseDetailAction
}
