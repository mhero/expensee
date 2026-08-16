package com.mac.expensee.feature.auth.domain.usecase

import com.mac.expensee.feature.auth.domain.repository.AuthRepository

class HasAccountUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): Boolean = authRepository.hasAccount()
}
