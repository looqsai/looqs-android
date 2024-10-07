package com.sean8.jeminai.core.accessibility.screenshots

import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

@Singleton
object ProjectionState {
    private val _projectionToken = MutableStateFlow<Intent?>(null)
    val projectionToken = _projectionToken.asStateFlow()

    fun setProjectionToken(intent: Intent) {
        _projectionToken.value = intent
    }
}