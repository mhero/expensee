package com.mac.expensee.feature.categories.presentation.list

import com.mac.expensee.core.ui.components.UiState
import com.mac.expensee.feature.categories.domain.model.Category

data class CategoriesUiState(
    val categories: UiState<List<Category>> = UiState.Loading,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface CategoriesAction {
    data class AddCategory(val name: String, val colorHex: String) : CategoriesAction
    data class RenameCategory(val id: String, val newName: String) : CategoriesAction
    data class DeleteCategory(val id: String) : CategoriesAction
    data object ErrorShown : CategoriesAction
}
