package ai.looqs.android.core.accessibility.record

import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.widget.HorizontalScrollView
import android.widget.ListView
import android.widget.ScrollView
import android.widget.TimePicker

// Note about this: it's not the most straightforward code, but because Kotlin does not have
// an auto backing field capability on extended properties, we use something else
private const val KEY_TARGET_NODE = "app.key.KEY_TARGET_NODE"
var AccessibilityNodeInfo.isTargetNode: Boolean
    get() = this.extras?.getBoolean(KEY_TARGET_NODE, false) ?: false
    set(value) {
        this.extras.putBoolean(KEY_TARGET_NODE, value)
    }

private const val KEY_INDEX = "app.key.KEY_INDEX"
var AccessibilityNodeInfo.index: Int?
    get() = this.extras?.getInt(KEY_INDEX)
    set(value) {
        if (value != null) extras.putInt(KEY_INDEX, value)
    }

fun AccessibilityNodeInfo.isWebView(): Boolean {
    return this.className == WebView::class.java.name
}

fun AccessibilityNodeInfo.isTimePicker(): Boolean {
    return this.className == TimePicker::class.java.name || this.viewIdResourceName == "android:id/timePicker"
}

fun AccessibilityNodeInfo.isDrawer() =
    this.className?.contains("DrawerLayout", ignoreCase = true) ?: false

fun AccessibilityNodeInfo.isScrollClass() =
    getClassNameSafe().split(".").last() in listOf(
        ListView::class.java.simpleName,
        androidx.recyclerview.widget.RecyclerView::class.java.simpleName,
        ScrollView::class.java.simpleName,
        HorizontalScrollView::class.java.simpleName,
    )

fun AccessibilityNodeInfo.isSystemAction() = viewIdResourceName in listOf(
    "com.android.systemui:id/recent_apps",
    "com.android.systemui:id/home",
    "com.android.systemui:id/back",
)

enum class ImeActions(val description: String, val editorCode: Int) {
    GO("Go", EditorInfo.IME_ACTION_GO),
    DONE("Done", EditorInfo.IME_ACTION_DONE),
    NEXT("Next", EditorInfo.IME_ACTION_NEXT),
    SEARCH("Search", EditorInfo.IME_ACTION_SEARCH),
    SEND("Send", EditorInfo.IME_ACTION_SEND),
    PREVIOUS("Previous", EditorInfo.IME_ACTION_PREVIOUS),
    UNSPECIFIED("Unspecified", EditorInfo.IME_ACTION_UNSPECIFIED)
}

fun AccessibilityNodeInfo.isImeAction() =
    viewIdResourceName == "com.google.android.inputmethod.latin:id/key_pos_ime_action" ||
        (contentDescription != null && contentDescription.toString() in ImeActions.values()
            .map { it.description })

fun AccessibilityNodeInfo.isPermissionAllowButton() =
    viewIdResourceName == "com.android.packageinstaller:id/permission_allow_button"

fun AccessibilityNodeInfo.click() = isClickable && performAction(AccessibilityNodeInfo.ACTION_CLICK)

/** Returns the class name if exist, or View as a fallback class **/
fun AccessibilityNodeInfo.getClassNameSafe(): String =
    if (className.isNullOrEmpty()) View::class.java.name else className.toString()

fun AccessibilityEvent.toConciseString(): String {
    return when {
        eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED -> printScroll()
        text.isNotEmpty() -> text.toString()
        else -> ""
    }
}

fun AccessibilityEvent.printScroll(): String {
    return if (this.fromIndex != -1) {
        "Index $fromIndex --> $toIndex (total: $itemCount)"
    } else if (this.scrollX != 0 || scrollY != 0 || maxScrollX != 0 || maxScrollY != 0) {
        "Scrolled to $scrollX,$scrollY (max of $maxScrollX,$maxScrollY)"
    } else {
        "No scroll data found"
    }
}

fun AccessibilityEvent.printType(): String {
    return when (this.eventType) {
        AccessibilityEvent.TYPE_VIEW_CLICKED -> "VIEW_CLICKED"
        AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> "VIEW_LONG_CLICKED"
        AccessibilityEvent.TYPE_VIEW_SELECTED -> "VIEW_SELECTED"
        AccessibilityEvent.TYPE_VIEW_FOCUSED -> "VIEW_FOCUSED"
        AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> "VIEW_TEXT_CHANGED"
        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> "WINDOW_STATE_CHANGED"
        AccessibilityEvent.TYPE_VIEW_HOVER_ENTER -> "VIEW_HOVER_ENTER"
        AccessibilityEvent.TYPE_VIEW_HOVER_EXIT -> "VIEW_HOVER_EXIT"
        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> "WINDOW_CONTENT_CHANGED"
        AccessibilityEvent.TYPE_VIEW_SCROLLED -> "VIEW_SCROLLED"
        AccessibilityEvent.TYPE_WINDOWS_CHANGED -> "WINDOWS_CHANGED"
        else -> "Event: ${this.eventType}"
    }
}