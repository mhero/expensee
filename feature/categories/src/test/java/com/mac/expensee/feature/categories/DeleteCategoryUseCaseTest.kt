package com.mac.expensee.feature.categories

import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.feature.categories.domain.model.Category
import com.mac.expensee.feature.categories.domain.usecase.DeleteCategoryUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteCategoryUseCaseTest {

    private val repository = FakeCategoryRepository()
    private val useCase = DeleteCategoryUseCase(repository)

    @Test
    fun `deleting an unreferenced category succeeds`() = runTest {
        repository.seed(Category("cat-1", "Food", "#EF6C00", "default", false))

        val result = useCase("cat-1")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        assertThat(repository.currentCategories()).isEmpty()
    }

    @Test
    fun `deleting a category still referenced by expenses fails and does not delete it`() = runTest {
        repository.seed(Category("cat-1", "Food", "#EF6C00", "default", false))
        repository.referencedIds = setOf("cat-1")

        val result = useCase("cat-1")

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat(repository.currentCategories()).hasSize(1)
    }

    @Test
    fun `deleting a nonexistent category fails`() = runTest {
        val result = useCase("ghost")
        assertThat(result).isInstanceOf(AppResult.Error::class.java)
    }
}
