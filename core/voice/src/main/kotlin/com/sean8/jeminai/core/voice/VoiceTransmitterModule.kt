package com.sean8.jeminai.core.voice

import com.sean8.core.hiltcoroutines.di.ApplicationScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VoiceTransmitterModule {

    @Provides
    @Singleton
    internal fun provideVoiceTransmitter(
        @ApplicationScope scope: CoroutineScope,
    ) = VoiceTransmitter(scope)
}