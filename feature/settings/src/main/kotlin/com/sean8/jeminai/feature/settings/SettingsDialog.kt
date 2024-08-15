package com.sean8.jeminai.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean8.jeminai.feature.settings.R.string
import com.sean8.jeminai.feature.settings.SettingsUiState.Loading
import com.sean8.jeminai.feature.settings.SettingsUiState.Success
import com.sean8.jeminai.feature.settings.model.data.DarkThemeConfig
import com.sean8.jeminai.feature.settings.model.data.DarkThemeConfig.DARK
import com.sean8.jeminai.feature.settings.model.data.DarkThemeConfig.FOLLOW_SYSTEM
import com.sean8.jeminai.feature.settings.model.data.DarkThemeConfig.LIGHT

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settingsUiState by viewModel.settingsUiState.collectAsStateWithLifecycle()
    SettingsDialog(
        onDismiss = onDismiss,
        settingsUiState = settingsUiState,
        onChangeDarkThemeConfig = viewModel::updateDarkThemeConfig,
    )
}

@Composable
fun SettingsDialog(
    settingsUiState: SettingsUiState,
    onDismiss: () -> Unit,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
) {
    val configuration = LocalConfiguration.current

    AlertDialog(
        properties = DialogProperties(),
        modifier = Modifier.widthIn(max = configuration.screenWidthDp.dp - 80.dp),
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = stringResource(string.feature_settings_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            HorizontalDivider()
            Column(Modifier.verticalScroll(rememberScrollState())) {
                when (settingsUiState) {
                    Loading -> {
                        Text(
                            text = stringResource(string.feature_settings_loading),
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    }

                    is Success -> {
                        SettingsPanel(
                            settings = settingsUiState.settings,
                            onChangeDarkThemeConfig = onChangeDarkThemeConfig,
                        )
                    }
                }
                HorizontalDivider(Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = {
            Text(
                text = stringResource(string.feature_settings_dismiss_dialog_button_text),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable { onDismiss() },
            )
        },
    )
}

@Composable
private fun SettingsPanel(
    settings: UserEditableSettings,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
) {
    SettingsDialogSectionTitle(text = stringResource(string.feature_settings_dark_mode_preference))
    Column(Modifier.selectableGroup()) {
        SettingsDialogThemeChooserRow(
            text = stringResource(string.feature_settings_dark_mode_config_system_default),
            selected = settings.darkThemeConfig == FOLLOW_SYSTEM,
            onClick = { onChangeDarkThemeConfig(FOLLOW_SYSTEM) },
        )
        SettingsDialogThemeChooserRow(
            text = stringResource(string.feature_settings_dark_mode_config_light),
            selected = settings.darkThemeConfig == LIGHT,
            onClick = { onChangeDarkThemeConfig(LIGHT) },
        )
        SettingsDialogThemeChooserRow(
            text = stringResource(string.feature_settings_dark_mode_config_dark),
            selected = settings.darkThemeConfig == DARK,
            onClick = { onChangeDarkThemeConfig(DARK) },
        )
    }
}

@Composable
private fun SettingsDialogSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
fun SettingsDialogThemeChooserRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@Preview
@Composable
private fun PreviewSettingsDialog() {
    MaterialTheme {
        SettingsDialog(
            onDismiss = {},
            settingsUiState = Success(
                UserEditableSettings(
                    darkThemeConfig = FOLLOW_SYSTEM,
                ),
            ),
            onChangeDarkThemeConfig = {},
        )
    }
}

@Preview
@Composable
private fun PreviewSettingsDialogLoading() {
    SettingsDialog(
        onDismiss = {},
        settingsUiState = Loading,
        onChangeDarkThemeConfig = {},
    )
}