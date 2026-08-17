package com.mac.expensee.feature.expenses.data.mapper

import com.mac.expensee.core.database.entity.CategoryEntity
import com.mac.expensee.feature.expenses.domain.model.ExpenseCategory

fun CategoryEntity.toExpenseCategory(): ExpenseCategory = ExpenseCategory(
    id = localId,
    name = name,
    colorHex = colorHex,
)
