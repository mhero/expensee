package com.mac.expensee.feature.categories.domain.usecase

import com.mac.expensee.feature.categories.domain.model.Category
import com.mac.expensee.feature.categories.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class ObserveCategoriesUseCase(private val repository: CategoryRepository) {
    operator fun invoke(): Flow<List<Category>> = repository.observeCategories()
}
