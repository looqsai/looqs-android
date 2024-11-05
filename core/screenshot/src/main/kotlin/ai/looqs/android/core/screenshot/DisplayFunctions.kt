package ai.looqs.android.core.screenshot

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import kotlin.math.ceil
import kotlin.math.max

data class screen(val width: Int, val height: Int)

val Context.screen: screen
    get() = screen(this.defaultDisplay.screenWidth, this.defaultDisplay.screenHeight)

val Context.screenHeight: Int
    get() = this.defaultDisplay.screenHeight

val Context.screenWidth: Int
    get() = this.defaultDisplay.screenWidth

val Display.screenHeight: Int
    get() = this.realMetrics.heightPixels

val Display.screenWidth: Int
    get() = this.realMetrics.widthPixels

val Context.dpi: Int
    get() = this.resources.displayMetrics.densityDpi

val Context.screenRect: Rect
    get() = Rect(0, 0, screenWidth, screenHeight)

/** https://stackoverflow.com/a/28965901/1446466 **/
val Context.statusBarHeight: Int
    get() {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0)
            resources.getDimensionPixelSize(resourceId)
        else
            ceil(24 * resources.displayMetrics.density).toInt()
    }

/** https://stackoverflow.com/a/20783949/1446466 **/
val Context.softButtonsBarHeight: Int
    get() {
        val display = this.defaultDisplay

        val usableHeight = display.metrics.heightPixels
        val realHeight = display.realMetrics.heightPixels

        return max(realHeight - usableHeight, 0)
    }

val Context.defaultDisplay: Display
    get() = (this.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay

val Display.metrics: DisplayMetrics
    get() {
        val metrics = DisplayMetrics()
        this.getMetrics(metrics)
        return metrics
    }

val Display.realMetrics: DisplayMetrics
    get() {
        val metrics = DisplayMetrics()
        this.getRealMetrics(metrics)
        return metrics
    }

fun Context.convertDpToPixel(dp: Float): Float =
    dp * (dpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)