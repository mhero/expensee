package com.mac.expensee.feature.auth.presentation.setup

data class SetupAccountUiState(
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val setupSucceeded: Boolean = false,
)

sealed interface SetupAccountAction {
    data class UsernameChanged(val value: String) : SetupAccountAction
    data class PasswordChanged(val value: String) : SetupAccountAction
    data class ConfirmPasswordChanged(val value: String) : SetupAccountAction
    data object Submit : SetupAccountAction
}
