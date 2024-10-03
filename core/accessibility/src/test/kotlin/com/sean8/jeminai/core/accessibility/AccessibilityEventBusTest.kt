package com.sean8.jeminai.core.accessibility

import android.view.accessibility.AccessibilityEvent
import com.sean8.jeminai.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AccessibilityEventBusTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var bus: AccessibilityEventBus

    @Before
    fun setup() {
        bus = AccessibilityEventBus()
    }

    @Test
    fun `Test emitting value and collect`() = runTest {
        val eventToEmit = AccessibilityEvent()

        bus.emit(eventToEmit)

        advanceUntilIdle()

        val eventReceived: AccessibilityEvent = bus.eventFlow.first()

        assertEquals(eventReceived, eventToEmit)
    }
}