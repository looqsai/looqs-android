package com.sean8.jeminai.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import androidx.annotation.RequiresApi
import androidx.core.view.accessibility.AccessibilityEventCompat.TYPE_WINDOWS_CHANGED
import com.sean8.jeminai.core.accessibility.actions.Action
import com.sean8.jeminai.core.accessibility.tts.TextToSpeechHelper
import com.sean8.jeminai.core.accessibility.util.AccessibilityNodeInfoXmlDumper
import com.sean8.jeminai.core.accessibility.util.TreeNode
import com.sean8.jeminai.core.accessibility.util.defaultDisplay
import com.sean8.jeminai.core.accessibility.util.getBoundsInScreen
import com.sean8.jeminai.core.accessibility.util.populateChildren
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
    lateinit var eventBus: AccessibilityEventBus

    @Inject
    lateinit var textToSpeechHelper: TextToSpeechHelper

    private val serviceScope = CoroutineScope(Dispatchers.Main)

    override fun onDestroy() {
        accessibilityComponent = null
        super.onDestroy()

        AccessibilityStatusManager.setServiceEnabled(false)
    }

    override fun onServiceConnected() {
        Log.d(TAG, "onServiceConnected, info: $serviceInfo")

        // TODO: God forgive this horrible spaghetti memory leak hell. Will fix later
        accessibilityComponent = this

        AccessibilityStatusManager.setServiceEnabled(true)
    }

    @RequiresApi(VERSION_CODES.P)
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val source: AccessibilityNodeInfo? = event?.source

        Log.d(TAG, "Event received: ${event.toString()}")

        Log.d(TAG, "Source Node found? $source")

        if (source != null) {
            val tree = getActiveWindowTree()
            val xml = tree?.toXml()
            Log.d(TAG, xml.toString())
        }

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
            Log.d(
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
        AccessibilityNodeInfoXmlDumper.getXml(this, this@AppAccessibilityService.defaultDisplay)

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
    fun populatedRoot() = TreeNode(this.root!!).apply { populateChildren() }

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

    fun performTapAction(action: Action.TapAction) {
        val path = Path().apply {
            moveTo(action.x, action.y)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        dispatchGesture(gesture, null, null)
    }

    fun performLongClickAction(action: Action.LongClickAction) {
        val path = Path().apply {
            moveTo(action.x, action.y)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 1000))
            .build()
        dispatchGesture(gesture, null, null)
    }

    fun performSwipeAction(action: Action.SwipeAction) {
        performGestureAction(action.start, action.end, action.swipeDuration)
    }

    fun performScrollAction(action: Action.ScrollAction) {
        performGestureAction(action.start, action.end, action.swipeDuration)
    }

    private fun performGestureAction(
        start: Action.Point,
        end: Action.Point,
        duration: Long,
    ) {
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
        dispatchGesture(gesture, null, null)
    }

    fun performDeviceKeyAction(action: Action.DeviceKeyAction) {
        val result = when (action.serverAction) {
            KeyEvent.KEYCODE_BACK -> performGlobalAction(GLOBAL_ACTION_BACK)
            KeyEvent.KEYCODE_HOME -> performGlobalAction(GLOBAL_ACTION_HOME)
            KeyEvent.KEYCODE_APP_SWITCH -> performGlobalAction(GLOBAL_ACTION_RECENTS)
            KeyEvent.KEYCODE_NOTIFICATION -> performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            else -> false
        }
        if (!result) {
            Log.e("AppAccessibilityService", "Failed to perform device key action")
        }
    }

    fun performTextAction(action: Action.TextAction) {
        val node = findNodeByPath(action.pathToElement)
        node?.let {
            val arguments = Bundle().apply {
                putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    action.text,
                )
            }
            it.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        } ?: Log.e("AppAccessibilityService", "Failed to find node for text action")
    }

    fun performTextEditorAction(action: Action.TextEditorAction) {
        // Implement based on action.action (e.g., "send", "done", "next")
        // This might involve finding the appropriate UI element and performing a click
    }

    private fun findNodeByPath(path: List<Int>): AccessibilityNodeInfo? {
        var currentNode = rootInActiveWindow
        for (index in path) {
            currentNode = currentNode?.getChild(index)
        }
        return currentNode
    }

    fun speak(text: String) {
        textToSpeechHelper.speak(text)
    }

    companion object {
        private const val ACTION_DISABLE_ACCESSIBILITY = "core.app.accessibility.disable"

        // TODO: Fix this horrible workaround
        var accessibilityComponent: AppAccessibilityService? = null

        const val SYSTEM_UI_PACKAGE = "com.android.systemui"

        fun isServiceEnabled(context: Context): Boolean {
            val am =
                context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val enabledServices =
                am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            for (enabledService in enabledServices) {
                val enabledServiceInfo =
                    enabledService.resolveInfo.serviceInfo
                if (enabledServiceInfo.packageName == context.packageName && enabledServiceInfo.name == AppAccessibilityService::class.java.name) return true
            }
            return false
        }

        fun startActionDisableAccessibility(context: Context) {
            context.startService(
                Intent(context, AppAccessibilityService::class.java).apply {
                    action = ACTION_DISABLE_ACCESSIBILITY
                },
            )
        }

        const val ACTION_ACCESSIBILITY_CHANGED = "ACTION_ACCESSIBILITY_CHANGED"
    }
}

// adb shell settings put secure enabled_accessibility_services packagname/servicename
// adb shell dumpsys activity services accessibility