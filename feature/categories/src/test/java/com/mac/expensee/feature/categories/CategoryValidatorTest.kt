package com.mac.expensee.feature.categories

import com.google.common.truth.Truth.assertThat
import com.mac.expensee.feature.categories.domain.validation.CategoryValidator
import org.junit.Test

class CategoryValidatorTest {
    @Test
    fun `blank name is invalid`() {
        assertThat(CategoryValidator.validateName("")).isNotNull()
        assertThat(CategoryValidator.validateName("   ")).isNotNull()
    }

    @Test
    fun `overly long name is invalid`() {
        assertThat(CategoryValidator.validateName("x".repeat(50))).isNotNull()
    }

    @Test
    fun `reasonable name is valid`() {
        assertThat(CategoryValidator.validateName("Groceries")).isNull()
    }
}
