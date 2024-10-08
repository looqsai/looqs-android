package com.sean8.jeminai.core.llm

import com.squareup.moshi.Json
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

@JsonClass(generateAdapter = true, generator = "sealed:type")
sealed class ContentBlock {

    @JsonClass(generateAdapter = true)
    @Json(name = "text")
    data class TextBlock(
        val text: String,
    ) : ContentBlock()

    @JsonClass(generateAdapter = true)
    @Json(name = "image_url")
    data class ImageBlock(
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
    @Json(name = "object")
    val objectType: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage? = null,
    val system_fingerprint: String? = null,
)

@JsonClass(generateAdapter = true)
data class Choice(
    val index: Int,
    val message: ResponseChatMessage,
    val logprobs: Any? = null,
    val finish_reason: String? = null,
)

@JsonClass(generateAdapter = true)
data class ResponseChatMessage(
    val role: String,
    val content: String,
    val refusal: Any? = null,
)

@JsonClass(generateAdapter = true)
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int,
    val prompt_tokens_details: PromptTokensDetails? = null,
    val completion_tokens_details: CompletionTokensDetails? = null,
)

@JsonClass(generateAdapter = true)
data class PromptTokensDetails(
    val cached_tokens: Int,
)

@JsonClass(generateAdapter = true)
data class CompletionTokensDetails(
    val reasoning_tokens: Int,
)