package com.mac.expensee.feature.categories.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.common.result.DataError
import com.mac.expensee.core.ui.components.UiState
import com.mac.expensee.feature.categories.domain.usecase.AddCategoryUseCase
import com.mac.expensee.feature.categories.domain.usecase.DeleteCategoryUseCase
import com.mac.expensee.feature.categories.domain.usecase.ObserveCategoriesUseCase
import com.mac.expensee.feature.categories.domain.usecase.RenameCategoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoriesViewModel(
    observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val renameCategoryUseCase: RenameCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
) : ViewModel() {

    private val transientState = MutableStateFlow(TransientState())

    val state: StateFlow<CategoriesUiState> = combine(
        observeCategoriesUseCase(),
        transientState,
    ) { categories, transient ->
        CategoriesUiState(
            categories = UiState.Content(categories),
            isSubmitting = transient.isSubmitting,
            errorMessage = transient.errorMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoriesUiState())

    fun onAction(action: CategoriesAction) {
        when (action) {
            is CategoriesAction.AddCategory -> submit { addCategoryUseCase(action.name, action.colorHex) }
            is CategoriesAction.RenameCategory -> submit { renameCategoryUseCase(action.id, action.newName) }
            is CategoriesAction.DeleteCategory -> submit { deleteCategoryUseCase(action.id) }
            CategoriesAction.ErrorShown -> transientState.update { it.copy(errorMessage = null) }
        }
    }

    private fun submit(action: suspend () -> AppResult<*>) {
        transientState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = action()) {
                is AppResult.Success -> transientState.update { it.copy(isSubmitting = false) }
                is AppResult.Error -> transientState.update {
                    it.copy(isSubmitting = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    private data class TransientState(val isSubmitting: Boolean = false, val errorMessage: String? = null)
}

private fun DataError.toMessage(): String = when (this) {
    is DataError.Validation.InvalidField -> reason
    DataError.Local.NotFound -> "This category no longer exists."
    else -> "Something went wrong. Please try again."
}
