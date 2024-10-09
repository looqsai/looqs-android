package com.sean8.jeminai.core.llm

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LLMModule {

    @Provides
    @Singleton
    fun provideLLMService(): LLMService = ChatGPTService()

    @Provides
    @Singleton
    fun provideTextToSpeechService(
        @ApplicationContext context: Context,
    ): TextToSpeechService = ChatGPTTextToSpeechService(context)
}