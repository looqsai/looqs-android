package com.sean8.jeminai.core.llm.models

import com.sean8.jeminai.core.actions.Action
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Assistant data class representing the Assistant object
@JsonClass(generateAdapter = true)
data class Assistant(
    val id: String,
    val name: String? = null,
    val description: String? = null,
)

// Thread data class representing the Thread object
@JsonClass(generateAdapter = true)
data class Thread(
    val id: String,
)

// LLMResponse data class representing the assistant's reply after processing
@JsonClass(generateAdapter = true)
data class LLMResponse(
    val spoken: String,
    val actions: List<Action>,
    val requiresFurtherAction: Boolean = false,
)

// Data classes for making requests to the API
@JsonClass(generateAdapter = true)
data class RequestChatMessage(
    val role: String, // "user" or "assistant"
    val content: List<RequestContentBlock>,
)

// Image URL content for image blocks in requests
@JsonClass(generateAdapter = true)
data class ImageUrlContent(
    val url: String,
    val detail: String? = null, // "low", "high", or "auto"
)

// Data classes for parsing responses from the API
@JsonClass(generateAdapter = true)
data class MessagesResponse(
    @Json(name = "object") val objectType: String,
    val data: List<Message>,
    @Json(name = "first_id") val firstId: String?,
    @Json(name = "last_id") val lastId: String?,
    @Json(name = "has_more") val hasMore: Boolean,
)

@JsonClass(generateAdapter = true)
data class Message(
    val id: String,
    @Json(name = "object") val objectType: String,
    @Json(name = "created_at") val createdAt: Long,
    @Json(name = "assistant_id") val assistantId: String?,
    @Json(name = "thread_id") val threadId: String,
    @Json(name = "run_id") val runId: String?,
    val role: String,
    val content: List<ResponseContentBlock>,
    val attachments: List<Attachment>?,
    val metadata: Map<String, Any>?,
)

// Content blocks used in the response messages
@JsonClass(generateAdapter = true, generator = "sealed:type")
sealed class ResponseContentBlock {

    @JsonClass(generateAdapter = true)
    @Json(name = "text")
    data class TextBlock(
        val text: TextContent,
    ) : ResponseContentBlock()

    @JsonClass(generateAdapter = true)
    @Json(name = "image_file")
    data class ImageFileBlock(
        @Json(name = "image_file")
        val imageFile: ImageFileContent,
    ) : ResponseContentBlock()
}

// Text content in response messages
@JsonClass(generateAdapter = true)
data class TextContent(
    val value: String,
    val annotations: List<Annotation> = emptyList(),
)

// Annotation data class (undefined)
@JsonClass(generateAdapter = true)
data class Annotation(
    val ffu: String? = null,
)

// Attachment data class (undefined)
@JsonClass(generateAdapter = true)
data class Attachment(
    val ffu: String? = null,
)

// RunRequest data class used when creating a run
@JsonClass(generateAdapter = true)
data class RunRequest(
    @Json(name = "assistant_id")
    val assistantId: String,
)

// Additional data classes for other API responses (if necessary)
@JsonClass(generateAdapter = true)
data class ChatGptResponse(
    val id: String,
    @Json(name = "object")
    val objectType: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage? = null,
    @Json(name = "system_fingerprint")
    val systemFingerprint: String? = null,
)

@JsonClass(generateAdapter = true)
data class Choice(
    val index: Int,
    val message: ResponseChatMessage,
    val logprobs: Any? = null,
    @Json(name = "finish_reason")
    val finishReason: String? = null,
)

@JsonClass(generateAdapter = true)
data class ResponseChatMessage(
    val role: String,
    val content: String,
    val refusal: Any? = null,
)

@JsonClass(generateAdapter = true)
data class Usage(
    @Json(name = "prompt_tokens")
    val promptTokens: Int,
    @Json(name = "completion_tokens")
    val completionTokens: Int,
    @Json(name = "total_tokens")
    val totalTokens: Int,
    @Json(name = "prompt_tokens_details")
    val promptTokensDetails: PromptTokensDetails? = null,
    @Json(name = "completion_tokens_details")
    val completionTokensDetails: CompletionTokensDetails? = null,
)

@JsonClass(generateAdapter = true)
data class PromptTokensDetails(
    @Json(name = "cached_tokens")
    val cachedTokens: Int,
)

@JsonClass(generateAdapter = true)
data class CompletionTokensDetails(
    @Json(name = "reasoning_tokens")
    val reasoningTokens: Int,
)

@JsonClass(generateAdapter = true)
data class AssistantRequest(
    val instructions: String,
    val name: String,
    val model: String,
)

@JsonClass(generateAdapter = true)
data class TranscriptionResponse(
    val text: String,
)

@JsonClass(generateAdapter = true)
data class RunResponse(
    val id: String,
    val `object`: String,
    @Json(name = "created_at")
    val createdAt: Long,
    @Json(name = "assistant_id")
    val assistantId: String,
    @Json(name = "thread_id")
    val threadId: String,
    val status: RunStatus,
    @Json(name = "started_at")
    val startedAt: Long?,
    @Json(name = "expires_at")
    val expiresAt: Long?,
    @Json(name = "cancelled_at")
    val cancelledAt: Long?,
    @Json(name = "failed_at")
    val failedAt: Long?,
    @Json(name = "completed_at")
    val completedAt: Long?,
    @Json(name = "last_error")
    val lastError: Any?,
)

enum class RunStatus {
    @Json(name = "queued")
    QUEUED,

    @Json(name = "in_progress")
    IN_PROGRESS,

    @Json(name = "completed")
    COMPLETED,

    @Json(name = "failed")
    FAILED,

    @Json(name = "cancelled")
    CANCELLED,

    @Json(name = "expired")
    EXPIRED,

    @Json(name = "requires_action")
    REQUIRES_ACTION,

    @Json(name = "incomplete")
    INCOMPLETE,

    @Json(name = "unknown")
    UNKNOWN
}

@JsonClass(generateAdapter = true)
data class FileUploadResponse(
    val id: String,
    @Json(name = "object") val objectType: String,
    val bytes: Int,
    @Json(name = "created_at") val createdAt: Long,
    val filename: String,
    val purpose: String,
)

// Modify RequestContentBlock to include ImageFileBlock
@JsonClass(generateAdapter = true, generator = "sealed:type")
sealed class RequestContentBlock {

    @JsonClass(generateAdapter = true)
    @Json(name = "text")
    data class TextBlock(
        val text: String,
    ) : RequestContentBlock()

    @JsonClass(generateAdapter = true)
    @Json(name = "image_file")
    data class ImageFileBlock(
        @Json(name = "image_file")
        val imageFile: ImageFileContent,
    ) : RequestContentBlock()
}

@JsonClass(generateAdapter = true)
data class ImageFileContent(
    @Json(name = "file_id") val fileId: String,
    val detail: String? = null, // "low", "high", or "auto"
)