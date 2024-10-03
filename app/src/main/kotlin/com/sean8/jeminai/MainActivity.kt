package com.sean8.jeminai

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.sean8.jeminai.MainActivityUiState.Loading
import com.sean8.jeminai.MainActivityUiState.Success
import com.sean8.jeminai.core.accessibility.AccessibilityEventBus
import com.sean8.jeminai.core.accessibility.llm.ServerMockService
import com.sean8.jeminai.core.accessibility.ui.SetupScreen
import com.sean8.jeminai.core.model.DarkThemeConfig.DARK
import com.sean8.jeminai.core.model.DarkThemeConfig.FOLLOW_SYSTEM
import com.sean8.jeminai.core.model.DarkThemeConfig.LIGHT
import com.sean8.jeminai.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    @Inject
    lateinit var eventBus: AccessibilityEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO ...
        val serviceIntent = Intent(this, ServerMockService::class.java)
        this.startForegroundService(serviceIntent)

        var uiState: MainActivityUiState by mutableStateOf(Loading)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(STARTED) {
                viewModel.uiState
                    .onEach { uiState = it }
                    .collect()
            }
        }

        lifecycleScope.launch {
            eventBus.eventFlow.collect { event ->
                Log.d("MainActivity", "Received eventBus event: $event")
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