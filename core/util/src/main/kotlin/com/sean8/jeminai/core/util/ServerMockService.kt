package com.sean8.jeminai.core.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sean8.jeminai.core.llm.LLMResponse
import com.sean8.jeminai.core.llm.LLMService
import com.sean8.jeminai.core.llm.PromptConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class ServerMockService : Service() {

    @Inject
    lateinit var llmService: LLMService

    @Inject
    lateinit var accessibilityHelperLazy: Provider<AccessibilityHelper>

    lateinit var accessibilityHelper: AccessibilityHelper

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val TAG = "ServerMockService"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ServerMockService created")
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        accessibilityHelper = accessibilityHelperLazy.get()

        serviceScope.launch {
            handleIntent(intent)
        }
        return START_STICKY
    }

    private suspend fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_PROCESS_AUDIO_INPUT -> {
                val audioFilePath = intent.getStringExtra(EXTRA_AUDIO_FILE_PATH)
                Log.d(TAG, "Received audio file path: $audioFilePath")
                if (audioFilePath != null) {
                    val transcribedText = transcribeAudioFile(audioFilePath)
                    if (transcribedText != null) {
                        Log.d(TAG, "Transcribed Text: $transcribedText")
                        captureAndSendData(userMessage = transcribedText, isRequestEcho = false)
                    } else {
                        Log.e(TAG, "Failed to transcribe audio")
                    }
                } else {
                    Log.e(TAG, "Audio file path is null")
                }
            }
            else -> {
                Log.d(TAG, "Unknown action received")
            }
        }
    }

    private suspend fun transcribeAudioFile(audioFilePath: String): String? {
        Log.d(TAG, "Transcribing audio file: $audioFilePath")
        val audioFile = File(audioFilePath)
        if (!audioFile.exists()) {
            Log.e(TAG, "Audio file does not exist")
            return null
        }

        // Send the audio file to LLM Whisper API for transcription
        return try {
            llmService.transcribeAudio(audioFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error transcribing audio", e)
            null
        }
    }

    private suspend fun captureAndSendData(userMessage: String, isRequestEcho: Boolean) {
        Log.d(TAG, "Capturing data and sending to LLM")
        val rootNode = accessibilityHelper.getRootNode()
        if (rootNode == null) {
            Log.e(TAG, "Failed to get root node")
            return
        }
        val treeRoot = TreeNode(rootNode)
        treeRoot.populateChildren()
        val xrayData =
            AccessibilityNodeInfoXmlDumper.getXml(
                treeRoot,
                accessibilityHelper.getDefaultDisplay()!!,
            )

        // Capture Screenshot
        val bitmap = accessibilityHelper.captureScreenshot()
        var screenshotBase64: String? = null
        if (bitmap != null) {
            screenshotBase64 = encodeBitmapToBase64(bitmap)
        }

        // Prepare the message
        val message = if (isRequestEcho) {
            // Request-Echo: No new user input
            ""
        } else {
            userMessage
        }

        // Combine the general prompt with the user message and XRAY data
        val prompt =
            "${PromptConstants.GENERAL_PROMPT}\n\nUser Input: \"$message\"\n\nXRAY Data:\n$xrayData"

        // Send message to LLM with image
        try {
            val llmResponse = llmService.sendMessage(prompt, screenshotBase64)
            Log.d(TAG, "Received LLM Response: $llmResponse")

            if (llmResponse != null) {
                processLLMResponse(llmResponse)
            } else {
                Log.w(TAG, "Exit due to null llmResponse")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error sending message to LLM Service", e)
        }
    }

    private fun processLLMResponse(llmResponse: LLMResponse) {
        Log.d(TAG, "Processing LLM Response")
        llmResponse.spoken?.let { spoken ->
            if (spoken.lowercase() != "none") {
                // Handle spoken response
                Log.d(TAG, "Spoken: $spoken")
                accessibilityHelper.speak(spoken)
            }
        }

        llmResponse.actions?.forEach { action ->
            Log.d(TAG, "Performing action: $action")
            accessibilityHelper.performAction(action)
        }

        // TODO: After performing actions, send Request-Echo. Need more logic so it won't be infinite loop
//        serviceScope.launch {
//            captureAndSendData(userMessage = "", isRequestEcho = true)
//        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val notificationChannelId = "SERVER_MOCK_SERVICE_CHANNEL"

        val channel = NotificationChannel(
            notificationChannelId,
            "Server Mock Service",
            NotificationManager.IMPORTANCE_LOW,
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Assistant Active")
            .setContentText("Processing voice commands")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)

        return notificationBuilder.build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        const val ACTION_PROCESS_AUDIO_INPUT = "com.sean8.jeminai.ACTION_PROCESS_AUDIO_INPUT"
        const val EXTRA_AUDIO_FILE_PATH = "extra_audio_file_path"
    }
}
