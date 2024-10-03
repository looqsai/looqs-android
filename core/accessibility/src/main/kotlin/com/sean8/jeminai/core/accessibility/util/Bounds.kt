package com.sean8.jeminai.core.accessibility.util

import android.graphics.Point
import android.graphics.Rect
import android.view.MotionEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import kotlin.math.atan2

fun AccessibilityWindowInfo.getBoundsInScreen(): Rect {
    val rect = Rect()
    getBoundsInScreen(rect)
    return rect
}

fun AccessibilityNodeInfo.getBoundsInScreen(): Rect {
    val rect = Rect()
    getBoundsInScreen(rect)
    return rect
}

fun Rect.contains(point: Point) = this.contains(point.x, point.y)

fun AccessibilityNodeInfo.getShortBoundsInScreen(): String {
    return getBoundsInScreen().toShortString()
}

fun MotionEvent.point() = Point(this.x.toInt(), this.y.toInt())

fun Rect.area() = width() * height()

fun Rect.getAcuteOrRightAngleOfLongSide(): Double =
    if (this.width() > this.height()) 0.toDouble() else 90.toDouble()

fun getAcuteOrRightAngle(touch1: Point, touch2: Point): Double {
    var angle =
        Math.toDegrees(atan2((touch1.y - touch2.y).toDouble(), (touch1.x - touch2.x).toDouble()))
    if (angle < 0) angle += 360f // To full positive angle
    if (angle >= 180) angle -= 180 // To ordinary angle
    if (angle > 90) angle = 180 - angle // To Acute/Right angle
    return angle
}