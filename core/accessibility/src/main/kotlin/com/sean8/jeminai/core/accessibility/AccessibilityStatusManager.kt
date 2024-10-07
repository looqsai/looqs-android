package com.sean8.jeminai.core.accessibility

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

@Singleton
object AccessibilityStatusManager {
    private val _isServiceEnabled = MutableStateFlow(false)
    val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled.asStateFlow()

    fun setServiceEnabled(enabled: Boolean) {
        _isServiceEnabled.value = enabled
    }
}
