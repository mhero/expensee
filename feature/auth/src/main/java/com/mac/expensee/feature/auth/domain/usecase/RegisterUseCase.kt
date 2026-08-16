package com.mac.expensee.feature.auth.domain.usecase

import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.common.result.asError
import com.mac.expensee.feature.auth.domain.model.AuthSession
import com.mac.expensee.feature.auth.domain.repository.AuthRepository
import com.mac.expensee.feature.auth.domain.validation.CredentialValidator

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(username: String, password: CharArray): AppResult<AuthSession> {
        CredentialValidator.validateUsername(username)?.let { return it.asError() }
        CredentialValidator.validatePassword(password)?.let { return it.asError() }
        return authRepository.register(username.trim(), password)
    }
}
