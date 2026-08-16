package com.mac.expensee.core.common.di

import com.mac.expensee.core.common.dispatcher.DefaultDispatcherProvider
import com.mac.expensee.core.common.dispatcher.DispatcherProvider
import org.koin.dsl.module

val commonModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
}
