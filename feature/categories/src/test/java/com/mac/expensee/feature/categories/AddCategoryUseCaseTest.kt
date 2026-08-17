package com.mac.expensee.feature.categories

import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.feature.categories.domain.usecase.AddCategoryUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AddCategoryUseCaseTest {

    private val repository = FakeCategoryRepository()
    private val useCase = AddCategoryUseCase(repository)

    @Test
    fun `valid name is added`() = runTest {
        val result = useCase("Travel", "#3949AB")
        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        assertThat(repository.currentCategories()).hasSize(1)
    }

    @Test
    fun `blank name is rejected before reaching the repository`() = runTest {
        val result = useCase("   ", "#3949AB")
        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat(repository.currentCategories()).isEmpty()
    }
}
