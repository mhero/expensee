package com.mac.expensee.core.testing

import app.cash.turbine.ReceiveTurbine
import kotlinx.coroutines.flow.Flow
import app.cash.turbine.test

/** Convenience wrapper so call sites read `flow.testFirst { ... }` instead of importing Turbine directly. */
suspend fun <T> Flow<T>.testFirst(assertions: suspend ReceiveTurbine<T>.(T) -> Unit) {
    test {
        val item = awaitItem()
        assertions(item)
        cancelAndIgnoreRemainingEvents()
    }
}
