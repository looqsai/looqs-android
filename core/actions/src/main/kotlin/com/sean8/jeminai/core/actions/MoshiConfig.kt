package com.sean8.jeminai.core.actions

import com.sean8.jeminai.core.actions.Action.DeviceKeyAction
import com.sean8.jeminai.core.actions.Action.LongClickAction
import com.sean8.jeminai.core.actions.Action.ScrollAction
import com.sean8.jeminai.core.actions.Action.SwipeAction
import com.sean8.jeminai.core.actions.Action.TapAction
import com.sean8.jeminai.core.actions.Action.TextAction
import com.sean8.jeminai.core.actions.Action.TextEditorAction
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MoshiConfig {
    val moshi: Moshi = Moshi.Builder()
        .add(
            PolymorphicJsonAdapterFactory.of(Action::class.java, "type")
                .withSubtype(TapAction::class.java, ActionType.Tap.serverName)
                .withSubtype(LongClickAction::class.java, ActionType.LongClick.serverName)
                .withSubtype(SwipeAction::class.java, ActionType.Swipe.serverName)
                .withSubtype(ScrollAction::class.java, ActionType.Scroll.serverName)
                .withSubtype(DeviceKeyAction::class.java, ActionType.DeviceKey.serverName)
                .withSubtype(TextAction::class.java, ActionType.Text.serverName)
                .withSubtype(TextEditorAction::class.java, ActionType.TextEditorAction.serverName),
        )
        .add(KotlinJsonAdapterFactory())
        .build()
}
