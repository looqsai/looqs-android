package ai.looqs.android.core.accessibility

import android.view.accessibility.AccessibilityEvent
import ai.looqs.android.core.accessibility.AccessibilityEventBus
import ai.looqs.android.core.testing.util.MainDispatcherRule
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

    private lateinit var bus: ai.looqs.android.core.accessibility.AccessibilityEventBus

    @Before
    fun setup() {
        bus = ai.looqs.android.core.accessibility.AccessibilityEventBus()
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