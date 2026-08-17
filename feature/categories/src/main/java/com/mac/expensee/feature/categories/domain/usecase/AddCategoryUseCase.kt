package com.mac.expensee.feature.categories.domain.usecase

import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.common.result.asError
import com.mac.expensee.feature.categories.domain.model.Category
import com.mac.expensee.feature.categories.domain.repository.CategoryRepository
import com.mac.expensee.feature.categories.domain.validation.CategoryValidator

class AddCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(name: String, colorHex: String, icon: String = "default"): AppResult<Category> {
        CategoryValidator.validateName(name)?.let { return it.asError() }
        return repository.addCategory(name.trim(), colorHex, icon)
    }
}
