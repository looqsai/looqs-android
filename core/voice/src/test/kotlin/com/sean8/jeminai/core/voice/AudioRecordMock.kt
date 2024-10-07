package com.sean8.jeminai.core.voice

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AudioRecordMock @Inject constructor() : AudioRecorder {
    var _state = MutableStateFlow<RecordingState>(RecordingState.Idle)

    override val state: StateFlow<RecordingState> = _state.asStateFlow()

    override fun startRecording(path: String) {
        _state.value = RecordingState.Started
    }

    override fun stopRecording() {
        _state.value = RecordingState.Finished
    }
}