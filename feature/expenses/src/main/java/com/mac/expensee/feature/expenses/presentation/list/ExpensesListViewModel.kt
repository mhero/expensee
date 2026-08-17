package com.mac.expensee.feature.expenses.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.common.money.Money
import com.mac.expensee.core.ui.components.UiState
import com.mac.expensee.feature.expenses.domain.model.ExpenseFilter
import com.mac.expensee.feature.expenses.domain.usecase.DeleteExpenseUseCase
import com.mac.expensee.feature.expenses.domain.usecase.ObserveExpenseCategoriesUseCase
import com.mac.expensee.feature.expenses.domain.usecase.ObserveExpensesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExpensesListViewModel(
    private val observeExpensesUseCase: ObserveExpensesUseCase,
    private val observeExpenseCategoriesUseCase: ObserveExpenseCategoriesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
) : ViewModel() {

    private val selectedCategoryId = MutableStateFlow<String?>(null)

    private val categories = observeExpenseCategoriesUseCase()

    private val filteredExpenses = selectedCategoryId.flatMapLatest { categoryId ->
        val filter = categoryId?.let { ExpenseFilter.ByCategory(it) } ?: ExpenseFilter.All
        observeExpensesUseCase(filter)
    }

    val state: StateFlow<ExpensesListUiState> = combine(
        filteredExpenses,
        categories,
        selectedCategoryId,
    ) { expenses, categoryList, selectedId ->
        val categoryById = categoryList.associateBy { it.id }
        val items = expenses.map { expense ->
            val category = categoryById[expense.categoryId]
            ExpenseListItem(
                expense = expense,
                categoryName = category?.name ?: "Uncategorized",
                categoryColorHex = category?.colorHex ?: "#9E9E9E",
            )
        }
        val total = if (expenses.isEmpty()) {
            null
        } else {
            Money.sum(expenses.map { it.amount }, expenses.first().amount.currency)
        }
        ExpensesListUiState(
            items = UiState.Content(items),
            categories = categoryList,
            selectedCategoryId = selectedId,
            total = total ?: Money.zero(CurrencyCode.USD),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpensesListUiState(),
    )

    fun onAction(action: ExpensesListAction) {
        when (action) {
            is ExpensesListAction.SelectCategoryFilter -> selectedCategoryId.update { action.categoryId }
            is ExpensesListAction.DeleteExpense -> viewModelScope.launch { deleteExpenseUseCase(action.expenseId) }
        }
    }
}
