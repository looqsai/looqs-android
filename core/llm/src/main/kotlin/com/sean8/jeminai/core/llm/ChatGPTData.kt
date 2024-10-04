package com.sean8.jeminai.core.llm

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatGPTRequest(
    val model: String,
    val messages: List<RequestChatMessage>,
    val max_tokens: Int? = null,
)

@JsonClass(generateAdapter = true)
data class RequestChatMessage(
    val role: String,
    val content: List<ContentBlock>,
)

@JsonClass(generateAdapter = true)
open class ContentBlock {
    @JsonClass(generateAdapter = true)
    data class TextBlock(
        val type: String = "text",
        val text: String,
    ) : ContentBlock()

    @JsonClass(generateAdapter = true)
    data class ImageBlock(
        val type: String = "image_url",
        val image_url: ImageUrl,
    ) : ContentBlock()
}

@JsonClass(generateAdapter = true)
data class ImageUrl(
    val url: String,
    val detail: String? = null, // "low", "high", or "auto"
)

@JsonClass(generateAdapter = true)
data class ChatGPTResponse(
    val id: String,
    val choices: List<Choice>,
)

@JsonClass(generateAdapter = true)
data class Choice(
    val index: Int,
    val message: ResponseChatMessage,
)

@JsonClass(generateAdapter = true)
data class ResponseChatMessage(
    val role: String,
    val content: String,
)
