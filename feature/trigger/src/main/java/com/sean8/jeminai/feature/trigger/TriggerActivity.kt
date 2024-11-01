package com.sean8.jeminai.feature.trigger

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.sean8.jeminai.core.voice.AudioRecordingService

class TriggerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start the service with the desired action
        val intent = Intent(this, AudioRecordingService::class.java).apply {
            action = "com.sean8.jeminai.ACTION_START_RECORDING"
        }
        startService(intent)

        // Finish the activity to prevent any UI from showing
        finish()
    }
}