package com.sean8.jeminai.core.orchestrator

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.sean8.core.hiltcoroutines.di.ApplicationScope
import com.sean8.jeminai.core.accessibility.record.AccessibilityEventsRecorder
import com.sean8.jeminai.core.accessibility.util.AccessibilityNodeInfoXmlDumper
import com.sean8.jeminai.core.accessibility.util.AccessibilityServiceHelper
import com.sean8.jeminai.core.accessibility.util.TreeNode
import com.sean8.jeminai.core.accessibility.util.populateChildren
import com.sean8.jeminai.core.actions.Action
import com.sean8.jeminai.core.llm.LLMResponse
import com.sean8.jeminai.core.llm.LLMService
import com.sean8.jeminai.core.llm.PromptConstants
import com.sean8.jeminai.core.util.encodeBitmapToBase64
import com.sean8.jeminai.core.voice.VoiceTransmitter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class OrchestratorService : Service() {
    private val TAG = "OrchestratorService"

    @Inject
    lateinit var llmService: LLMService

    @Inject
    lateinit var accessibilityHelperLazy: Provider<AccessibilityServiceHelper>

    @Inject
    lateinit var eventsRecorder: AccessibilityEventsRecorder

    @Inject
    lateinit var voiceTransmitter: VoiceTransmitter

    private lateinit var accessibilityHelper: AccessibilityServiceHelper

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "OrchestratorService created")
        Log.d(TAG, "VoiceTransmitter instance: $voiceTransmitter")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        accessibilityHelper = accessibilityHelperLazy.get()

        val foregroundFlags =
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION xor if (VERSION.SDK_INT >= VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            } else {
                0
            }

        if (VERSION.SDK_INT >= VERSION_CODES.R) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                createNotification(),
                foregroundFlags,
            )
        }

        appScope.launch {
            voiceTransmitter.latestVoice.collect { voiceFile ->
                voiceFile?.let {
                    voiceTriggered(voiceFile)
                }
            }

        }
        return START_STICKY
    }

    private suspend fun voiceTriggered(voiceFile: File) {
        val transcribedText = transcribeAudioFile(voiceFile)
        if (transcribedText != null) {
            Log.d(TAG, "Transcribed Text: $transcribedText")
            captureAndSendData(userMessage = transcribedText, isRequestEcho = false)
        } else {
            Log.e(TAG, "Failed to transcribe audio")
        }
    }

    private suspend fun transcribeAudioFile(audioFile: File): String? {
        Log.d(TAG, "Transcribing audio file: ${audioFile.path}")
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

        // Prepare recent events
        val events = eventsRecorder.getAndResetEventHistory()

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
            "${PromptConstants.GENERAL_PROMPT}\n\n" +
                "User Input: \"$message\"\n\n" +
                "Latest events: \n$events" + "\n\n" +
                "View Hierarchy Data: \n$xrayData"

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

        llmResponse.actions?.forEach { action: Action ->
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
    }
}
