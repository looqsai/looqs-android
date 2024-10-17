package com.sean8.jeminai.core.llm

import com.sean8.jeminai.core.llm.models.Assistant
import com.sean8.jeminai.core.llm.models.LLMResponse
import com.sean8.jeminai.core.llm.models.Thread
import java.io.File

interface LLMService {
    suspend fun initializeAssistant(assistantInstructions: String): Assistant
    suspend fun createThread(): Thread
    suspend fun sendMessage(
        assistantId: String,
        threadId: String,
        message: String,
        imageBase64: String? = null,
    ): LLMResponse?
    suspend fun transcribeAudio(audioFile: File): String?
}
