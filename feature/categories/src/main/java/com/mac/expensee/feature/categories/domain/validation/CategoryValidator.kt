package com.mac.expensee.feature.categories.domain.validation

import com.mac.expensee.core.common.result.DataError

private const val MAX_NAME_LENGTH = 40

object CategoryValidator {
    fun validateName(name: String): DataError.Validation.InvalidField? = when {
        name.isBlank() -> DataError.Validation.InvalidField("name", "Category name is required")
        name.trim().length > MAX_NAME_LENGTH -> DataError.Validation.InvalidField("name", "Name is too long")
        else -> null
    }
}
