package com.sean8.jeminai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun BaseAppTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        typography = Typography,
        content = content,
    )
}