package ai.looqs.android.core.llm

import ai.looqs.android.core.llm.BuildConfig
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatGPTTextToSpeechService @Inject constructor(
    @ApplicationContext private val context: Context,
) : TextToSpeechService {

    private val TAG = "ChatGPTTextToSpeechService"

    private val client = OkHttpClient()

    // Use your actual API key securely
    private val apiKey = BuildConfig.OPENAI_API_KEY

    override suspend fun speak(text: String) {
        withContext(Dispatchers.IO) {
            val url = "https://api.openai.com/v1/audio/speech"

            val jsonBody = """
                {
                    "model": "tts-1",
                    "input": "$text",
                    "voice": "alloy",
                    "response_format": "aac",
                    "speed": "1.05"
                }
            """.trimIndent()

            val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $apiKey")
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Failed to get speech: ${response.code} ${response.message}")
                        return@withContext
                    }

                    val audioBytes = response.body?.bytes()
                    if (audioBytes != null) {
                        playAudio(audioBytes)
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error during text-to-speech", e)
            }
        }
    }

    private fun playAudio(audioBytes: ByteArray) {
        try {
            // Create a temporary file to store the audio
            val tempFile = File.createTempFile("tts_audio", ".aac", context.cacheDir)
            tempFile.writeBytes(audioBytes)

            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(tempFile.absolutePath)
            mediaPlayer.prepare()
            mediaPlayer.start()

            mediaPlayer.setOnCompletionListener {
                mediaPlayer.release()
                tempFile.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio", e)
        }
    }
}
