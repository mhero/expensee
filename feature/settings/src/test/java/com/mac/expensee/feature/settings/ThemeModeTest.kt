package com.mac.expensee.feature.settings

import com.google.common.truth.Truth.assertThat
import com.mac.expensee.feature.settings.domain.model.ThemeMode
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `parses a valid stored name`() {
        assertThat(ThemeMode.fromStorageValue("DARK")).isEqualTo(ThemeMode.DARK)
        assertThat(ThemeMode.fromStorageValue("LIGHT")).isEqualTo(ThemeMode.LIGHT)
    }

    @Test
    fun `falls back to SYSTEM for null or unrecognized values`() {
        assertThat(ThemeMode.fromStorageValue(null)).isEqualTo(ThemeMode.SYSTEM)
        assertThat(ThemeMode.fromStorageValue("not-a-theme")).isEqualTo(ThemeMode.SYSTEM)
    }
}
