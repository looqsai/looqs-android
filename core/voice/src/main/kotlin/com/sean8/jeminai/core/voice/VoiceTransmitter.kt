package com.sean8.jeminai.core.voice

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceTransmitter @Inject constructor() {
    private val _latestVoice = MutableStateFlow<File?>(null)
    val latestVoice: StateFlow<File?> = _latestVoice.asStateFlow()

    fun postNewRecording(recordingFile: File) {
        _latestVoice.value = recordingFile
    }
}
