package com.sean8.jeminai.core.accessibility

import android.view.Display
import android.view.accessibility.AccessibilityNodeInfo
import com.sean8.jeminai.core.accessibility.util.AccessibilityServiceHelper
import com.sean8.jeminai.core.accessibility.util.defaultDisplay
import com.sean8.jeminai.core.actions.Action
import com.sean8.jeminai.core.actions.Action.DeviceKeyAction
import com.sean8.jeminai.core.actions.Action.LongClickAction
import com.sean8.jeminai.core.actions.Action.ScrollAction
import com.sean8.jeminai.core.actions.Action.SwipeAction
import com.sean8.jeminai.core.actions.Action.TapAction
import com.sean8.jeminai.core.actions.Action.TextAction
import com.sean8.jeminai.core.actions.Action.TextEditorAction
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

    override fun performAction(action: Action) {
        accessibilityService?.let { service ->
            when (action) {
                is TapAction -> service.performTapAction(action)
                is LongClickAction -> service.performLongClickAction(action)
                is SwipeAction -> service.performSwipeAction(action)
                is ScrollAction -> service.performScrollAction(action)
                is DeviceKeyAction -> service.performDeviceKeyAction(action)
                is TextAction -> service.performTextAction(action)
                is TextEditorAction -> service.performTextEditorAction(action)
            }
        }
    }

    fun findNodeByPath(path: List<Int>): AccessibilityNodeInfo? {
        var currentNode = accessibilityService?.rootInActiveWindow
        for (index in path) {
            currentNode = currentNode?.getChild(index)
        }
        return currentNode
    }
}
