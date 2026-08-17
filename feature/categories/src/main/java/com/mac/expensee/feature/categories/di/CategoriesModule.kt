package com.mac.expensee.feature.categories.di

import com.mac.expensee.feature.categories.data.CategoryRepositoryImpl
import com.mac.expensee.feature.categories.domain.repository.CategoryRepository
import com.mac.expensee.feature.categories.domain.usecase.AddCategoryUseCase
import com.mac.expensee.feature.categories.domain.usecase.DeleteCategoryUseCase
import com.mac.expensee.feature.categories.domain.usecase.ObserveCategoriesUseCase
import com.mac.expensee.feature.categories.domain.usecase.RenameCategoryUseCase
import com.mac.expensee.feature.categories.presentation.list.CategoriesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val categoriesModule = module {
    single<CategoryRepository> {
        CategoryRepositoryImpl(categoryDao = get(), expenseDao = get(), dispatcherProvider = get())
    }

    factory { ObserveCategoriesUseCase(get()) }
    factory { AddCategoryUseCase(get()) }
    factory { RenameCategoryUseCase(get()) }
    factory { DeleteCategoryUseCase(get()) }

    viewModel { CategoriesViewModel(get(), get(), get(), get()) }
}
