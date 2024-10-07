package com.sean8.jeminai.core.voice

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