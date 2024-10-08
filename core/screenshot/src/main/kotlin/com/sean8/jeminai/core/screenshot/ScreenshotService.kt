package com.sean8.jeminai.core.screenshot

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ScreenshotService : Service() {

    private val TAG = "ScreenshotService"

    @Inject
    lateinit var mediaProjectionManager: MediaProjectionManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()

        val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_CANCELED)
            ?: Activity.RESULT_CANCELED
        val data = intent?.getParcelableExtra<Intent>("data")

        if (resultCode == Activity.RESULT_OK && data != null) {
            val mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
            if (mediaProjection != null) {
                // Store the MediaProjection instance
                ProjectionState.setMediaProjection(mediaProjection)
                Log.d(TAG, "MediaProjection obtained and stored.")
            } else {
                Log.e(TAG, "MediaProjection is null.")
                stopSelf()
            }
        } else {
            Log.e(TAG, "Invalid result code or data.")
            stopSelf()
        }

        // Stop the service since its job is done
        stopSelf()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "ScreenshotService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundService() {
        val channelId = "screenshot_service_channel"
        val channelName = "Screenshot Service"

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId, channelName, NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Screen Capture Active")
            .setContentText("Preparing to capture your screen")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()

        startForeground(1, notification)
    }
}
