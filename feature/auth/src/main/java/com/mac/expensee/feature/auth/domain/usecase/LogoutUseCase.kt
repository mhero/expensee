package com.mac.expensee.feature.auth.domain.usecase

import com.mac.expensee.feature.auth.domain.repository.AuthRepository

class LogoutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke() = authRepository.logout()
}
