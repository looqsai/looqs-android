package ai.looqs.android.core.accessibility

import android.view.Display
import android.view.accessibility.AccessibilityNodeInfo
import ai.looqs.android.core.accessibility.util.AccessibilityServiceHelper
import ai.looqs.android.core.accessibility.util.defaultDisplay
import ai.looqs.android.core.actions.Action
import ai.looqs.android.core.actions.Action.DeviceKeyAction
import ai.looqs.android.core.actions.Action.LongClickAction
import ai.looqs.android.core.actions.Action.ScrollAction
import ai.looqs.android.core.actions.Action.SwipeAction
import ai.looqs.android.core.actions.Action.TapAction
import ai.looqs.android.core.actions.Action.TextAction
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

@ServiceScoped
class AccessibilityServiceHelperImpl @Inject constructor(
    private val accessibilityService: ai.looqs.android.core.accessibility.AppAccessibilityService?,
) : AccessibilityServiceHelper {

    override fun getRootNode(): AccessibilityNodeInfo? {
        return accessibilityService?.rootInActiveWindow
    }

    override fun getDefaultDisplay(): Display? {
        return accessibilityService?.defaultDisplay
    }

    override fun performAction(action: Action): Boolean {
        val service = accessibilityService ?: return false

        return when (action) {
            is TapAction -> service.performTapAction(action)
            is LongClickAction -> service.performLongClickAction(action)
            is SwipeAction -> service.performSwipeAction(action)
            is ScrollAction -> service.performScrollAction(action)
            is DeviceKeyAction -> service.performDeviceKeyAction(action)
            is TextAction -> service.performTextAction(action)
            else -> false
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
