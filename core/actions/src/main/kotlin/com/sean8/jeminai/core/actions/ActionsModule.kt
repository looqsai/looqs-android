package com.sean8.jeminai.core.actions

import com.sean8.jeminai.core.actions.ActionType.DeviceKey
import com.sean8.jeminai.core.actions.ActionType.LongClick
import com.sean8.jeminai.core.actions.ActionType.Scroll
import com.sean8.jeminai.core.actions.ActionType.Swipe
import com.sean8.jeminai.core.actions.ActionType.Tap
import com.sean8.jeminai.core.actions.ActionType.Text
import com.squareup.moshi.JsonClass

// Enum for action types
enum class ActionType(val serverName: String) {
    DeviceKey("android-device-key"),
    Tap("android-tap"),
    LongClick("android-long-click"),
    Swipe("android-swipe"),
    Scroll("android-scroll"),
    Text("text"),
    TextEditorAction("android-key-board-special-keys")
}

// Base action class
@JsonClass(generateAdapter = true, generator = "sealed:type")
sealed class Action {
    abstract val type: String

    @JsonClass(generateAdapter = true)
    data class DeviceKeyAction(
        override val type: String = DeviceKey.serverName,
        val serverAction: Int,
    ) : Action()

    @JsonClass(generateAdapter = true)
    data class TapAction(
        override val type: String = Tap.serverName,
        val attributes: Map<String, String> = emptyMap(),
        val pathToElement: List<Int>,
        val x: Float,
        val y: Float,
    ) : Action()

    @JsonClass(generateAdapter = true)
    data class LongClickAction(
        override val type: String = LongClick.serverName,
        val attributes: Map<String, String> = emptyMap(),
        val pathToElement: List<Int>,
        val x: Float,
        val y: Float,
    ) : Action()

    @JsonClass(generateAdapter = true)
    data class SwipeAction(
        override val type: String = Swipe.serverName,
        val attributes: Map<String, String> = emptyMap(),
        val pathToElement: List<Int>,
        val start: Point,
        val end: Point,
        val swipeDuration: Long = 1000,
        val direction: String,
    ) : Action()

    @JsonClass(generateAdapter = true)
    data class ScrollAction(
        override val type: String = Scroll.serverName,
        val attributes: Map<String, String> = emptyMap(),
        val pathToElement: List<Int>,
        val start: Point,
        val end: Point,
        val swipeDuration: Long = 1000,
    ) : Action()

    @JsonClass(generateAdapter = true)
    data class TextAction(
        override val type: String = Text.serverName,
        val attributes: Map<String, String> = emptyMap(),
        val pathToElement: List<Int>,
        val text: String,
    ) : Action()

    @JsonClass(generateAdapter = true)
    data class TextEditorAction(
        override val type: String = com.sean8.jeminai.core.actions.ActionType.TextEditorAction.serverName,
        val action: String,
    ) : Action()

    @JsonClass(generateAdapter = true)
    data class Point(
        val x: Float,
        val y: Float,
    )
}
