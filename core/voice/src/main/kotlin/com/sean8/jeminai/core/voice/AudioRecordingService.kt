package com.sean8.jeminai.core.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sean8.jeminai.core.util.ServerMockService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

@AndroidEntryPoint
class AudioRecordingService : Service() {

    private val TAG = "AudioRecordingService"
    private var mediaRecorder: MediaRecorder? = null
    private var outputFilePath: String? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

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
                startRecording()
                delay(duration * 1000L)
                stopRecording()
                sendAudioToLLMService()
            }

            else -> {
                Log.d(TAG, "Unknown action received")
            }
        }
    }

    private fun startRecording() {
        // Use internal cache directory to store the recording
        outputFilePath = "${cacheDir.absolutePath}/recording_${System.currentTimeMillis()}.m4a"
        Log.d(TAG, "Output file: $outputFilePath")

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFilePath)
            try {
                prepare()
                start()
                Log.d(TAG, "Recording started")
            } catch (e: IOException) {
                Log.e(TAG, "Failed to start recording", e)
            }
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            Log.d(TAG, "Recording stopped")
        } catch (e: RuntimeException) {
            Log.e(TAG, "Failed to stop recording", e)
        }
    }

    private fun sendAudioToLLMService() {
        Log.d(TAG, "Sending audio to LLM Service")
        val intent = Intent(this, ServerMockService::class.java).apply {
            action = ServerMockService.ACTION_PROCESS_AUDIO_INPUT
            putExtra(ServerMockService.EXTRA_AUDIO_FILE_PATH, outputFilePath)
        }
        startService(intent)

        // TODO: Delete audio later?
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        mediaRecorder?.release()
        mediaRecorder = null
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
