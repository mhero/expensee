package com.mac.expensee.feature.auth.presentation.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.common.result.DataError
import com.mac.expensee.feature.auth.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SetupAccountViewModel(private val registerUseCase: RegisterUseCase) : ViewModel() {

    private val _state = MutableStateFlow(SetupAccountUiState())
    val state: StateFlow<SetupAccountUiState> = _state.asStateFlow()

    fun onAction(action: SetupAccountAction) {
        when (action) {
            is SetupAccountAction.UsernameChanged -> _state.update { it.copy(username = action.value, errorMessage = null) }
            is SetupAccountAction.PasswordChanged -> _state.update { it.copy(password = action.value, errorMessage = null) }
            is SetupAccountAction.ConfirmPasswordChanged ->
                _state.update { it.copy(confirmPassword = action.value, errorMessage = null) }
            SetupAccountAction.Submit -> submit()
        }
    }

    private fun submit() {
        val current = _state.value
        if (current.isSubmitting) return
        if (current.password != current.confirmPassword) {
            _state.update { it.copy(errorMessage = "Passwords don't match.") }
            return
        }
        _state.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val passwordChars = current.password.toCharArray()
            val result = registerUseCase(current.username, passwordChars)
            passwordChars.fill('\u0000')
            when (result) {
                is AppResult.Success -> _state.update {
                    it.copy(isSubmitting = false, setupSucceeded = true, password = "", confirmPassword = "")
                }
                is AppResult.Error -> _state.update {
                    it.copy(isSubmitting = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }
}

private fun DataError.toMessage(): String = when (this) {
    is DataError.Validation.InvalidField -> reason
    else -> "Something went wrong. Please try again."
}
