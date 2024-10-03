package com.sean8.jeminai.core.voice

import android.service.voice.VoiceInteractionService
import android.util.Log

class MyVoiceInteractionService : VoiceInteractionService() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "VoiceInteractionService created")
    }

    override fun onReady() {
        super.onReady()
        Log.d(TAG, "VoiceInteractionService is ready")
    }

    companion object {
        private const val TAG = "MyVoiceInteractionService"
    }
}
