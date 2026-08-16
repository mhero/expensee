package com.mac.expensee.core.common.result

/**
 * Root of every error the data/domain layers can surface to the presentation layer.
 * Kept as a sealed hierarchy (rather than raw exceptions) so ViewModels can exhaustively
 * `when`-match on failure causes and show an appropriate message, without depending on
 * Room/Retrofit exception types.
 */
sealed interface DataError {

    sealed interface Local : DataError {
        data object DiskFull : Local
        data object NotFound : Local
        data class Unknown(val message: String?) : Local
    }

    sealed interface Remote : DataError {
        data object NoConnection : Remote
        data object Timeout : Remote
        data object Unauthorized : Remote
        data class Server(val code: Int) : Remote
        data class Unknown(val message: String?) : Remote
    }

    sealed interface Validation : DataError {
        data class InvalidField(val field: String, val reason: String) : Validation
    }

    sealed interface Auth : DataError {
        data object InvalidCredentials : Auth
        data object SessionExpired : Auth
        data object BiometricUnavailable : Auth
        data object BiometricFailed : Auth
    }
}
