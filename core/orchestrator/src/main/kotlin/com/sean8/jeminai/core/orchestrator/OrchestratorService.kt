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
import com.sean8.jeminai.core.llm.PromptConstants.formatCommand
import com.sean8.jeminai.core.llm.TextToSpeechService
import com.sean8.jeminai.core.llm.models.LLMResponse
import com.sean8.jeminai.core.reflector.MessageType
import com.sean8.jeminai.core.reflector.MessageType.AI
import com.sean8.jeminai.core.reflector.MessageType.STOP_TYPING
import com.sean8.jeminai.core.reflector.MessageType.TYPING
import com.sean8.jeminai.core.reflector.MessageType.USER
import com.sean8.jeminai.core.reflector.Metadata
import com.sean8.jeminai.core.reflector.ReflectorController
import com.sean8.jeminai.core.reflector.ReflectorMessage
import com.sean8.jeminai.core.screenshot.ProjectedScreenshotExtractor
import com.sean8.jeminai.core.screenshot.ProjectionState
import com.sean8.jeminai.core.voice.AudioRecordingService
import com.sean8.jeminai.core.voice.VoiceTransmitter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
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

    @Inject
    lateinit var reflectorController: ReflectorController

    private lateinit var accessibilityHelper: AccessibilityServiceHelper

    private var echoAttempts = 0
    private val maxEchoAttempts = 5

    @Inject
    @ApplicationScope
    lateinit var scope: CoroutineScope

    private var latestScreenshot: Bitmap? = null

    private var assistantId: String? = null
    private var threadId: String? = null

    // Define constants for the action and extra key
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val ACTION_DEMO = "com.sean8.jeminai.ACTION_DEMO"
        private const val EXTRA_DEMO_VOICE_INPUT_TEXT = "DEMO_VOICE_INPUT_TEXT"
    }

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

        // Connect to the WebSocket server
        val reflectorWsHost = "ws://indirectly-closing-airedale.ngrok-free.app"
        reflectorController.connect(reflectorWsHost)

        // Handle incoming messages
        // TODO: is mapLatest the correct function to call?
        scope.launch {
            reflectorController.incomingMessages.mapLatest { message ->
                when (message.type) {
                    USER -> {
                        // Handle incoming user message
                        scope.launch {
                            captureAndSendData(message.content)
                        }
                    }

                    AI -> Log.d(TAG, "Received AI message: ${message.content}")
                    TYPING, STOP_TYPING -> Log.v(TAG, "Received $message message")
                }
            }
        }

        // Start collecting voice inputs and media projections
        scope.launch {
            collectVoiceInputs()
            collectMediaProjections()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called with intent: $intent")
        if (intent != null) {
            val action = intent.action
            if (action == ACTION_DEMO) {
                val demoVoiceInputText = intent.getStringExtra(EXTRA_DEMO_VOICE_INPUT_TEXT)
                if (!demoVoiceInputText.isNullOrEmpty()) {
                    Log.d(TAG, "Received demo voice input text: $demoVoiceInputText")
                    val stopAudioIntent = Intent(this, AudioRecordingService::class.java).apply {
                        this.action = AudioRecordingService.ACTION_DISMISS_RECORDING
                    }
                    // Process the injected voice input text as if it was transcribed from voice
                    scope.launch {
                        startService(stopAudioIntent)
                        captureAndSendData(userMessage = demoVoiceInputText)
                    }
                } else {
                    Log.e(TAG, "No voice input text provided in intent")
                }
            }
        }
        return START_STICKY
    }

    private suspend fun initializeAssistantAndThread() {
        // Load assistantId and threadId from UserData
        val userData = userDataRepository.userData.first()
        assistantId = userData.assistantId
        threadId = userData.threadId

        if (true) {
            Log.w(TAG, "SKIPPING PERSISTENT ASSISTANT AND THREADS FOR TEMP DEBUG PURPOSES ")
            assistantId = null
            threadId = null
        }

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
        reflectorController.disconnect()
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
        var screenshotFile: File? = null
        val bitmap = latestScreenshot
        if (bitmap != null) {
            // Save the bitmap to a temporary file
            try {
                screenshotFile = File(cacheDir, "screenshot_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(screenshotFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                outputStream.flush()
                outputStream.close()
                Log.d(TAG, "Screenshot captured and saved to file: ${screenshotFile.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving screenshot to file", e)
                screenshotFile = null
            }
        } else {
            Log.w(TAG, "No screenshot available")
        }

        Log.d(TAG, "User message to LLM: \"$userMessage\"")

        // Send user message to WebSocket
        reflectorController.sendMessage(
            ReflectorMessage(
                type = MessageType.USER,
                content = userMessage,
            ),
        )

        // Send typing indicator (LLM is processing)
        sendTypingIndicator(isTyping = true)

        // Combine the general prompt with the user message and XRAY data
        val prompt =
            "User Input: \"$userMessage\"\n\n" +
                "XRAY: $xrayData"

        // Send message to LLM with image
        try {
            if (assistantId == null || threadId == null) {
                initializeAssistantAndThread()
            }
            val llmResponse =
                llmService.sendMessage(assistantId!!, threadId!!, prompt, screenshotFile)
            Log.d(TAG, "Received LLM Response")
            Log.v(TAG, "LLM Response: $llmResponse")

            if (llmResponse != null) {
                val processSucceeded = processLLMResponse(llmResponse)

                // There's an issue with the LLM response sometimes returning requiresFurtherAction True, but no actual actions.
                // So proceed with actual actions if they exist
                val actuallyRequiresFurtherActions = llmResponse.actions.isNotEmpty()

                // Check if more actions are needed
                if ((!processSucceeded || actuallyRequiresFurtherActions) && echoAttempts < maxEchoAttempts) {
                    echoAttempts++
                    Log.d(TAG, "Requires further action, echo attempt $echoAttempts")
                    captureAndSendData(formatCommand(ECHO_REQUEST))
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
        } finally {
            // Clean up the temporary screenshot file
            screenshotFile?.delete()
        }
    }

    private suspend fun processLLMResponse(llmResponse: LLMResponse): Boolean {
        Log.d(TAG, "Processing LLM Response")

        // Stop typing indicator
        sendTypingIndicator(isTyping = false)

        llmResponse.spoken?.let { spoken ->
            if (spoken.lowercase() != "none") {
                // Handle spoken response
                Log.d(TAG, "Spoken: $spoken")
                scope.launch {
                    textToSpeechService.speak(spoken)
                }

                // Send AI response to WebSocket
                reflectorController.sendMessage(
                    ReflectorMessage(
                        type = MessageType.AI,
                        content = spoken,
                        metadata = Metadata(
                            actions = llmResponse.actions.map { it.type },
                            requiresAdditional = llmResponse.requiresFurtherAction,
                            model = llmResponse.toString(), // TODO: Entire response from llm as JSON
                            imageId = null, // TODO: add if image was sent
                        ),
                    ),
                )
            }
        }


        llmResponse.actions?.forEach { action: Action ->
            Log.d(TAG, "Performing action: $action")
            if (!accessibilityHelper.performAction(action)) {
                return false
            }

            // TODO: Make this delay reactive on the state of events coming from accessibility
            delay(1_500)
        }

        return true
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

    private fun sendTypingIndicator(isTyping: Boolean) {
        val messageType = if (isTyping) MessageType.TYPING else MessageType.STOP_TYPING
        reflectorController.sendMessage(
            ReflectorMessage(
                type = messageType,
                content = "",
                metadata = null,
            ),
        )
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
}
