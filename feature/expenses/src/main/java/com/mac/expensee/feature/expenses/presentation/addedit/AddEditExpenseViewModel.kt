package com.mac.expensee.feature.expenses.presentation.addedit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.common.money.Money
import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.common.result.DataError
import com.mac.expensee.feature.expenses.domain.model.Expense
import com.mac.expensee.feature.expenses.domain.usecase.AddExpenseUseCase
import com.mac.expensee.feature.expenses.domain.usecase.GetDefaultCurrencyUseCase
import com.mac.expensee.feature.expenses.domain.usecase.ObserveExpenseCategoriesUseCase
import com.mac.expensee.feature.expenses.domain.usecase.ObserveExpenseUseCase
import com.mac.expensee.feature.expenses.domain.usecase.SaveReceiptUseCase
import com.mac.expensee.feature.expenses.domain.usecase.UpdateExpenseUseCase
import java.math.BigDecimal
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Handles both create and edit: [existingExpenseId] is null for "add", set for "edit". A single
 * ViewModel for both avoids duplicating the form state/validation between two near-identical
 * screens; only [submit] branches on which use case to call.
 */
class AddEditExpenseViewModel(
    private val existingExpenseId: String?,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val observeExpenseUseCase: ObserveExpenseUseCase,
    private val observeExpenseCategoriesUseCase: ObserveExpenseCategoriesUseCase,
    private val saveReceiptUseCase: SaveReceiptUseCase,
    private val getDefaultCurrencyUseCase: GetDefaultCurrencyUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AddEditExpenseUiState(expenseId = existingExpenseId, isLoadingExisting = existingExpenseId != null),
    )
    val state: StateFlow<AddEditExpenseUiState> = _state.asStateFlow()

    init {
        observeExpenseCategoriesUseCase()
            .onEach { categories ->
                _state.update {
                    it.copy(
                        categories = categories,
                        selectedCategoryId = it.selectedCategoryId ?: categories.firstOrNull()?.id,
                    )
                }
            }
            .launchIn(viewModelScope)

        if (existingExpenseId != null) {
            // Editing: applyExisting sets currency from the expense's own stored amount, below.
            observeExpenseUseCase(existingExpenseId)
                .onEach { expense -> expense?.let { applyExisting(it) } }
                .launchIn(viewModelScope)
        } else {
            // Creating: default to whatever the user's currency setting is right now (see
            // AddEditExpenseUiState.currency's KDoc for why this is fetched once, not observed).
            viewModelScope.launch {
                val defaultCurrency = getDefaultCurrencyUseCase()
                _state.update { it.copy(currency = defaultCurrency) }
            }
        }
    }

    private fun applyExisting(expense: Expense) {
        _state.update {
            it.copy(
                isLoadingExisting = false,
                amountText = expense.amount.toMajorUnits().toPlainString(),
                currency = expense.amount.currency,
                description = expense.description,
                notes = expense.notes.orEmpty(),
                date = expense.date,
                selectedCategoryId = expense.categoryId,
                receiptPath = expense.receiptPath,
            )
        }
    }

    fun onAction(action: AddEditExpenseAction) {
        when (action) {
            is AddEditExpenseAction.AmountChanged -> _state.update { it.copy(amountText = action.value, errorMessage = null) }
            is AddEditExpenseAction.DescriptionChanged ->
                _state.update { it.copy(description = action.value, errorMessage = null) }
            is AddEditExpenseAction.NotesChanged -> _state.update { it.copy(notes = action.value) }
            is AddEditExpenseAction.DateChanged -> _state.update { it.copy(date = action.epochMillis) }
            is AddEditExpenseAction.CategorySelected -> _state.update { it.copy(selectedCategoryId = action.categoryId) }
            is AddEditExpenseAction.ReceiptPicked -> pickReceipt(action.uri)
            AddEditExpenseAction.ReceiptRemoved -> _state.update { it.copy(receiptPath = null) }
            AddEditExpenseAction.Save -> submit()
        }
    }

    private fun pickReceipt(uri: Uri) {
        _state.update { it.copy(isUploadingReceipt = true) }
        viewModelScope.launch {
            when (val result = saveReceiptUseCase(uri)) {
                is AppResult.Success -> _state.update { it.copy(isUploadingReceipt = false, receiptPath = result.data) }
                is AppResult.Error -> _state.update {
                    it.copy(isUploadingReceipt = false, errorMessage = "Couldn't save the receipt image.")
                }
            }
        }
    }

    private fun submit() {
        val current = _state.value
        if (current.isSaving) return

        val amount = parseAmount(current.amountText, current.currency)
        val categoryId = current.selectedCategoryId
        if (amount == null) {
            _state.update { it.copy(errorMessage = "Enter a valid amount") }
            return
        }
        if (categoryId == null) {
            _state.update { it.copy(errorMessage = "Choose a category") }
            return
        }

        val expense = Expense(
            id = current.expenseId ?: UUID.randomUUID().toString(),
            categoryId = categoryId,
            amount = amount,
            description = current.description.trim(),
            notes = current.notes.trim().ifBlank { null },
            date = current.date,
            receiptPath = current.receiptPath,
        )

        _state.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (current.isEditing) updateExpenseUseCase(expense) else addExpenseUseCase(expense)
            when (result) {
                is AppResult.Success -> _state.update { it.copy(isSaving = false, saveSucceeded = true) }
                is AppResult.Error -> _state.update {
                    it.copy(isSaving = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    private fun parseAmount(text: String, currency: CurrencyCode): Money? {
        val decimal = text.trim().toBigDecimalOrNull() ?: return null
        if (decimal <= BigDecimal.ZERO) return null
        return runCatching { Money.fromMajorUnits(decimal, currency) }.getOrNull()
    }
}

private fun DataError.toMessage(): String = when (this) {
    is DataError.Validation.InvalidField -> reason
    DataError.Local.NotFound -> "This expense no longer exists."
    else -> "Something went wrong. Please try again."
}
