package ai.looqs.android.core.llm

interface TextToSpeechService {
    suspend fun speak(text: String)
}
