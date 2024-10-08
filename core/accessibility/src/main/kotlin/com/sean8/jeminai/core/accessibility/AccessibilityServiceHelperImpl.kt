package com.sean8.jeminai.core.accessibility

import android.view.Display
import android.view.accessibility.AccessibilityNodeInfo
import com.sean8.jeminai.core.accessibility.util.AccessibilityServiceHelper
import com.sean8.jeminai.core.actions.Action
import com.sean8.jeminai.core.util.defaultDisplay
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

@ServiceScoped
class AccessibilityServiceHelperImpl @Inject constructor(
    private val accessibilityService: AppAccessibilityService?,
) : AccessibilityServiceHelper {

    override fun getRootNode(): AccessibilityNodeInfo? {
        return accessibilityService?.rootInActiveWindow
    }

    override fun getDefaultDisplay(): Display? {
        return accessibilityService?.defaultDisplay
    }

    override fun speak(text: String) {
        accessibilityService?.speak(text)
    }

    override fun performAction(action: Action) {
        accessibilityService?.let { service ->
            when (action) {
                is Action.TapAction -> service.performTapAction(action)
                is Action.LongClickAction -> service.performLongClickAction(action)
                is Action.DeviceKeyAction -> service.performDeviceKeyAction(action)
                is Action.TextAction -> service.performTextAction(action)
                is Action.TextEditorAction -> service.performTextEditorAction(action)
                is Action.SwipeAction -> service.performSwipeAction(action)
                is Action.ScrollAction -> service.performScrollAction(action)
                else -> {
                    // Handle unsupported actions
                }
            }
        }
    }
}
