package com.sean8.jeminai.core.voice

import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import javax.inject.Inject

class AudioRecorderImpl @Inject constructor() : AudioRecorder {
    private val TAG = "AudioRecorderInterface"

    private var mediaRecorder: MediaRecorder? = null

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)

    override val state: StateFlow<RecordingState>
        get() = _recordingState.asStateFlow()

    override fun startRecording(path: String) {
        mediaRecorder = MediaRecorder().apply {
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
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            Log.d(TAG, "Recording stopped")
            _recordingState.value = RecordingState.Finished
        } catch (e: RuntimeException) {
            Log.e(TAG, "Failed to stop recording", e)
        }
    }

    // TODO: mediaRecorder?.release()
}