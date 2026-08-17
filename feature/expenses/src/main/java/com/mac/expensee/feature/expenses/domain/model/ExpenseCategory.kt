package com.mac.expensee.feature.expenses.domain.model

/**
 * Read-only, minimal view of a category, scoped to what `feature:expenses` needs for selection
 * and display (picker list, colored chip on a list row). `feature:categories` owns the full CRUD
 * domain model separately -- both map from the same `core:database` `CategoryEntity` via their
 * own mapper, so neither feature module depends on the other.
 */
data class ExpenseCategory(
    val id: String,
    val name: String,
    val colorHex: String,
)
