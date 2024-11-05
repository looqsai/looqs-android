package ai.looqs.android.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import android.widget.EditText
import androidx.core.view.accessibility.AccessibilityEventCompat.TYPE_WINDOWS_CHANGED
import androidx.core.view.accessibility.AccessibilityEventCompat.TYPE_WINDOW_CONTENT_CHANGED
import ai.looqs.android.core.accessibility.util.AccessibilityNodeInfoXmlDumper
import ai.looqs.android.core.accessibility.util.TreeNode
import ai.looqs.android.core.accessibility.util.defaultDisplay
import ai.looqs.android.core.accessibility.util.populateChildren
import ai.looqs.android.core.actions.Action
import ai.looqs.android.core.actions.Action.DeviceKeyAction
import ai.looqs.android.core.actions.Action.LongClickAction
import ai.looqs.android.core.actions.Action.Point
import ai.looqs.android.core.actions.Action.ScrollAction
import ai.looqs.android.core.actions.Action.SwipeAction
import ai.looqs.android.core.actions.Action.TapAction
import ai.looqs.android.core.util.getBoundsInScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppAccessibilityService : AccessibilityService() {
    private val TAG = "AppAccessibilityService"

    @Inject
    lateinit var eventBus: ai.looqs.android.core.accessibility.AccessibilityEventBus

    @Inject
    lateinit var accessibilityStatusManager: ai.looqs.android.core.accessibility.AccessibilityStatusManager

    private val serviceScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        ai.looqs.android.core.accessibility.AppAccessibilityService.Companion.instance = this
    }

    override fun onDestroy() {
        ai.looqs.android.core.accessibility.AppAccessibilityService.Companion.instance = null
        super.onDestroy()

        accessibilityStatusManager.setServiceEnabled(false)
    }

    override fun onServiceConnected() {
        Log.d(TAG, "onServiceConnected, info: $serviceInfo")

        accessibilityStatusManager.setServiceEnabled(true)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val source: AccessibilityNodeInfo? = event?.source

        if (event?.eventType == TYPE_WINDOW_CONTENT_CHANGED) {
            return
        }

        Log.v(TAG, "Event received: ${event.toString()} , Source Node found? $source")

        event?.let {
            emitAccessibilityEvent(it)
        }

        if (event?.eventType == TYPE_WINDOWS_CHANGED) {
            Log.d(TAG, "Window change content: ${event.windowChanges}")
        }

        rootInActiveWindow
    }

    override fun onInterrupt() {
        Log.d(TAG, "onInterrupt")
    }

    private fun emitAccessibilityEvent(event: AccessibilityEvent) {
        serviceScope.launch {
            eventBus.emit(event)
        }
    }

    private fun getActiveWindowTree(): TreeNode<AccessibilityNodeInfo>? {
        val timeBeforeBuild = System.currentTimeMillis()
        val rootTree = TreeNode(rootInActiveWindow)

        return if (rootTree.populateChildren()) {
            // TODO: need log?
            if (false) Log.d(
                TAG,
                "Snapshot 🗂 (" +
                    "time=${
                        (System.currentTimeMillis() - timeBeforeBuild).toString().padEnd(3)
                    }, " +
                    "nodes=${rootTree.countNodes()})",
            )

            rootTree
        } else {
            Log.i(TAG, "Failed to get a full snapshot of tree ⚠️🗂!")
            null
        }
    }

    private fun isPackageOnFront(appPackage: String) =
        rootInActiveWindow.packageName?.toString() == appPackage

    private fun TreeNode<AccessibilityNodeInfo>.toXml(printToFile: Boolean = true): String =
        AccessibilityNodeInfoXmlDumper.getXml(
            this,
            this@AppAccessibilityService.defaultDisplay,
        )

    private fun getKeyboardWindow() =
        windows.firstOrNull { it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD }

    fun getKeyboardSize() =
        getKeyboardWindow()?.getBoundsInScreen()?.height() ?: 0

    // TODO: Can root be null?
    private val root: AccessibilityNodeInfo?
        get() = rootInActiveWindow

    fun AccessibilityService.getWindowManager() =
        getSystemService(Context.WINDOW_SERVICE) as WindowManager?

    /** Gets the root in active window and populates it **/
    fun populatedRoot() =
        TreeNode(this.root!!).apply { populateChildren() }

    /** Returns failure reason if failed **/
    suspend fun launchAppAndWaitForIt(packageName: String): String? {
        val wasAppOnFront = this.isPackageOnFront(packageName)

        val startIntent = this.packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        } ?: return "Did not find package name $packageName"
        this.startActivity(startIntent)

        // If app was on front already, than we need to wait for a reset of the app
        // TODO: this can be nicer with checking the acce
        if (wasAppOnFront) delay(4000)

        repeat(20) {
            Log.d("Debug", "Sean - wait package")
            if (this.isPackageOnFront(packageName)) return null
            delay(1000)
        }

        return "Timeout while trying to launch package $packageName"
    }

    fun performTapAction(action: TapAction): Boolean {
        val path = Path().apply {
            moveTo(action.x, action.y)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        return dispatchGesture(gesture, null, null)
    }

    fun performLongClickAction(action: LongClickAction): Boolean {
        val path = Path().apply {
            moveTo(action.x, action.y)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 1000))
            .build()
        return dispatchGesture(gesture, null, null)
    }

    fun performSwipeAction(action: SwipeAction) =
        performGestureAction(action.start, action.end, action.swipeDuration)

    fun performScrollAction(action: ScrollAction) =
        performGestureAction(action.start, action.end, action.swipeDuration)

    private fun performGestureAction(
        start: Point,
        end: Point,
        duration: Long,
    ): Boolean {
        val path = Path().apply {
            moveTo(start.x, start.y)
            lineTo(end.x, end.y)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(
                    path,
                    0,
                    duration,
                ),
            )
            .build()
        return dispatchGesture(gesture, null, null)
    }

    fun performDeviceKeyAction(action: DeviceKeyAction): Boolean {
        return performGlobalAction(action.serverAction)
    }

    fun performTextAction(action: Action.TextAction): Boolean {
        val node = findNodeAtPosition(action.x.toInt(), action.y.toInt())
        if (node != null) {
            if (node.isEditable) {
                val arguments = Bundle().apply {
                    putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        action.text,
                    )
                }
                val success = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                if (!success) {
                    Log.e(TAG, "Failed to set text on node at (${action.x}, ${action.y})")
                    return false
                }
            } else {
                Log.e(TAG, "Node at (${action.x}, ${action.y}) is not editable")
                return false
            }
        } else {
            Log.e(TAG, "No node found at (${action.x}, ${action.y})")
            return false
        }
        return true
    }

    private fun findNodeAtPosition(x: Int, y: Int): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        return findNodeAtPositionRecursive(rootNode, x, y)
    }

    private fun findNodeAtPositionRecursive(
        node: AccessibilityNodeInfo,
        x: Int,
        y: Int,
    ): AccessibilityNodeInfo? {
        val nodeBounds = Rect()
        node.getBoundsInScreen(nodeBounds)
        if (nodeBounds.contains(x, y)) {
            // Check if the node is an EditText or is editable
            if (node.className == EditText::class.java.name || node.isEditable) {
                return node
            }
            // Recursively check children
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    val result = findNodeAtPositionRecursive(child, x, y)
                    if (result != null) {
                        return result
                    }
                }
            }
        }
        return null
    }

    companion object {
        private const val ACTION_DISABLE_ACCESSIBILITY = "core.app.accessibility.disable"

        // Hilt can't create Android components so we are using this last resort hack
        private var instance: ai.looqs.android.core.accessibility.AppAccessibilityService? = null
        fun getInstance(): ai.looqs.android.core.accessibility.AppAccessibilityService? {
            return ai.looqs.android.core.accessibility.AppAccessibilityService.Companion.instance
        }

        const val SYSTEM_UI_PACKAGE = "com.android.systemui"

        fun isServiceEnabled(context: Context): Boolean {
            val am =
                context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val enabledServices =
                am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            for (enabledService in enabledServices) {
                val enabledServiceInfo =
                    enabledService.resolveInfo.serviceInfo
                if (enabledServiceInfo.packageName == context.packageName && enabledServiceInfo.name == ai.looqs.android.core.accessibility.AppAccessibilityService::class.java.name) return true
            }
            return false
        }

        fun startActionDisableAccessibility(context: Context) {
            context.startService(
                Intent(context, ai.looqs.android.core.accessibility.AppAccessibilityService::class.java).apply {
                    action =
                        ai.looqs.android.core.accessibility.AppAccessibilityService.Companion.ACTION_DISABLE_ACCESSIBILITY
                },
            )
        }

        const val ACTION_ACCESSIBILITY_CHANGED = "ACTION_ACCESSIBILITY_CHANGED"
    }
}

// adb shell settings put secure enabled_accessibility_services packagname/servicename
// adb shell dumpsys activity services accessibility