package com.sean8.jeminai.core.voice

import android.media.MediaRecorder
import javax.inject.Inject

class AudioRecorderFactoryImpl @Inject constructor() : AudioRecorderFactory {
    override fun createAudioRecorder(): AudioRecorder {
        return AudioRecorderImpl(MediaRecorder())
    }
}
