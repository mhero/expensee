package com.mac.expensee.core.common.result

/**
 * A minimal Result type carrying a typed error instead of a raw [Throwable]. Deliberately
 * separate from [kotlin.Result], which is designed around exceptions rather than domain errors.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: DataError) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Error -> this
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(data)
    return this
}

inline fun <T> AppResult<T>.onError(action: (DataError) -> Unit): AppResult<T> {
    if (this is AppResult.Error) action(error)
    return this
}

fun <T> T.asSuccess(): AppResult<T> = AppResult.Success(this)
fun DataError.asError(): AppResult<Nothing> = AppResult.Error(this)
