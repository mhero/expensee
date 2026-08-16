package com.mac.expensee.feature.auth

import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.feature.auth.domain.usecase.RegisterUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RegisterUseCaseTest {

    private val repository = FakeAuthRepository()
    private val useCase = RegisterUseCase(repository)

    @Test
    fun `rejects blank username before touching the repository`() = runTest {
        val result = useCase("", "password1".toCharArray())
        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat(repository.hasAccount()).isFalse()
    }

    @Test
    fun `rejects short password`() = runTest {
        val result = useCase("john", "123".toCharArray())
        assertThat(result).isInstanceOf(AppResult.Error::class.java)
    }

    @Test
    fun `valid credentials create a session`() = runTest {
        val result = useCase("john", "password1".toCharArray())
        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val session = (result as AppResult.Success).data
        assertThat(session.user.username).isEqualTo("john")
    }

    @Test
    fun `duplicate username is rejected by the repository layer`() = runTest {
        useCase("john", "password1".toCharArray())
        val second = useCase("john", "password2".toCharArray())
        assertThat(second).isInstanceOf(AppResult.Error::class.java)
    }
}
