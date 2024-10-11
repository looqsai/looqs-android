package com.sean8.jeminai.core.llm

import android.util.Log
import com.sean8.jeminai.core.actions.Action
import com.sean8.jeminai.core.llm.models.Assistant
import com.sean8.jeminai.core.llm.models.AssistantRequest
import com.sean8.jeminai.core.llm.models.ImageUrlContent
import com.sean8.jeminai.core.llm.models.LLMResponse
import com.sean8.jeminai.core.llm.models.MessagesResponse
import com.sean8.jeminai.core.llm.models.RequestChatMessage
import com.sean8.jeminai.core.llm.models.RequestContentBlock
import com.sean8.jeminai.core.llm.models.ResponseContentBlock
import com.sean8.jeminai.core.llm.models.RunRequest
import com.sean8.jeminai.core.llm.models.Thread
import com.sean8.jeminai.core.llm.models.TranscriptionResponse
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatGPTService @Inject constructor(
    moshiProvider: MoshiProvider,
) : LLMService {

    private val TAG = "ChatGPTService"
    private val client = OkHttpClient()
    private val apiKey = BuildConfig.OPENAI_API_KEY
    private val betaHeader = "OpenAI-Beta"
    private val betaHeaderValue = "assistants=v2"

    private var assistantId: String? = null
    private var threadId: String? = null

    private val moshi: Moshi = moshiProvider.moshi

    private val messagesAdapter = moshi.adapter(MessagesResponse::class.java)
    private val assistantAdapter = moshi.adapter(Assistant::class.java)
    private val threadAdapter = moshi.adapter(Thread::class.java)
    private val requestChatMessageAdapter = moshi.adapter(RequestChatMessage::class.java)
    private val runRequestAdapter = moshi.adapter(RunRequest::class.java)
    private val transcriptionResponseAdapter = moshi.adapter(TranscriptionResponse::class.java)
    private val assistantRequestAdapter = moshi.adapter(AssistantRequest::class.java)

    override suspend fun initializeAssistant(): Assistant {
        return withContext(Dispatchers.IO) {
            val url = "https://api.openai.com/v1/assistants"

            val assistantRequest = AssistantRequest(
                instructions = PromptConstants.GENERAL_PROMPT,
                name = "VI Assistant",
                model = "gpt-4",
            )

            val requestBodyString = assistantRequestAdapter.toJson(assistantRequest)
            val requestBody = requestBodyString.toRequestBody("application/json".toMediaType())

            Log.d(TAG, "Initializing assistant...")
            Log.v(TAG, "Assistant Request JSON: $requestBodyString")

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader(betaHeader, betaHeaderValue)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    Log.v(TAG, "Assistant Response: $responseBody")
                    if (!response.isSuccessful) {
                        Log.e(
                            TAG,
                            "Failed to create assistant: ${response.code} ${response.message}",
                        )
                        throw IOException("Failed to create assistant")
                    } else {
                        val assistant = assistantAdapter.fromJson(responseBody!!)
                        assistantId = assistant?.id
                        Log.d(TAG, "Assistant created with ID: $assistantId")
                        assistant ?: throw IOException("Failed to parse assistant")
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error creating assistant", e)
                throw e
            }
        }
    }

    override suspend fun createThread(): Thread {
        return withContext(Dispatchers.IO) {
            val url = "https://api.openai.com/v1/threads"

            val requestBody = "{}".toRequestBody("application/json".toMediaType())

            Log.d(TAG, "Creating new thread...")

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader(betaHeader, betaHeaderValue)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    Log.v(TAG, "Create Thread Response: $responseBody")
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Failed to create thread: ${response.code} ${response.message}")
                        throw IOException("Failed to create thread")
                    } else {
                        val thread = threadAdapter.fromJson(responseBody!!)
                        threadId = thread?.id
                        Log.d(TAG, "Thread created with ID: $threadId")
                        thread ?: throw IOException("Failed to parse thread")
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error creating thread", e)
                throw e
            }
        }
    }

    override suspend fun sendMessage(
        threadId: String,
        message: String,
        imageBase64: String?,
    ): LLMResponse? {
        Log.d(TAG, "Sending message to thread ID: $threadId")
        // Build the content blocks
        val contentBlocks = mutableListOf<RequestContentBlock>()

        // Add the text message
        contentBlocks.add(RequestContentBlock.TextBlock(text = message))

        // Add the image if provided
        if (imageBase64 != null) {
            contentBlocks.add(
                RequestContentBlock.ImageBlock(
                    imageUrl = ImageUrlContent(
                        url = "data:image/jpeg;base64,$imageBase64",
                    ),
                ),
            )
            Log.d(TAG, "Image included in the message")
        }

        // Create the user message
        val userMessage = RequestChatMessage(
            role = "user",
            content = contentBlocks,
        )

        // Add the message to the thread
        withContext(Dispatchers.IO) {
            val url = "https://api.openai.com/v1/threads/$threadId/messages"

            val requestBodyString = requestChatMessageAdapter.toJson(userMessage)
            val requestBody = requestBodyString.toRequestBody("application/json".toMediaType())

            Log.d(TAG, "Adding message to thread...")
            Log.v(TAG, "Message Request JSON: $requestBodyString")

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader(betaHeader, betaHeaderValue)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    Log.v(TAG, "Add Message Response: $responseBody")
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Failed to add message: ${response.code} ${response.message}")
                        throw IOException("Failed to add message")
                    } else {
                        Log.d(TAG, "Message added to thread successfully")
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error adding message to thread", e)
                throw e
            }
        }

        // Create and run the Assistant on the Thread
        return runAssistantOnThread(threadId)
    }

    private suspend fun runAssistantOnThread(threadId: String): LLMResponse? {
        Log.d(TAG, "Running assistant on thread ID: $threadId")
        return withContext(Dispatchers.IO) {
            val url = "https://api.openai.com/v1/threads/$threadId/runs"

            val runRequest = RunRequest(
                assistantId = assistantId!!,
            )

            val requestBodyString = runRequestAdapter.toJson(runRequest)
            val requestBody = requestBodyString.toRequestBody("application/json".toMediaType())

            Log.v(TAG, "Run Request JSON: $requestBodyString")

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader(betaHeader, betaHeaderValue)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    Log.v(TAG, "Run Assistant Response: $responseBody")
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Failed to run assistant: ${response.code} ${response.message}")
                        return@withContext null
                    } else {
                        Log.d(TAG, "Assistant run successfully on thread")
                        // Parse the assistant's reply from the thread messages
                        val llmResponse = parseAssistantReply(threadId)
                        return@withContext llmResponse
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error running assistant", e)
                return@withContext null
            }
        }
    }

    private suspend fun parseAssistantReply(threadId: String): LLMResponse? {
        Log.d(TAG, "Parsing assistant's reply for thread ID: $threadId")
        val url = "https://api.openai.com/v1/threads/$threadId/messages"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader(betaHeader, betaHeaderValue)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    Log.v(TAG, "Get Messages Response: $responseBody")
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Failed to get messages: ${response.code} ${response.message}")
                        return@withContext null
                    } else {
                        val messagesResponse = messagesAdapter.fromJson(responseBody!!)
                        if (messagesResponse != null) {
                            val messages = messagesResponse.data
                            // Get the last assistant message
                            for (message in messages.asReversed()) {
                                if (message.role == "assistant") {
                                    for (contentBlock in message.content) {
                                        if (contentBlock is ResponseContentBlock.TextBlock) {
                                            var assistantText = contentBlock.text.value.trim()

                                            Log.d(TAG, "Assistant Raw Text: $assistantText")

                                            // Extract JSON object from the assistant's reply
                                            val jsonString = extractJsonFromText(assistantText)
                                            if (jsonString != null) {
                                                Log.d(TAG, "Extracted JSON: $jsonString")
                                                // Parse the JSON string
                                                val jsonReader =
                                                    JsonReader.of(Buffer().writeUtf8(jsonString))
                                                try {
                                                    val llmResponse = parseLLMResponse(jsonReader)
                                                    Log.d(TAG, "Parsed LLM Response successfully")
                                                    return@withContext llmResponse
                                                } catch (e: Exception) {
                                                    Log.e(
                                                        TAG,
                                                        "Exception during parsing LLMResponse",
                                                        e,
                                                    )
                                                    // As a fallback, return the spoken text as is
                                                    return@withContext LLMResponse(
                                                        spoken = assistantText,
                                                        actions = emptyList(),
                                                    )
                                                }
                                            } else {
                                                Log.e(
                                                    TAG,
                                                    "No JSON object found in assistant's reply",
                                                )
                                                // As a fallback, return the spoken text as is
                                                return@withContext LLMResponse(
                                                    spoken = assistantText,
                                                    actions = emptyList(),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Log.w(TAG, "No assistant message found in thread messages")
                        } else {
                            Log.e(TAG, "Failed to parse messagesResponse")
                        }
                        return@withContext null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching messages", e)
                return@withContext null
            }
        }
    }

    private fun extractJsonFromText(text: String): String? {
        val jsonRegex = Regex("""\{.*\}""", RegexOption.DOT_MATCHES_ALL)
        val matchResult = jsonRegex.find(text)
        return matchResult?.value
    }

    private fun parseLLMResponse(jsonReader: JsonReader): LLMResponse {
        var spoken: String? = null
        val actions = mutableListOf<Action>()
        var requiresFurtherAction = false
        jsonReader.isLenient = true
        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "spoken" -> {
                    spoken = jsonReader.nextString()
                }

                "actions" -> {
                    jsonReader.beginArray()
                    while (jsonReader.hasNext()) {
                        try {
                            val actionAdapter = moshi.adapter(Action::class.java)
                            val action = actionAdapter.fromJson(jsonReader)
                            if (action != null) {
                                actions.add(action)
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to parse action", e)
                            jsonReader.skipValue() // Skip the problematic action
                        }
                    }
                    jsonReader.endArray()
                }

                "requiresFurtherAction" -> {
                    requiresFurtherAction = jsonReader.nextBoolean()
                }

                else -> {
                    jsonReader.skipValue()
                }
            }
        }
        jsonReader.endObject()
        if (spoken == null) {
            Log.e(TAG, "Missing 'spoken' field in assistant's response")
            spoken = ""
        }
        Log.v(
            TAG,
            "Parsed LLMResponse: spoken='$spoken', actions=$actions, requiresFurtherAction=$requiresFurtherAction",
        )
        return LLMResponse(
            spoken = spoken,
            actions = actions,
            requiresFurtherAction = requiresFurtherAction,
        )
    }

    override suspend fun transcribeAudio(audioFile: File): String? {
        Log.d(TAG, "Transcribing audio file: ${audioFile.path}")
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
                val responseBody = response.body?.string()
                Log.v(TAG, "Transcription Response: $responseBody")
                if (!response.isSuccessful) {
                    Log.e(TAG, "Transcription request failed: ${response.code} ${response.message}")
                    null
                } else {
                    if (responseBody != null) {
                        val transcriptionResponse =
                            transcriptionResponseAdapter.fromJson(responseBody)
                        val transcribedText = transcriptionResponse?.text
                        Log.d(TAG, "Transcribed Text: $transcribedText")
                        transcribedText
                    } else {
                        Log.e(TAG, "Transcription response body is null")
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in transcribeAudio", e)
                null
            }
        }
    }
}
