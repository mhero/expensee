package com.mac.expensee.feature.categories.data.mapper

import com.mac.expensee.core.database.entity.CategoryEntity
import com.mac.expensee.feature.categories.domain.model.Category

fun CategoryEntity.toDomain(): Category = Category(
    id = localId,
    name = name,
    colorHex = colorHex,
    icon = icon,
    isDefault = isDefault,
)
