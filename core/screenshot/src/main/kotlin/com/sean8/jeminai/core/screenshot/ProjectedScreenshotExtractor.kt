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
import javax.inject.Inject

@ServiceScoped
class ProjectedScreenshotExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "ProjectedScreenshotExtractor"

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null

    private var imageReader: ImageReader? = null
    private var imageReaderHandlerThread: HandlerThread? = null
    private var imageReaderHandler: Handler? = null

    private var screenshotCallback: ((Bitmap?) -> Unit)? = null

    fun start(
        mediaProjection: MediaProjection,
        scope: CoroutineScope,
        callback: (Bitmap?) -> Unit
    ) {
        this.mediaProjection = mediaProjection
        this.screenshotCallback = callback

        // Initialize ImageReader and Handler
        imageReaderHandlerThread = HandlerThread("ImageReaderHandler").apply { start() }
        imageReaderHandler = Handler(imageReaderHandlerThread!!.looper)

        val metrics = context.resources.displayMetrics

        imageReader = ImageReader.newInstance(
            metrics.widthPixels,
            metrics.heightPixels,
            PixelFormat.RGBA_8888,
            2,
        )

        imageReader?.setOnImageAvailableListener(
            { reader ->
                processImage(reader)
            },
            imageReaderHandler,
        )

        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenCapture",
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC or DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null,
        )
    }

    private fun processImage(reader: ImageReader) {
        var image: Image? = null
        try {
            image = reader.acquireLatestImage()
            if (image != null) {
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
                bitmap.recycle() // Recycle the original bitmap

                // Invoke the callback with the bitmap
                screenshotCallback?.invoke(croppedBitmap)
                Log.v(TAG, "Screenshot captured")
            } else {
                Log.v(TAG, "Image is null")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to process image: ${e.message}")
        } finally {
            image?.close()
        }
    }

    fun stop() {
        virtualDisplay?.release()
        virtualDisplay = null

        mediaProjection?.stop()
        mediaProjection = null

        imageReader?.close()
        imageReader = null

        imageReaderHandlerThread?.quitSafely()
        imageReaderHandlerThread = null
        imageReaderHandler = null
    }
}
