package com.mac.expensee.feature.auth.domain.usecase

import com.mac.expensee.feature.auth.domain.model.AuthSession
import com.mac.expensee.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveSessionUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<AuthSession?> = authRepository.observeSession()
}
