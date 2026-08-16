package com.mac.expensee.core.ui.components

/** Generic loading/content/error wrapper reused across list-style screens' UI state. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Content<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
