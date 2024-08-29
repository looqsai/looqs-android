package com.sean8.jeminai.core.dispatchers

import com.sean8.jeminai.core.dispatchers.di.DispatchersModule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test

@HiltAndroidTest
class DispatchersModuleTest {
    private val dispatchersModule = DispatchersModule

    @Test
    fun testProvidesIODispatcher() {
        val ioDispatcher: CoroutineDispatcher = dispatchersModule.providesIODispatcher()
        assertEquals(Dispatchers.IO, ioDispatcher)
    }

    @Test
    fun testProvidesDefaultDispatcher() {
        val defaultDispatcher: CoroutineDispatcher = dispatchersModule.providesDefaultDispatcher()
        assertEquals(Dispatchers.Default, defaultDispatcher)
    }
}