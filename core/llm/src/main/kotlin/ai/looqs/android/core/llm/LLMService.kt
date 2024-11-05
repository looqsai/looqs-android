package ai.looqs.android.core.llm

import ai.looqs.android.core.llm.models.Assistant
import ai.looqs.android.core.llm.models.LLMResponse
import ai.looqs.android.core.llm.models.Thread
import java.io.File

interface LLMService {
    suspend fun initializeAssistant(assistantInstructions: String): Assistant
    suspend fun createThread(): Thread
    suspend fun sendMessage(
        assistantId: String,
        threadId: String,
        message: String,
        imageFile: File? = null,
    ): LLMResponse?
    suspend fun transcribeAudio(audioFile: File): String?
}
