package com.sean8.jeminai.feature.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sean8.jeminai.core.model.DarkThemeConfig.DARK
import com.sean8.jeminai.feature.settings.SettingsUiState.Loading
import com.sean8.jeminai.feature.settings.SettingsUiState.Success
import org.junit.Rule
import org.junit.Test

class SettingsDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun getString(id: Int) = composeTestRule.activity.resources.getString(id)

    @Test
    fun whenLoading_showsLoadingText() {
        composeTestRule.setContent {
            SettingsDialog(
                settingsUiState = Loading,
                onDismiss = {},
                onChangeDarkThemeConfig = {},
            )
        }

        composeTestRule
            .onNodeWithText(getString(R.string.feature_settings_loading))
            .assertExists()
    }

    @Test
    fun whenStateIsSuccess_allDefaultSettingsAreDisplayed() {
        composeTestRule.setContent {
            SettingsDialog(
                settingsUiState = Success(
                    UserEditableSettings(
                        darkThemeConfig = DARK,
                    ),
                ),
                onDismiss = { },
                onChangeDarkThemeConfig = {},
            )
        }

        // Check that all the possible settings are displayed.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dark_mode_config_system_default))
            .assertExists()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dark_mode_config_light))
            .assertExists()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dark_mode_config_dark))
            .assertExists()

        // Check that the correct settings are selected.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dark_mode_config_dark))
            .assertIsSelected()
    }

    @Test
    fun whenDialogIsDismissed_onDismissIsCalled() {
        var dismissed = false
        composeTestRule.setContent {
            SettingsDialog(
                onDismiss = {
                    dismissed = true
                },
                settingsUiState = Success(
                        UserEditableSettings(
                            darkThemeConfig = DARK,
                        ),
                    ),
                onChangeDarkThemeConfig = { }
            )
        }

        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dismiss_dialog_button_text))
            .performClick()

        assert(dismissed)
    }
}
