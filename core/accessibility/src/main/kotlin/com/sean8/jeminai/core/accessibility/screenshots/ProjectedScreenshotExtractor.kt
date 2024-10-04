package com.sean8.jeminai.core.accessibility.screenshots

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

/**
 * Once a user approved the projection permission, this class will help extract screenshots from the current screen.
 */
@ServiceScoped
class ProjectedScreenshotExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaProjectionManager: MediaProjectionManager,
) {
    val TAG = "ProjectedScreenshotExtractor"

    private var resultData: Intent? = null

    private var mediaProjection: MediaProjection? = null

    private var virtualDisplay: VirtualDisplay? = null

    private val imageReader: ImageReader = ImageReader.newInstance(
        context.resources.displayMetrics.widthPixels,
        context.resources.displayMetrics.heightPixels,
        PixelFormat.RGBA_8888, 2,
    )

    fun start() {
        resultData = ProjectionState.projectionToken.value ?: return
        mediaProjection = mediaProjectionManager.getMediaProjection(RESULT_OK, resultData!!)

        if (virtualDisplay == null) {
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "ScreenCapture",
                context.resources.displayMetrics.widthPixels,
                context.resources.displayMetrics.heightPixels,
                context.resources.displayMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                imageReader.surface, null, null,
            )
        }
    }

    fun acquireScreenshot(): Bitmap? {
        val image: Image = try {
            imageReader.acquireLatestImage() ?: return null
        } catch (e: Exception) {
            Log.w(TAG, "No screenshot taken: " + e.message)
            return null
        }

        try {
            val width = image.width
            val height = image.height
            val plane = image.planes[0]
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            val rowPadding = rowStride - pixelStride * width

            return Bitmap.createBitmap(
                width + rowPadding / pixelStride,
                height, Bitmap.Config.ARGB_8888,
            ).apply {
                val buffer = plane.buffer
                this.copyPixelsFromBuffer(buffer)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to transform screenshot's Image to Bitmap", e)
            return null
        } finally {
            image.close()
        }
    }

    fun stop() {
        virtualDisplay?.release()
        virtualDisplay = null
        mediaProjection?.stop()
        imageReader.close()
    }
}