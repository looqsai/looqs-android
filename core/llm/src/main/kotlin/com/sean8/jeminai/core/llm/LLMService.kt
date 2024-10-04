package com.sean8.jeminai.core.llm

import com.sean8.jeminai.core.actions.Action
import com.squareup.moshi.JsonClass
import java.io.File

interface LLMService {
    suspend fun sendMessage(message: String, imageBase64: String?): LLMResponse?
    suspend fun transcribeAudio(audioFile: File): String?
}

@JsonClass(generateAdapter = true)
data class LLMResponse(
    val spoken: String,
    val actions: List<Action>,
)