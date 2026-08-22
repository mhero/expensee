package com.mac.expensee.feature.settings.data

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.datastore.UserPreferencesDataSource
import com.mac.expensee.core.testing.testFirst
import com.mac.expensee.feature.settings.domain.model.ThemeMode
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Exercises [SettingsRepositoryImpl] against a real, on-disk `UserPreferencesDataSource` --
 * `SettingsViewModelTest` already covers the ViewModel against a fake `SettingsRepository`, but
 * nothing previously verified this class's own DataStore key mapping and default-value fallbacks.
 * Robolectric is required because `androidx.datastore.preferences` needs a real [android.content.Context].
 */
@RunWith(RobolectricTestRunner::class)
class SettingsRepositoryImplTest {

    private lateinit var repository: SettingsRepositoryImpl
    private lateinit var biometricUnlockRepository: FakeBiometricUnlockRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        biometricUnlockRepository = FakeBiometricUnlockRepository()
        repository = SettingsRepositoryImpl(
            preferencesDataSource = UserPreferencesDataSource(context),
            biometricUnlockRepository = biometricUnlockRepository,
        )
    }

    @Test
    fun `defaults are USD, SYSTEM theme, notifications off, biometric off`() = runTest {
        repository.observeSettings().testFirst { settings ->
            assertThat(settings.currency).isEqualTo(CurrencyCode.USD)
            assertThat(settings.theme).isEqualTo(ThemeMode.SYSTEM)
            assertThat(settings.notificationsEnabled).isFalse()
            assertThat(settings.biometricUnlockEnabled).isFalse()
        }
    }

    @Test
    fun `setCurrency persists and is reflected on the next read`() = runTest {
        repository.setCurrency(CurrencyCode.EUR)

        repository.observeSettings().testFirst { settings ->
            assertThat(settings.currency).isEqualTo(CurrencyCode.EUR)
        }
    }

    @Test
    fun `setTheme persists and is reflected on the next read`() = runTest {
        repository.setTheme(ThemeMode.DARK)

        repository.observeSettings().testFirst { settings ->
            assertThat(settings.theme).isEqualTo(ThemeMode.DARK)
        }
    }

    @Test
    fun `setNotificationsEnabled persists and is reflected on the next read`() = runTest {
        repository.setNotificationsEnabled(true)

        repository.observeSettings().testFirst { settings ->
            assertThat(settings.notificationsEnabled).isTrue()
        }
    }

    @Test
    fun `setBiometricUnlockEnabled delegates to BiometricUnlockRepository rather than DataStore`() = runTest {
        repository.setBiometricUnlockEnabled(true)

        assertThat(biometricUnlockRepository.observeBiometricUnlockEnabled().value).isTrue()
        repository.observeSettings().testFirst { settings ->
            assertThat(settings.biometricUnlockEnabled).isTrue()
        }
    }

    @Test
    fun `an unrecognized stored theme value falls back to SYSTEM`() = runTest {
        // Simulates a stored value from a future/removed enum constant -- ThemeMode.fromStorageValue
        // is expected to fail safe rather than crash on valueOf().
        UserPreferencesDataSource(ApplicationProvider.getApplicationContext()).setThemeMode("NOT_A_REAL_MODE")

        repository.observeSettings().testFirst { settings ->
            assertThat(settings.theme).isEqualTo(ThemeMode.SYSTEM)
        }
    }
}
