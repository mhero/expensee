package com.mac.expensee.feature.categories.domain.repository

import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.feature.categories.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>

    suspend fun addCategory(name: String, colorHex: String, icon: String): AppResult<Category>

    suspend fun renameCategory(id: String, newName: String): AppResult<Category>

    /**
     * Fails with [com.mac.expensee.core.common.result.DataError.Validation.InvalidField] if any
     * expense still references this category -- see project README "Categories" requirement:
     * deletion must be reference-safe, not a silent cascade.
     */
    suspend fun deleteCategory(id: String): AppResult<Unit>

    suspend fun isReferencedByExpenses(id: String): Boolean
}
