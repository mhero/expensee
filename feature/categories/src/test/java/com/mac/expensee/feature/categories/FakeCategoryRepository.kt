package com.mac.expensee.feature.categories

import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.common.result.DataError
import com.mac.expensee.core.common.result.asError
import com.mac.expensee.core.common.result.asSuccess
import com.mac.expensee.feature.categories.domain.model.Category
import com.mac.expensee.feature.categories.domain.repository.CategoryRepository
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCategoryRepository : CategoryRepository {
    private val categories = MutableStateFlow<Map<String, Category>>(emptyMap())
    var referencedIds: Set<String> = emptySet()

    override fun observeCategories() = categories.map { it.values.toList() }

    override suspend fun addCategory(name: String, colorHex: String, icon: String): AppResult<Category> {
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = name,
            colorHex = colorHex,
            icon = icon,
            isDefault = false,
        )
        categories.value = categories.value + (category.id to category)
        return category.asSuccess()
    }

    override suspend fun renameCategory(id: String, newName: String): AppResult<Category> {
        val existing = categories.value[id] ?: return DataError.Local.NotFound.asError()
        val updated = existing.copy(name = newName)
        categories.value = categories.value + (id to updated)
        return updated.asSuccess()
    }

    override suspend fun deleteCategory(id: String): AppResult<Unit> {
        if (!categories.value.containsKey(id)) return DataError.Local.NotFound.asError()
        if (id in referencedIds) {
            return DataError.Validation.InvalidField("category", "still referenced").asError()
        }
        categories.value = categories.value - id
        return Unit.asSuccess()
    }

    override suspend fun isReferencedByExpenses(id: String): Boolean = id in referencedIds

    fun currentCategories(): List<Category> = categories.value.values.toList()

    fun seed(category: Category) {
        categories.value = categories.value + (category.id to category)
    }
}
