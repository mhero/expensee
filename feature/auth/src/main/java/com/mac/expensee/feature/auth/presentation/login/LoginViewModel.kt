package com.mac.expensee.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.common.result.DataError
import com.mac.expensee.feature.auth.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.UsernameChanged -> _state.update { it.copy(username = action.value, errorMessage = null) }
            is LoginAction.PasswordChanged -> _state.update { it.copy(password = action.value, errorMessage = null) }
            LoginAction.Submit -> submit()
            LoginAction.ErrorShown -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun submit() {
        val current = _state.value
        if (current.isSubmitting) return
        _state.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val passwordChars = current.password.toCharArray()
            val result = loginUseCase(current.username, passwordChars)
            passwordChars.fill('\u0000')
            when (result) {
                is AppResult.Success -> _state.update {
                    it.copy(isSubmitting = false, loginSucceeded = true, password = "")
                }
                is AppResult.Error -> _state.update {
                    it.copy(isSubmitting = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }
}

private fun DataError.toMessage(): String = when (this) {
    DataError.Auth.InvalidCredentials -> "Incorrect username or password."
    is DataError.Validation.InvalidField -> reason
    else -> "Something went wrong. Please try again."
}
