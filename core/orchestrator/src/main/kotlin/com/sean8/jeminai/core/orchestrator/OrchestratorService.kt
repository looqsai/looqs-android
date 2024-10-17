package com.sean8.jeminai.core.orchestrator

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import com.sean8.jeminai.core.data.repository.UserDataRepository
import com.sean8.jeminai.core.llm.LLMService
import com.sean8.jeminai.core.llm.PromptConstants.ECHO_REQUEST
import com.sean8.jeminai.core.llm.TextToSpeechService
import com.sean8.jeminai.core.llm.models.LLMResponse
import com.sean8.jeminai.core.screenshot.ProjectedScreenshotExtractor
import com.sean8.jeminai.core.screenshot.ProjectionState
import com.sean8.jeminai.core.util.encodeBitmapToBase64
import com.sean8.jeminai.core.voice.VoiceTransmitter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
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

    @Inject
    lateinit var screenshotExtractor: ProjectedScreenshotExtractor

    @Inject
    lateinit var textToSpeechService: TextToSpeechService

    @Inject
    lateinit var assistantPrompt: String

    @Inject
    lateinit var userDataRepository: UserDataRepository

    private lateinit var accessibilityHelper: AccessibilityServiceHelper

    private var echoAttempts = 0
    private val maxEchoAttempts = 3

    @Inject
    @ApplicationScope
    lateinit var scope: CoroutineScope

    private var latestScreenshot: Bitmap? = null

    private var assistantId: String? = null
    private var threadId: String? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "OrchestratorService created")
        accessibilityHelper = accessibilityHelperLazy.get()

        // Initialize assistantId and threadId
        scope.launch {
            initializeAssistantAndThread()
        }

        if (VERSION.SDK_INT >= VERSION_CODES.R) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                createNotification(),
                0,
            )
        }

        // Start collecting voice inputs and media projections
        scope.launch {
            collectVoiceInputs()
            collectMediaProjections()
        }
    }

    private suspend fun initializeAssistantAndThread() {
        // Load assistantId and threadId from UserData
        val userData = userDataRepository.userData.first()
        assistantId = userData.assistantId
        threadId = userData.threadId

        if (assistantId.isNullOrEmpty()) {
            Log.d(TAG, "Assistant ID is null, initializing assistant")
            val assistant = llmService.initializeAssistant(assistantPrompt)
            assistantId = assistant.id
            userDataRepository.setAssistantId(assistantId!!)
            Log.d(TAG, "Assistant initialized with ID: $assistantId")
        } else {
            Log.d(TAG, "Using existing assistant ID: $assistantId")
        }

        if (threadId.isNullOrEmpty()) {
            Log.d(TAG, "Thread ID is null, creating thread")
            val thread = llmService.createThread()
            threadId = thread.id
            userDataRepository.setThreadId(threadId!!)
            Log.d(TAG, "Thread created with ID: $threadId")
        } else {
            Log.d(TAG, "Using existing thread ID: $threadId")
        }
    }

    private suspend fun collectMediaProjections() {
        ProjectionState.mediaProjection.collect { mediaProjection ->
            if (mediaProjection != null) {
                Log.d(TAG, "MediaProjection is available")

                // Start the screenshot extractor
                screenshotExtractor.start(
                    mediaProjection,
                    scope,
                    callback = { bitmap ->
                        latestScreenshot = bitmap
                    },
                )
            } else {
                Log.e(TAG, "MediaProjection is null")
            }
        }
    }

    private fun collectVoiceInputs() {
        scope.launch {
            voiceTransmitter.latestVoice.collect { voiceFile ->
                voiceFile?.let {
                    scope.launch {
                        voiceTriggered(voiceFile)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        screenshotExtractor.stop()
        Log.d(TAG, "OrchestratorService destroyed")
    }

    private suspend fun voiceTriggered(voiceFile: File) {
        Log.d(TAG, "Voice input received: ${voiceFile.name}")
        val transcribedText = transcribeAudioFile(voiceFile)
        if (transcribedText != null) {
            Log.d(TAG, "Transcribed Text: $transcribedText")
            captureAndSendData(userMessage = transcribedText)
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
            val transcription = llmService.transcribeAudio(audioFile)
            Log.d(TAG, "Transcription result: $transcription")
            transcription
        } catch (e: Exception) {
            Log.e(TAG, "Error transcribing audio", e)
            null
        }
    }

    private suspend fun captureAndSendData(userMessage: String) {
        Log.d(TAG, "Capturing data before sending to LLM")
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

        Log.v(TAG, "XRAY Data: $xrayData")

        // Prepare recent events
        val events = eventsRecorder.getAndResetEventHistory()
        Log.v(TAG, "Recent Events: $events")

        // Use the latest screenshot captured
        var screenshotBase64: String? = null
        val bitmap = latestScreenshot
        if (bitmap != null) {
            screenshotBase64 = encodeBitmapToBase64(bitmap)
            Log.d(TAG, "Screenshot captured and encoded")
        } else {
            Log.w(TAG, "No screenshot available")
        }

        Log.d(TAG, "User message to LLM: \"$userMessage\"")

        // Combine the general prompt with the user message and XRAY data
        val prompt =
            "User Input: \"$userMessage\"\n\n" +
                "Latest events: $events\n\n" +
                "XRAY: $xrayData"

        // Send message to LLM with image
        try {
            if (assistantId == null || threadId == null) {
                initializeAssistantAndThread()
            }
            val llmResponse =
                llmService.sendMessage(assistantId!!, threadId!!, prompt, screenshotBase64)
            Log.d(TAG, "Received LLM Response")
            Log.v(TAG, "LLM Response: $llmResponse")

            if (llmResponse != null) {
                processLLMResponse(llmResponse)
                // Check if more actions are needed
                if (llmResponse.requiresFurtherAction && echoAttempts < maxEchoAttempts) {
                    echoAttempts++
                    Log.d(TAG, "Requires further action, echo attempt $echoAttempts")
                    // Trigger echo without new user input
                    captureAndSendData(ECHO_REQUEST)
                } else {
                    echoAttempts = 0 // Reset for next user request
                    Log.d(TAG, "No further action required or max echo attempts reached")
                }
            } else {
                Log.w(TAG, "LLM Response is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message to LLM Service", e)
            // Reset assistant and thread IDs
            resetAssistantAndThread()
        }
    }

    private suspend fun processLLMResponse(llmResponse: LLMResponse) {
        Log.d(TAG, "Processing LLM Response")
        llmResponse.spoken?.let { spoken ->
            if (spoken.lowercase() != "none") {
                // Handle spoken response using TextToSpeechService
                Log.d(TAG, "Spoken: $spoken")
                scope.launch {
                    textToSpeechService.speak(spoken)
                }
            }
        }

        llmResponse.actions?.forEach { action: Action ->
            Log.d(TAG, "Performing action: $action")
            accessibilityHelper.performAction(action)

            // TODO: Make this delay reactive on the state of events coming from accessibility
            delay(1_000)
        }
    }

    private suspend fun resetAssistantAndThread() {
        Log.d(TAG, "Resetting assistant and thread IDs")
        assistantId = null
        threadId = null
        userDataRepository.setAssistantId("")
        userDataRepository.setThreadId("")
        // Re-initialize assistant and thread
        initializeAssistantAndThread()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val notificationChannelId = "ASSISTANT_SERVICE_CHANNEL"

        val channel = NotificationChannel(
            notificationChannelId,
            "Assistant Service",
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
