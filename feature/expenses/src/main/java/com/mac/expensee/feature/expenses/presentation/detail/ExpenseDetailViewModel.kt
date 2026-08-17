package com.mac.expensee.feature.expenses.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.ui.components.UiState
import com.mac.expensee.feature.expenses.domain.repository.ExpenseCategoryLookupRepository
import com.mac.expensee.feature.expenses.domain.usecase.DeleteExpenseUseCase
import com.mac.expensee.feature.expenses.domain.usecase.ObserveExpenseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExpenseDetailViewModel(
    private val expenseId: String,
    private val observeExpenseUseCase: ObserveExpenseUseCase,
    private val categoryLookupRepository: ExpenseCategoryLookupRepository,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseDetailUiState())
    val state: StateFlow<ExpenseDetailUiState> = _state.asStateFlow()

    init {
        observeExpenseUseCase(expenseId)
            .onEach { expense ->
                if (expense == null) {
                    _state.update { it.copy(expense = UiState.Error("This expense no longer exists.")) }
                } else {
                    val categoryName = categoryLookupRepository.getCategoryById(expense.categoryId)?.name ?: "Uncategorized"
                    _state.update { it.copy(expense = UiState.Content(ExpenseDetailContent(expense, categoryName))) }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: ExpenseDetailAction) {
        when (action) {
            ExpenseDetailAction.Delete -> viewModelScope.launch {
                when (deleteExpenseUseCase(expenseId)) {
                    is AppResult.Success -> _state.update { it.copy(deleteSucceeded = true) }
                    is AppResult.Error -> Unit
                }
            }
        }
    }
}
