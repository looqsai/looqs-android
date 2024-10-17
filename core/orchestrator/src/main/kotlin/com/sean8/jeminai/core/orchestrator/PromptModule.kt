package com.sean8.jeminai.core.orchestrator

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PromptModule {

    @Provides
    @Singleton
    fun provideAssistantPrompt(@ApplicationContext context: Context): String {
        return PromptLoader.loadPrompt(context)
    }
}
