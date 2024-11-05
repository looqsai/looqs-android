package ai.looqs.android.core.llm

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LLMModule {

    @Provides
    @Singleton
    fun provideLLMService(
        moshiProvider: MoshiProvider,
    ): LLMService = ChatGPTService(moshiProvider, OkHttpClient(), "https://api.openai.com/v1")

    @Provides
    @Singleton
    fun provideTextToSpeechService(
        @ApplicationContext context: Context,
    ): TextToSpeechService = ChatGPTTextToSpeechService(context)
}