package com.sean8.jeminai.core.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class AudioRecordingService : Service() {
    private val TAG = "AudioRecordingService"
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    @Inject
    lateinit var audioRecorderFactory: AudioRecorderFactory

    @Inject
    lateinit var voiceTransmitter: VoiceTransmitter

    companion object {
        const val ACTION_START_RECORDING = "com.sean8.jeminai.ACTION_START_RECORDING"
        const val EXTRA_RECORDING_DURATION = "extra_recording_duration"
        const val DEFAULT_RECORDING_DURATION = 10 // in seconds
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        startForegroundService()

        Log.d(TAG, "VoiceTransmitter instance: $voiceTransmitter")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            handleIntent(intent)
        }
        return START_NOT_STICKY
    }

    private suspend fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_START_RECORDING -> {
                val duration =
                    intent.getIntExtra(EXTRA_RECORDING_DURATION, DEFAULT_RECORDING_DURATION)

                Log.d(TAG, "Starting recording for $duration seconds")

                // Use internal cache directory to store the recording
                val outputFilePath =
                    "${cacheDir.absolutePath}/recording_${System.currentTimeMillis()}.m4a"
                Log.d(TAG, "Output file: $outputFilePath")

                audioRecorderFactory.createAudioRecorder().let { recorder ->
                    recorder.startRecording(outputFilePath)
                    delay(duration * 1000L)
                    recorder.stopRecording()

                    voiceTransmitter.postNewRecording(File(outputFilePath))
                }
            }

            else -> {
                Log.d(TAG, "Unknown action received")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")

        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundService() {
        val channelId = "audio_recording_service_channel"
        val channelName = "Audio Recording Service"

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId, channelName, NotificationManager.IMPORTANCE_LOW,
        )
        notificationManager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Recording Audio")
            .setContentText("Recording in progress")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }
}
