package ai.looqs.android.core.voice

import ai.looqs.android.core.voice.AudioRecorderImpl
import ai.looqs.android.core.voice.RecordingState
import android.media.MediaRecorder
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.io.IOException

class AudioRecorderImplTest {

    private lateinit var audioRecorder: AudioRecorderImpl
    private lateinit var mediaRecorder: MediaRecorder

    @Before
    fun setUp() {
        mediaRecorder = mock(MediaRecorder::class.java)
        audioRecorder = AudioRecorderImpl(mediaRecorder)
    }

    @After
    fun tearDown() {
        // Clean up resources if needed
    }

    @Test
    fun `startRecording should set state to Started`() = runBlocking {
        val path = "test_path.m4a"

        audioRecorder.startRecording(path)

        assertTrue(audioRecorder.state.value is RecordingState.Started)
        verify(mediaRecorder).prepare()
        verify(mediaRecorder).start()
        verify(mediaRecorder).setAudioSource(MediaRecorder.AudioSource.MIC)
        verify(mediaRecorder).setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        verify(mediaRecorder).setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        verify(mediaRecorder).setOutputFile(path)
    }

    @Test
    fun `stopRecording should set state to Finished`() = runBlocking {
        val path = "test_path.m4a"

        audioRecorder.startRecording(path)
        audioRecorder.stopRecording()

        assertTrue(audioRecorder.state.value is RecordingState.Finished)
        verify(mediaRecorder).stop()
        verify(mediaRecorder).release()
    }

    @Test
    fun `startRecording should set state to Error when IOException occurs`() = runBlocking {
        val path = "test_path.m4a"

        doThrow(IOException("Test exception")).`when`(mediaRecorder).prepare()

        audioRecorder.startRecording(path)

        assertTrue(audioRecorder.state.value is RecordingState.Error)
        val errorState = audioRecorder.state.value as RecordingState.Error
        assertEquals("java.io.IOException: Test exception", errorState.message)
    }

    @Test
    fun `stopRecording should handle RuntimeException`() = runBlocking {
        val path = "test_path.m4a"

        audioRecorder.startRecording(path)
        doThrow(RuntimeException("Test exception")).`when`(mediaRecorder).stop()

        audioRecorder.stopRecording()

        assertTrue(audioRecorder.state.value is RecordingState.Error)
        val errorState = audioRecorder.state.value as RecordingState.Error
        assertTrue(errorState.message.isNotEmpty())
    }
}
