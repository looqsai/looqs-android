package com.sean8.jeminai.core.voice

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
abstract class AudioModule {

    @Binds
    @ServiceScoped
    abstract fun bindAudioRecorderFactory(
        impl: AudioRecorderFactoryImpl,
    ): AudioRecorderFactory
}