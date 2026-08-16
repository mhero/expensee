package com.mac.expensee.core.database.di

import com.mac.expensee.core.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val databaseModule = module {
    single { CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.IO) }
    single { AppDatabase.build(context = get(), applicationScope = get()) }
    single { get<AppDatabase>().expenseDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().userDao() }
}
