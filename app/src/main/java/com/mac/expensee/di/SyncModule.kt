package com.mac.expensee.di

import com.mac.expensee.sync.CleanupScheduler
import com.mac.expensee.sync.CleanupWorker
import com.mac.expensee.sync.SyncManager
import com.mac.expensee.sync.SyncScheduler
import com.mac.expensee.sync.SyncWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val syncModule = module {
    single { SyncManager(apiConfig = get(), expenseSyncGateway = get(), remoteExpenseDataSource = get(), userPreferencesDataSource = get()) }
    single { SyncScheduler(context = get()) }
    single { CleanupScheduler(context = get()) }

    worker { params ->
        SyncWorker(context = params.get(), workerParams = params.get(), syncManager = get())
    }
    worker { params ->
        CleanupWorker(context = params.get(), workerParams = params.get(), expenseDao = get(), categoryDao = get())
    }
}
