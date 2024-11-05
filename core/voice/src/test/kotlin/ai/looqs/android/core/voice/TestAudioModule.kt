package ai.looqs.android.core.voice

import ai.looqs.android.core.voice.AudioModule
import ai.looqs.android.core.voice.AudioRecorder
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AudioModule::class],
)
abstract class TestAudioModule {

    @Binds
    abstract fun bindTestAudio(
        mock: AudioRecordMock,
    ): AudioRecorder
}