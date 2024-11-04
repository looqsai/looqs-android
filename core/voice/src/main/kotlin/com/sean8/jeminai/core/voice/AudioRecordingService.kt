package com.sean8.jeminai.core.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sean8.jeminai.core.reflector.MessageType
import com.sean8.jeminai.core.reflector.ReflectorController
import com.sean8.jeminai.core.reflector.ReflectorMessage
import com.sean8.jeminai.core.voice.RecordingState.Error
import com.sean8.jeminai.core.voice.RecordingState.Finished
import com.sean8.jeminai.core.voice.RecordingState.Idle
import com.sean8.jeminai.core.voice.RecordingState.Started
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

    @Inject
    lateinit var reflectorController: ReflectorController

    private lateinit var recorder: AudioRecorder

    var outputFilePath: String? = null

    companion object {
        const val ACTION_START_RECORDING = "com.sean8.jeminai.ACTION_START_RECORDING"
        const val ACTION_DISMISS_RECORDING = "com.sean8.jeminai.ACTION_DISMISS_RECORDING"
        const val EXTRA_RECORDING_DURATION = "extra_recording_duration"
        const val DEFAULT_RECORDING_DURATION = 5 // in seconds
        private const val NOTIFICATION_ID = 2
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        recorder = audioRecorderFactory.createAudioRecorder()
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
            ACTION_START_RECORDING -> handleStartRecording(intent)
            ACTION_DISMISS_RECORDING -> handleDismissRecording()
            else -> Log.d(TAG, "Unknown action received")
        }
    }

    private suspend fun handleStartRecording(intent: Intent) {
        when (recorder.state.value) {
            is Started -> {
                Log.d(TAG, "Finishing an already started recording")
                stopAndPostRecording()
            }

            is Error, Finished, Idle -> {
                val duration =
                    intent.getIntExtra(EXTRA_RECORDING_DURATION, DEFAULT_RECORDING_DURATION)

                Log.d(TAG, "Starting recording for $duration seconds")

                // Use internal cache directory to store the recording
                outputFilePath =
                    "${cacheDir.absolutePath}/recording_${System.currentTimeMillis()}.m4a"
                Log.d(TAG, "Output file: $outputFilePath")

                playStartingSound()

                startRecording(duration)
            }
        }
    }

    private fun playStartingSound() {
        val mediaPlayer =
            MediaPlayer.create(this, R.raw.core_voice_chime_free_royalty_pixabay_click_effect)
        mediaPlayer.setOnCompletionListener { mp ->
            mp.release()
        }
        mediaPlayer.start()
    }

    private fun handleDismissRecording() {
        Log.d(TAG, "Stopping recording service")
        recorder.stopRecording()
        sendRecordingStatusToWebSocket(started = false)
        stopSelf()
    }

    private suspend fun startRecording(duration: Int) {
        recorder.startRecording(outputFilePath!!)
        sendRecordingStatusToWebSocket(started = true)

        delay(duration * 1000L)

        if (recorder.state.value == Started) {
            stopAndPostRecording()
        }
    }

    private fun stopAndPostRecording() {
        recorder.stopRecording()
        sendRecordingStatusToWebSocket(started = false)
        voiceTransmitter.postNewRecording(File(outputFilePath!!))
    }

    private fun sendRecordingStatusToWebSocket(started: Boolean) {
        val messageType =
            if (started) MessageType.VOICE_RECORDING_START else MessageType.VOICE_RECORDING_STOP
        reflectorController.sendMessage(
            ReflectorMessage(
                type = messageType,
                content = "",
                isVisible = true,
            ),
        )
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
