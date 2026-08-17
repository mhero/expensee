package com.mac.expensee.feature.expenses.presentation.addedit

import com.mac.expensee.feature.expenses.domain.model.ExpenseCategory

data class AddEditExpenseUiState(
    val expenseId: String? = null,
    val amountText: String = "",
    val description: String = "",
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),
    val categories: List<ExpenseCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val receiptPath: String? = null,
    val isLoadingExisting: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingReceipt: Boolean = false,
    val errorMessage: String? = null,
    val saveSucceeded: Boolean = false,
) {
    val isEditing: Boolean get() = expenseId != null
}

sealed interface AddEditExpenseAction {
    data class AmountChanged(val value: String) : AddEditExpenseAction
    data class DescriptionChanged(val value: String) : AddEditExpenseAction
    data class NotesChanged(val value: String) : AddEditExpenseAction
    data class DateChanged(val epochMillis: Long) : AddEditExpenseAction
    data class CategorySelected(val categoryId: String) : AddEditExpenseAction
    data class ReceiptPicked(val uri: android.net.Uri) : AddEditExpenseAction
    data object ReceiptRemoved : AddEditExpenseAction
    data object Save : AddEditExpenseAction
}
