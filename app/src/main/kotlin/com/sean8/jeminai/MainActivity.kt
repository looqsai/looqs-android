package ai.looqs.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ai.looqs.android.MainActivityUiState.Loading
import ai.looqs.android.MainActivityUiState.Success
import ai.looqs.android.core.model.DarkThemeConfig.DARK
import ai.looqs.android.core.model.DarkThemeConfig.FOLLOW_SYSTEM
import ai.looqs.android.core.model.DarkThemeConfig.LIGHT
import ai.looqs.android.feature.setup.SetupScreen
import ai.looqs.android.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var uiState: MainActivityUiState by mutableStateOf(Loading)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(STARTED) {
                viewModel.uiState
                    .onEach { uiState = it }
                    .collect()
            }
        }

        setContent {
            AppTheme(
                darkTheme = isDarkTheme(uiState),
            ) {
                Row {
//                    SettingsDialog(onDismiss = { })
                    SetupScreen()
                }

            }
        }
    }

    @Composable
    private fun isDarkTheme(
        uiState: MainActivityUiState,
    ): Boolean = when (uiState) {
        Loading -> isSystemInDarkTheme()
        is Success -> when (uiState.userData.darkThemeConfig) {
            FOLLOW_SYSTEM -> isSystemInDarkTheme()
            LIGHT -> false
            DARK -> true
        }
    }
}