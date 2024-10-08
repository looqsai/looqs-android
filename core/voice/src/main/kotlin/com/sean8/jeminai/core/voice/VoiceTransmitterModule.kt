package com.sean8.jeminai.core.voice

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VoiceTransmitterModule {

    @Provides
    @Singleton
    internal fun provideVoiceTransmitter() = VoiceTransmitter()
}