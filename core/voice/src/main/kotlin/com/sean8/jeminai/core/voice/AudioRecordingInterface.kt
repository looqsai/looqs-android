package com.sean8.jeminai.core.voice

import kotlinx.coroutines.flow.StateFlow

interface AudioRecorder {
    val state: StateFlow<RecordingState>
    fun startRecording(path: String)
    fun stopRecording()
}

sealed class RecordingState {
    data object Idle : RecordingState()
    data object Started : RecordingState()
    data object Finished : RecordingState()
    data class Error(val message: String) : RecordingState()
}