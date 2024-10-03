package com.sean8.jeminai.core.accessibility

import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccessibilityEventBus @Inject constructor() {
    private val _eventFlow = MutableSharedFlow<AccessibilityEvent>(replay = 1)
    val eventFlow: SharedFlow<AccessibilityEvent> = _eventFlow.asSharedFlow()

    suspend fun emit(event: AccessibilityEvent) {
        _eventFlow.emit(event)
    }
}