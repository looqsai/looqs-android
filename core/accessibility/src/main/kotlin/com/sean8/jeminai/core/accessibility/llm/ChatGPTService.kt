package com.sean8.jeminai.core.accessibility.llm

import android.util.Log
import com.sean8.jeminai.core.accessibility.actions.MoshiConfig.moshi
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatGPTService @Inject constructor() : LLMService {

    private val TAG = "ChatGPTService"

    private val client = OkHttpClient()

    // TODO: ...
    private val apiKey = ""

    private val chatGPTRequestAdapter: JsonAdapter<ChatGPTRequest> =
        moshi.adapter(ChatGPTRequest::class.java)

    private val chatGPTResponseAdapter: JsonAdapter<ChatGPTResponse> =
        moshi.adapter(ChatGPTResponse::class.java)

    private val llmResponseAdapter: JsonAdapter<LLMResponse> =
        moshi.adapter(LLMResponse::class.java)

    override suspend fun sendMessage(message: String, imageBase64: String?): LLMResponse {
        // Build content blocks for the request
        val contentBlocks = mutableListOf<ContentBlock>()

        // Add text block if message is not empty
        if (message.isNotEmpty()) {
            contentBlocks.add(ContentBlock.TextBlock(text = message))
        }

        // Add image block if available
        if (imageBase64 != null) {
            contentBlocks.add(
                ContentBlock.ImageBlock(
                    image_url = ImageUrl(
                        url = "data:image/png;base64,$imageBase64",
                        detail = "auto",
                    ),
                ),
            )
        }

        // Use RequestChatMessage for the request
        val userMessage = RequestChatMessage(
            role = "user",
            content = contentBlocks,
        )

        // Build the request
        val requestData = ChatGPTRequest(
            model = "gpt-4",
            messages = listOf(userMessage),
            max_tokens = 500,
        )

        // Serialize the request
        val requestBodyString = chatGPTRequestAdapter.toJson(requestData)
        val requestBody = requestBodyString.toRequestBody("application/json".toMediaType())

        // Build and execute the request
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    if (!response.isSuccessful) {
                        Log.e(
                            TAG,
                            "Error: ${response.code} ${response.message}, Body: $responseBody",
                        )
                        return@withContext LLMResponse(null, null)
                    } else {
                        val chatGPTResponse = chatGPTResponseAdapter.fromJson(responseBody!!)
                        val assistantMessage = chatGPTResponse?.choices?.firstOrNull()?.message

                        // Process assistantMessage.content as JSON
                        val responseContent = assistantMessage?.content
                        if (responseContent != null) {
                            // Parse the JSON content
                            val llmResponse = llmResponseAdapter.fromJson(responseContent)
                            return@withContext llmResponse ?: LLMResponse(null, null)
                        } else {
                            return@withContext LLMResponse(null, null)
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error: ${e.message}", e)
                return@withContext LLMResponse(null, null)
            }
        }
    }

    override fun resetSession() {
        // Implement session reset if needed
    }
}
