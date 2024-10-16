package com.sean8.jeminai.core.voice

import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import javax.inject.Inject

class AudioRecorderImpl @Inject constructor(
    private var mediaRecorder: MediaRecorder,
) : AudioRecorder {
    private val TAG = "AudioRecorderInterface"

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)

    override val state: StateFlow<RecordingState>
        get() = _recordingState.asStateFlow()

    // TODO: Handle the media recorder for mock
    override fun startRecording(path: String) {
        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(path)
            try {
                prepare()
                start()
                _recordingState.value = RecordingState.Started
                Log.d(TAG, "Recording started")
            } catch (e: IOException) {
                Log.e(TAG, "Failed to start recording", e)
                _recordingState.value = RecordingState.Error(e.toString())
            }
        }
    }

    override fun stopRecording() {
        try {
            mediaRecorder.apply {
                stop()
                release()
            }
            Log.d(TAG, "Recording stopped")
            _recordingState.value = RecordingState.Finished
        } catch (e: RuntimeException) {
            Log.e(TAG, "Failed to stop recording", e)
            _recordingState.value = RecordingState.Error("Failed to stop recording")
        }
    }
}