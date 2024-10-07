package com.sean8.jeminai.core.voice

import com.sean8.core.hiltcoroutines.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Singleton

@Singleton
class VoiceTransmitter(
    @ApplicationScope private val scope: CoroutineScope,
) {
    private val _latestVoice = MutableStateFlow<File?>(null)

    val latestVoice: StateFlow<File?> = _latestVoice.asStateFlow()

    fun postNewRecording(recordingFile: File) {
        scope.launch {
            _latestVoice.emit(recordingFile)
        }
    }
}