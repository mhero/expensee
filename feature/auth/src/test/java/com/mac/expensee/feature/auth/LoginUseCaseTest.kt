package com.mac.expensee.feature.auth

import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.common.result.DataError
import com.mac.expensee.feature.auth.domain.usecase.LoginUseCase
import com.mac.expensee.feature.auth.domain.usecase.RegisterUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LoginUseCaseTest {

    private val repository = FakeAuthRepository()
    private val registerUseCase = RegisterUseCase(repository)
    private val loginUseCase = LoginUseCase(repository)

    @Test
    fun `correct credentials succeed`() = runTest {
        registerUseCase("jane", "password1".toCharArray())
        val result = loginUseCase("jane", "password1".toCharArray())
        assertThat(result).isInstanceOf(AppResult.Success::class.java)
    }

    @Test
    fun `wrong password is rejected with InvalidCredentials`() = runTest {
        registerUseCase("jane", "password1".toCharArray())
        val result = loginUseCase("jane", "wrongpass".toCharArray())
        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat((result as AppResult.Error).error).isEqualTo(DataError.Auth.InvalidCredentials)
    }

    @Test
    fun `unknown username is rejected with InvalidCredentials, not a distinguishable error`() = runTest {
        val result = loginUseCase("ghost", "password1".toCharArray())
        assertThat((result as AppResult.Error).error).isEqualTo(DataError.Auth.InvalidCredentials)
    }
}
