package com.mac.expensee.feature.auth.presentation.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

/**
 * Drives [LoginScreen] directly with a fixed [LoginUiState] rather than going through
 * [LoginRoute]/`koinViewModel()` -- a stateless composable taking `(state, onAction)` is testable
 * without standing up Koin or a real [LoginViewModel], mirroring how [LoginViewModel] itself is
 * unit-tested against a fake `AuthRepository` (see `feature:auth`'s test source set).
 */
class LoginScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        state: LoginUiState = LoginUiState(),
        onAction: (LoginAction) -> Unit = {},
        onNavigateToSetup: () -> Unit = {},
    ) {
        composeRule.setContent {
            LoginScreen(state = state, onAction = onAction, onNavigateToSetup = onNavigateToSetup)
        }
    }

    @Test
    fun `typing a username reports UsernameChanged with the full field value`() {
        val actions = mutableListOf<LoginAction>()
        setContent(onAction = actions::add)

        composeRule.onNodeWithText("Username").performTextInput("alice")

        // OutlinedTextField reports the full new value on each keystroke, not a delta -- since the
        // test types into an initially-empty field in one call, the last (and only) reported value
        // is the whole string.
        assertThat(actions.filterIsInstance<LoginAction.UsernameChanged>().last().value).isEqualTo("alice")
    }

    @Test
    fun `an error message is shown when present in state`() {
        setContent(state = LoginUiState(errorMessage = "Incorrect username or password"))

        composeRule.onNodeWithText("Incorrect username or password").assertIsDisplayed()
    }

    @Test
    fun `the screen renders its sign in button when there is no error`() {
        setContent(state = LoginUiState(errorMessage = null))

        composeRule.onNodeWithText("Sign in").assertIsDisplayed()
    }

    @Test
    fun `the sign in button is disabled while submitting`() {
        setContent(state = LoginUiState(isSubmitting = true))

        composeRule.onNodeWithText("Sign in").assertIsNotEnabled()
    }

    @Test
    fun `tapping sign in reports a Submit action`() {
        val actions = mutableListOf<LoginAction>()
        setContent(
            state = LoginUiState(username = "alice", password = "hunter2"),
            onAction = actions::add,
        )

        composeRule.onNodeWithText("Sign in").performClick()

        assertThat(actions).contains(LoginAction.Submit)
    }

    @Test
    fun `tapping the create-account link invokes onNavigateToSetup, not onAction`() {
        val actions = mutableListOf<LoginAction>()
        var navigatedToSetup = false
        setContent(onAction = actions::add, onNavigateToSetup = { navigatedToSetup = true })

        composeRule.onNodeWithText("Don't have an account? Create one").performClick()

        assertThat(navigatedToSetup).isTrue()
        assertThat(actions).isEmpty()
    }
}
