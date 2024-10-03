package com.sean8.jeminai.core.accessibility.record

import android.view.accessibility.AccessibilityEvent
import com.sean8.core.hiltcoroutines.di.ApplicationScope
import com.sean8.jeminai.core.accessibility.AccessibilityEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccessibilityEventsRecorder @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope,
    private val eventBus: AccessibilityEventBus,
) {
    private val MAX_EVENT_HISTORY = 100

    private val _eventFlow = MutableSharedFlow<AccessibilityEvent>(extraBufferCapacity = 64)
    val eventFlow = _eventFlow.asSharedFlow()

    // Buffer to store events since the last consumption
    private val eventBuffer = mutableListOf<AccessibilityEvent>()

    init {
        scope.launch {
            eventBus.eventFlow.collect { event ->
                onEvent(event)
            }
        }
    }

    private fun onEvent(event: AccessibilityEvent) {
        // Emit the event to the shared flow
        scope.launch {
            _eventFlow.emit(AccessibilityEvent.obtain(event))
        }

        // Add the event to the buffer but first clear historical item
        if (eventBuffer.size >= MAX_EVENT_HISTORY) {
            eventBuffer.removeAt(0) // Remove the oldest event
        }
        eventBuffer.add(AccessibilityEvent.obtain(event))
    }

    /**
     * Returns the list of events since the last reset and clears the buffer.
     */
    fun getAndResetEventHistory(): List<AccessibilityEvent> {
        val events = eventBuffer.toList()
        eventBuffer.clear()
        return events
    }
}
