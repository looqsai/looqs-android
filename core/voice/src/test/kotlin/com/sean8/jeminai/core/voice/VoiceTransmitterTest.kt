package com.sean8.jeminai.core.voice

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class VoiceTransmitterTest {

    private lateinit var voiceTransmitter: VoiceTransmitter

    @Before
    fun setUp() {
        voiceTransmitter = VoiceTransmitter()
    }

    @Test
    fun `postNewRecording updates latestVoice`() = runBlocking {
        val testFile = File("test_recording.m4a")

        voiceTransmitter.postNewRecording(testFile)

        val latestVoice = voiceTransmitter.latestVoice.first()
        assertEquals(testFile, latestVoice)
    }
}
