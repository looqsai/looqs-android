package com.sean8.jeminai.core.llm

import com.sean8.jeminai.core.actions.Action
import com.squareup.moshi.JsonClass

interface LLMService {
    suspend fun sendMessage(message: String, imageBase64: String?): LLMResponse
    fun resetSession()
}

@JsonClass(generateAdapter = true)
data class LLMResponse(
    val spoken: String?,
    val actions: List<Action>?,
)