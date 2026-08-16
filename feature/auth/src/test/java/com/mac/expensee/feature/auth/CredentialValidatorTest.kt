package com.mac.expensee.feature.auth

import com.google.common.truth.Truth.assertThat
import com.mac.expensee.feature.auth.domain.validation.CredentialValidator
import org.junit.Test

class CredentialValidatorTest {

    @Test
    fun `blank username is invalid`() {
        assertThat(CredentialValidator.validateUsername("")).isNotNull()
        assertThat(CredentialValidator.validateUsername("   ")).isNotNull()
    }

    @Test
    fun `username shorter than 3 chars is invalid`() {
        assertThat(CredentialValidator.validateUsername("ab")).isNotNull()
    }

    @Test
    fun `valid username passes`() {
        assertThat(CredentialValidator.validateUsername("abc")).isNull()
    }

    @Test
    fun `password shorter than 6 chars is invalid`() {
        assertThat(CredentialValidator.validatePassword("12345".toCharArray())).isNotNull()
    }

    @Test
    fun `password of 6 or more chars passes`() {
        assertThat(CredentialValidator.validatePassword("123456".toCharArray())).isNull()
    }
}
