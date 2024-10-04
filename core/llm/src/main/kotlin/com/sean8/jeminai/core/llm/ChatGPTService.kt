package com.sean8.jeminai.core.llm

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatGPTService @Inject constructor() : LLMService {

    private val TAG = "ChatGPTService"

    private val NetworkTimeout = 20L

    private val client = OkHttpClient().newBuilder()
        .callTimeout(NetworkTimeout, SECONDS)
        .readTimeout(NetworkTimeout, SECONDS)
        .connectTimeout(NetworkTimeout, SECONDS)
        .build()

    // Replace with your actual API key or securely fetch it from your configuration
    private val apiKey = BuildConfig.OPENAI_API_KEY

    // Simplified Moshi configuration
    private val moshiRequest: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val chatGPTRequestAdapter = moshiRequest.adapter(ChatGPTRequest::class.java)
    private val chatGPTResponseAdapter =
        com.sean8.jeminai.core.actions.MoshiConfig.moshi.adapter(ChatGPTResponse::class.java)
    private val llmResponseAdapter =
        com.sean8.jeminai.core.actions.MoshiConfig.moshi.adapter(LLMResponse::class.java)

    override suspend fun sendMessage(message: String, imageBase64: String?): LLMResponse? {
        // Create the user message
        val userMessage = RequestChatMessage(
            role = "user",
            content = message,
        )

        // Build the request
        val requestData = ChatGPTRequest(
            model = "gpt-4",
            messages = listOf(userMessage),
            max_tokens = 500,
        )

        // Serialize the request
        val requestBodyString = chatGPTRequestAdapter.toJson(requestData)
        Log.d(TAG, "Request JSON: $requestBodyString")
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
                        return@withContext null
                    } else {
                        val chatGPTResponse = chatGPTResponseAdapter.fromJson(responseBody!!)
                        val assistantMessage = chatGPTResponse?.choices?.firstOrNull()?.message

                        // Process assistantMessage.content as JSON
                        val responseContent = assistantMessage?.content
                        if (responseContent != null) {
                            // Extract JSON from content
                            val jsonString = extractJsonFromContent(responseContent)
                            Log.d(TAG, "Extracted JSON: $jsonString")

                            // Parse the JSON content
                            val llmResponse = llmResponseAdapter.fromJson(jsonString)
                            return@withContext llmResponse
                        } else {
                            return@withContext null
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error: ${e.message}", e)
                return@withContext null
            }
        }
    }

    override suspend fun transcribeAudio(audioFile: File): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.openai.com/v1/audio/transcriptions"

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        audioFile.name,
                        audioFile.asRequestBody("audio/m4a".toMediaTypeOrNull()),
                    )
                    .addFormDataPart("model", "whisper-1")
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.e(TAG, "Transcription request failed: ${response.code} ${response.message}")
                    null
                } else {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val json = JSONObject(responseBody)
                        val transcribedText = json.getString("text")
                        transcribedText
                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in transcribeAudio: $e", e)
                null
            }
        }
    }

    private fun extractJsonFromContent(content: String): String {
        // Remove code fences if present
        val regex = Regex("""```json\s*(\{[\s\S]*?\})\s*```""")
        val matchResult = regex.find(content)
        return if (matchResult != null) {
            // Extract the JSON string within the code fences
            matchResult.groupValues[1]
        } else {
            // If no code fences, assume the content is the JSON string
            content
        }
    }
}
