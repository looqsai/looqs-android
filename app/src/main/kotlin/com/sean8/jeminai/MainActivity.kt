package com.sean8.jeminai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sean8.jeminai.feature.settings.SettingsDialog
import com.sean8.jeminai.ui.theme.BaseAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BaseAppTheme {
                SettingsDialog(onDismiss = { })
            }
        }
    }
}