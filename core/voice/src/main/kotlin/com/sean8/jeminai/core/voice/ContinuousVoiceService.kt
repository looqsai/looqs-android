package com.sean8.jeminai.core.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sean8.jeminai.util.ServerMockService
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class ContinuousVoiceService : Service() {

    private val TAG = "ContinuousVoiceService"
    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var speechRecognizerIntent: Intent

    private val triggerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_START_LISTENING) {
                Log.d(TAG, "Received trigger to start listening")
                startListening()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()

        // Register the receiver
        val filter = IntentFilter(ACTION_START_LISTENING)
        registerReceiver(triggerReceiver, filter)
    }

    private fun startForegroundService() {
        val channelId = "continuous_voice_service_channel"
        val channelName = "Continuous Voice Service"

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId, channelName, NotificationManager.IMPORTANCE_LOW,
        )
        notificationManager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Voice Assistant")
            .setContentText("Ready to receive voice commands")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()

        startForeground(1, notification)
    }

    fun startListening() {
        Log.d(TAG, "Initializing SpeechRecognizer")
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(
                object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d(TAG, "onReadyForSpeech")
                    }

                    override fun onBeginningOfSpeech() {
                        Log.d(TAG, "onBeginningOfSpeech")
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        Log.d(TAG, "onRmsChanged: $rmsdB")
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {
                        Log.d(TAG, "onBufferReceived")
                    }

                    override fun onEndOfSpeech() {
                        Log.d(TAG, "onEndOfSpeech")
                    }

                    override fun onError(error: Int) {
                        Log.e(TAG, "Speech recognition error: $error")
                        // Map error codes to messages for clarity
                        val errorMessage = getErrorText(error)
                        Log.e(TAG, "Error: $errorMessage")
                        // Optionally restart listening
                        // restartListening()
                    }

                    override fun onResults(results: Bundle?) {
                        Log.d(TAG, "onResults")
                        val matches =
                            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.joinToString(separator = " ") ?: ""
                        Log.d(TAG, "Recognized Text: $text")
                        // Send text to LLM service
                        sendToLLMService(text)
                        // Optionally restart listening
                        // restartListening()
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        Log.d(TAG, "onPartialResults")
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {
                        Log.d(TAG, "onEvent: $eventType")
                    }
                },
            )

            speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                )
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000)
                putExtra(
                    RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                    5000,
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            }

            Log.d(TAG, "Starting speech recognition")
            speechRecognizer?.startListening(speechRecognizerIntent)
        } else {
            Log.e(TAG, "Speech recognition not available on this device.")
        }
    }

    private fun restartListening() {
        Log.d(TAG, "Restarting speech recognition")
        speechRecognizer?.stopListening()
        speechRecognizer?.cancel()
        speechRecognizer?.startListening(speechRecognizerIntent)
    }

    private fun sendToLLMService(text: String) {
        Log.d(TAG, "Sending text to LLM Service: $text")
        // Start ServerMockService and pass the voice input
        val intent = Intent(this, ServerMockService::class.java).apply {
            action = ServerMockService.ACTION_PROCESS_VOICE_INPUT
            putExtra(ServerMockService.EXTRA_VOICE_INPUT, text)
        }
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        speechRecognizer?.destroy()
        speechRecognizer = null

        unregisterReceiver(triggerReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
    }

    companion object {
        const val ACTION_START_LISTENING = "com.sean8.jeminai.ACTION_START_LISTENING"
    }
}
