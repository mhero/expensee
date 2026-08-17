package com.mac.expensee.feature.categories.domain.usecase

import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.common.result.asError
import com.mac.expensee.feature.categories.domain.model.Category
import com.mac.expensee.feature.categories.domain.repository.CategoryRepository
import com.mac.expensee.feature.categories.domain.validation.CategoryValidator

class RenameCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(id: String, newName: String): AppResult<Category> {
        CategoryValidator.validateName(newName)?.let { return it.asError() }
        return repository.renameCategory(id, newName.trim())
    }
}
