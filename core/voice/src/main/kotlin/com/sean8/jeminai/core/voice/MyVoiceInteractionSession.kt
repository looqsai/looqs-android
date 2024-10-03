package com.sean8.jeminai.core.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.sean8.jeminai.core.accessibility.llm.ServerMockService

class MyVoiceInteractionSession(context: Context) : VoiceInteractionSession(context),
    RecognitionListener {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechRecognizerIntent: Intent

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "VoiceInteractionSession created")

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer.setRecognitionListener(this)

        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
    }

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        Log.d(TAG, "VoiceInteractionSession shown")
        // Start listening for voice input
        startListening()
    }

    private fun startListening() {
        speechRecognizer.startListening(speechRecognizerIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }

    // Implement RecognitionListener methods

    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "Ready for speech")
    }

    override fun onBeginningOfSpeech() {
        Log.d(TAG, "Beginning of speech")
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Not used
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        // Not used
    }

    override fun onEndOfSpeech() {
        Log.d(TAG, "End of speech")
    }

    override fun onError(error: Int) {
        Log.e(TAG, "Error: $error")
        // Handle error and perhaps restart listening
        // Optionally, you can implement retry logic here
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val speechInput = matches?.firstOrNull() ?: ""
        Log.d(TAG, "User said: $speechInput")
        handleUserInput(speechInput)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        // Not used
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        // Not used
    }

    private fun handleUserInput(speechInput: String) {
        // Send the user's voice input to ServerMockService
        val intent = Intent(context, ServerMockService::class.java).apply {
            action = ServerMockService.ACTION_PROCESS_VOICE_INPUT
            putExtra(ServerMockService.EXTRA_VOICE_INPUT, speechInput)
        }
        context.startService(intent)

        // Optionally, you can dismiss the session
        finish()
    }

    companion object {
        private const val TAG = "MyVoiceInteractionSession"
    }
}
