package com.mac.expensee.feature.categories.data

import com.mac.expensee.core.common.dispatcher.DispatcherProvider
import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.common.result.DataError
import com.mac.expensee.core.common.result.asError
import com.mac.expensee.core.common.result.asSuccess
import com.mac.expensee.core.common.sync.SyncStatus
import com.mac.expensee.core.database.dao.CategoryDao
import com.mac.expensee.core.database.dao.ExpenseDao
import com.mac.expensee.core.database.entity.CategoryEntity
import com.mac.expensee.feature.categories.data.mapper.toDomain
import com.mac.expensee.feature.categories.domain.model.Category
import com.mac.expensee.feature.categories.domain.repository.CategoryRepository
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * [deleteCategory] is the interesting method here: it checks [ExpenseDao.countByCategory] before
 * deleting, so a category that's still referenced fails with a [DataError.Validation.InvalidField]
 * rather than silently orphaning expenses or cascading. This mirrors `ExpenseEntity`'s foreign key
 * (`onDelete = RESTRICT`) at the domain-error level, so the UI can show a message instead of the
 * app crashing on a constraint violation.
 */
class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val dispatcherProvider: DispatcherProvider,
) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addCategory(name: String, colorHex: String, icon: String): AppResult<Category> =
        withContext(dispatcherProvider.io) {
            val now = System.currentTimeMillis()
            val entity = CategoryEntity(
                localId = UUID.randomUUID().toString(),
                remoteId = null,
                name = name,
                colorHex = colorHex,
                icon = icon,
                isDefault = false,
                createdAt = now,
                updatedAt = now,
                syncStatus = SyncStatus.PENDING_UPLOAD,
                deletedAt = null,
                version = 1,
            )
            categoryDao.upsert(entity)
            entity.toDomain().asSuccess()
        }

    override suspend fun renameCategory(id: String, newName: String): AppResult<Category> =
        withContext(dispatcherProvider.io) {
            val existing = categoryDao.getById(id) ?: return@withContext DataError.Local.NotFound.asError()
            val updated = existing.copy(
                name = newName,
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING_UPLOAD,
                version = existing.version + 1,
            )
            categoryDao.update(updated)
            updated.toDomain().asSuccess()
        }

    override suspend fun deleteCategory(id: String): AppResult<Unit> = withContext(dispatcherProvider.io) {
        if (categoryDao.getById(id) == null) return@withContext DataError.Local.NotFound.asError()
        if (expenseDao.countByCategory(id) > 0) {
            return@withContext DataError.Validation.InvalidField(
                field = "category",
                reason = "This category is still used by one or more expenses. Move or delete those first.",
            ).asError()
        }
        // Soft-delete (tombstone), not a hard delete: matches ExpenseEntity's sync-ready design --
        // a category that was already pushed to a (future) server still needs its deletion
        // communicated, so it can't just vanish locally. SyncManager/CleanupWorker (see
        // project README's "Sync model") purge the tombstone once that's confirmed.
        categoryDao.softDelete(localId = id, deletedAt = System.currentTimeMillis())
        Unit.asSuccess()
    }

    override suspend fun isReferencedByExpenses(id: String): Boolean = withContext(dispatcherProvider.io) {
        expenseDao.countByCategory(id) > 0
    }
}
