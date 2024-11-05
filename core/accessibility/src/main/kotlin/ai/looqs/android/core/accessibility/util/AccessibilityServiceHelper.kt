package ai.looqs.android.core.accessibility.util

import android.view.Display
import android.view.accessibility.AccessibilityNodeInfo
import ai.looqs.android.core.actions.Action

interface AccessibilityServiceHelper {
    fun getRootNode(): AccessibilityNodeInfo?
    fun getDefaultDisplay(): Display?
    fun performAction(action: Action): Boolean
}
