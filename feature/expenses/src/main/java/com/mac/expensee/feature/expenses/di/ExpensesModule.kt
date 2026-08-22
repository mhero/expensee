package com.mac.expensee.feature.expenses.di

import com.mac.expensee.feature.expenses.data.ExpenseCategoryLookupRepositoryImpl
import com.mac.expensee.feature.expenses.data.ExpenseRepositoryImpl
import com.mac.expensee.feature.expenses.data.ExpenseSyncGatewayImpl
import com.mac.expensee.feature.expenses.data.receipt.LocalReceiptStorage
import com.mac.expensee.feature.expenses.data.remote.RemoteExpenseDataSource
import com.mac.expensee.feature.expenses.data.remote.RetrofitRemoteExpenseDataSource
import com.mac.expensee.feature.expenses.domain.repository.ExpenseCategoryLookupRepository
import com.mac.expensee.feature.expenses.domain.repository.ExpenseRepository
import com.mac.expensee.feature.expenses.domain.repository.ExpenseSyncGateway
import com.mac.expensee.feature.expenses.domain.repository.ReceiptStorage
import com.mac.expensee.feature.expenses.domain.usecase.AddExpenseUseCase
import com.mac.expensee.feature.expenses.domain.usecase.DeleteExpenseUseCase
import com.mac.expensee.feature.expenses.domain.usecase.ObserveExpenseCategoriesUseCase
import com.mac.expensee.feature.expenses.domain.usecase.ObserveExpenseUseCase
import com.mac.expensee.feature.expenses.domain.usecase.ObserveExpensesUseCase
import com.mac.expensee.feature.expenses.domain.usecase.SaveReceiptUseCase
import com.mac.expensee.feature.expenses.domain.usecase.UpdateExpenseUseCase
import com.mac.expensee.feature.expenses.presentation.addedit.AddEditExpenseViewModel
import com.mac.expensee.feature.expenses.presentation.detail.ExpenseDetailViewModel
import com.mac.expensee.feature.expenses.presentation.list.ExpensesListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val expensesModule = module {
    single<ExpenseRepository> { ExpenseRepositoryImpl(expenseDao = get(), dispatcherProvider = get()) }
    single<ExpenseCategoryLookupRepository> { ExpenseCategoryLookupRepositoryImpl(categoryDao = get()) }
    single<ReceiptStorage> { LocalReceiptStorage(context = get(), dispatcherProvider = get()) }
    single<RemoteExpenseDataSource> { RetrofitRemoteExpenseDataSource(api = get()) }
    single<ExpenseSyncGateway> { ExpenseSyncGatewayImpl(expenseDao = get()) }

    factory { AddExpenseUseCase(get()) }
    factory { UpdateExpenseUseCase(get()) }
    factory { DeleteExpenseUseCase(get(), get()) }
    factory { ObserveExpensesUseCase(get()) }
    factory { ObserveExpenseUseCase(get()) }
    factory { ObserveExpenseCategoriesUseCase(get()) }
    factory { SaveReceiptUseCase(get()) }

    viewModel { ExpensesListViewModel(get(), get(), get()) }
    viewModel { (expenseId: String?) ->
        AddEditExpenseViewModel(
            existingExpenseId = expenseId,
            addExpenseUseCase = get(),
            updateExpenseUseCase = get(),
            observeExpenseUseCase = get(),
            observeExpenseCategoriesUseCase = get(),
            saveReceiptUseCase = get(),
        )
    }
    viewModel { (expenseId: String) ->
        ExpenseDetailViewModel(
            expenseId = expenseId,
            observeExpenseUseCase = get(),
            categoryLookupRepository = get(),
            deleteExpenseUseCase = get(),
        )
    }
}
