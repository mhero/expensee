package com.mac.expensee.feature.auth.domain.validation

import com.mac.expensee.core.common.result.DataError

private const val MIN_USERNAME_LENGTH = 3
private const val MIN_PASSWORD_LENGTH = 6

/** Pure validation rules, kept independent of Room/DataStore/Compose so they're trivial to unit test. */
object CredentialValidator {

    fun validateUsername(username: String): DataError.Validation.InvalidField? = when {
        username.isBlank() -> DataError.Validation.InvalidField("username", "Username is required")
        username.trim().length < MIN_USERNAME_LENGTH ->
            DataError.Validation.InvalidField("username", "Username must be at least $MIN_USERNAME_LENGTH characters")
        else -> null
    }

    fun validatePassword(password: CharArray): DataError.Validation.InvalidField? = when {
        password.isEmpty() -> DataError.Validation.InvalidField("password", "Password is required")
        password.size < MIN_PASSWORD_LENGTH ->
            DataError.Validation.InvalidField("password", "Password must be at least $MIN_PASSWORD_LENGTH characters")
        else -> null
    }
}
