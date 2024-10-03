package com.sean8.jeminai.core.accessibility.util

import android.view.accessibility.AccessibilityEvent

fun AccessibilityEvent.isKeyboardEvent() =
    packageName != null && isSupportedKeyboard(packageName) &&
        eventType in listOf(
        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
        AccessibilityEvent.TYPE_VIEW_HOVER_ENTER,
    )

fun isSupportedKeyboard(packageName: CharSequence) = packageName in listOf(
    "com.android.inputmethod.latin",
    "com.google.android.inputmethod.latin",
)