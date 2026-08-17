package com.mac.expensee.feature.categories.domain.usecase

import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.feature.categories.domain.repository.CategoryRepository

class DeleteCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(id: String): AppResult<Unit> = repository.deleteCategory(id)
}
