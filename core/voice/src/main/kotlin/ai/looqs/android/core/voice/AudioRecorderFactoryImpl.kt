package ai.looqs.android.core.voice

import javax.inject.Inject

class AudioRecorderFactoryImpl @Inject constructor() : AudioRecorderFactory {
    override fun createAudioRecorder(): AudioRecorder {
        return AudioRecorderImpl()
    }
}
