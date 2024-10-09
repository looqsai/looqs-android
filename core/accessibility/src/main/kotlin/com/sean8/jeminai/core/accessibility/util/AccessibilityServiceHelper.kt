package com.sean8.jeminai.core.accessibility.util

import android.view.Display
import android.view.accessibility.AccessibilityNodeInfo
import com.sean8.jeminai.core.actions.Action

interface AccessibilityServiceHelper {
    fun getRootNode(): AccessibilityNodeInfo?
    fun getDefaultDisplay(): Display?
    fun performAction(action: Action)
}
