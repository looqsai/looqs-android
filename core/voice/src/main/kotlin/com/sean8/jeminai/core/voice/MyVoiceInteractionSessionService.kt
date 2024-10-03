package com.sean8.jeminai.core.voice

import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.util.Log

class MyVoiceInteractionSessionService : VoiceInteractionSessionService() {

    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        Log.d(TAG, "onNewSession called")
        return MyVoiceInteractionSession(this)
    }

    companion object {
        private const val TAG = "MyVoiceInteractionSessionService"
    }
}