package com.mac.expensee.core.datastore.di

import com.mac.expensee.core.datastore.UserPreferencesDataSource
import org.koin.dsl.module

val dataStoreModule = module {
    single { UserPreferencesDataSource(context = get()) }
}
