package com.sean8.jeminai.core.util

import android.graphics.Bitmap
import android.view.Display
import android.view.accessibility.AccessibilityNodeInfo
import com.sean8.jeminai.core.actions.Action

interface AccessibilityHelper {
    fun getRootNode(): AccessibilityNodeInfo?
    fun getDefaultDisplay(): Display?
    fun captureScreenshot(): Bitmap?
    fun speak(text: String)
    fun performAction(action: Action)
}
