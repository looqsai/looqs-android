package ai.looqs.android.core.reflector

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReflectorMessage(
    @Json(name = "type") val type: MessageType,
    @Json(name = "content") val content: String? = null,
    @Json(name = "metadata") val metadata: Metadata? = null,
    @Json(name = "is_visible") val isVisible: Boolean = true,
)

@JsonClass(generateAdapter = true)
data class Metadata(
    @Json(name = "actions") val actions: List<String>? = null,
    @Json(name = "requires_additional") val requiresAdditional: Boolean = false,
    @Json(name = "model") val model: String? = null, // Plaintext JSON string
    @Json(name = "image_id") val imageId: String? = null, // Image ID if an image is sent
)

enum class MessageType {
    USER,
    AI,
    TYPING,
    STOP_TYPING,
    VOICE_RECORDING_START,
    VOICE_RECORDING_STOP,
}