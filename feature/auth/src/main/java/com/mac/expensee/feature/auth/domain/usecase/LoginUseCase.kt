package com.mac.expensee.feature.auth.domain.usecase

import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.common.result.asError
import com.mac.expensee.feature.auth.domain.model.AuthSession
import com.mac.expensee.feature.auth.domain.repository.AuthRepository
import com.mac.expensee.feature.auth.domain.validation.CredentialValidator

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(username: String, password: CharArray): AppResult<AuthSession> {
        CredentialValidator.validateUsername(username)?.let { return it.asError() }
        if (password.isEmpty()) {
            return CredentialValidator.validatePassword(password)!!.asError()
        }
        return authRepository.login(username.trim(), password)
    }
}
