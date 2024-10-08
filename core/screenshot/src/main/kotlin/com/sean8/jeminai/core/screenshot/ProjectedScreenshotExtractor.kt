package com.sean8.jeminai.core.screenshot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@ServiceScoped
class ProjectedScreenshotExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "ProjectedScreenshotExtractor"

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null

    private val imageReader: ImageReader
    private val imageReaderHandlerThread: HandlerThread
    private val imageReaderHandler: Handler

    private var captureJob: Job? = null

    private var screenshotCallback: ((Bitmap?) -> Unit)? = null

    init {
        imageReaderHandlerThread = HandlerThread("ImageReaderHandler").apply { start() }
        imageReaderHandler = Handler(imageReaderHandlerThread.looper)

        imageReader = ImageReader.newInstance(
            context.resources.displayMetrics.widthPixels,
            context.resources.displayMetrics.heightPixels,
            PixelFormat.RGBA_8888, 2
        )
    }

    fun start(
        mediaProjection: MediaProjection,
        scope: CoroutineScope,
        callback: (Bitmap?) -> Unit
    ) {
        this.mediaProjection = mediaProjection
        this.screenshotCallback = callback

        if (virtualDisplay == null) {
            virtualDisplay = mediaProjection.createVirtualDisplay(
                "ScreenCapture",
                context.resources.displayMetrics.widthPixels,
                context.resources.displayMetrics.heightPixels,
                context.resources.displayMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                imageReader.surface, null, null
            )
        }

        // Start capturing screenshots at fixed intervals
        captureJob = scope.launch(Dispatchers.IO) {
            while (true) {
                captureScreenshot()
                delay(1000) // Delay for 1 second
            }
        }
    }

    private fun captureScreenshot() {
        val image: Image? = imageReader.acquireLatestImage()
        if (image != null) {
            try {
                val width = image.width
                val height = image.height
                val plane = image.planes[0]
                val buffer = plane.buffer
                val pixelStride = plane.pixelStride
                val rowStride = plane.rowStride
                val rowPadding = rowStride - pixelStride * width

                val bitmap = Bitmap.createBitmap(
                    width + rowPadding / pixelStride,
                    height, Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)

                val croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)

                // Invoke the callback with the bitmap
                screenshotCallback?.invoke(croppedBitmap)
                Log.v(TAG, "Screenshot captured")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to process image: ${e.message}")
            } finally {
                image.close()
            }
        } else {
            Log.v(TAG, "Image is null, will use latest")
        }
    }

    fun stop() {
        captureJob?.cancel()
        captureJob = null

        virtualDisplay?.release()
        virtualDisplay = null

        mediaProjection?.stop()
        mediaProjection = null

        imageReader.close()
        imageReaderHandlerThread.quitSafely()
    }
}
