package com.mac.expensee.core.testing

import com.mac.expensee.core.common.dispatcher.DispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/** A [DispatcherProvider] that routes every dispatcher to a single test dispatcher. */
@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherProvider(
    dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : DispatcherProvider {
    override val main = dispatcher
    override val io = dispatcher
    override val default = dispatcher
    override val unconfined = dispatcher
}
