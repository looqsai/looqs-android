package com.sean8.jeminai.core.actions

import com.sean8.jeminai.core.actions.Action.DeviceKeyAction
import com.sean8.jeminai.core.actions.Action.LongClickAction
import com.sean8.jeminai.core.actions.Action.ScrollAction
import com.sean8.jeminai.core.actions.Action.SwipeAction
import com.sean8.jeminai.core.actions.Action.TapAction
import com.sean8.jeminai.core.actions.Action.TextAction
import com.sean8.jeminai.core.actions.Action.TextEditorAction
import com.sean8.jeminai.core.actions.ActionType.DeviceKey
import com.sean8.jeminai.core.actions.ActionType.LongClick
import com.sean8.jeminai.core.actions.ActionType.Scroll
import com.sean8.jeminai.core.actions.ActionType.Swipe
import com.sean8.jeminai.core.actions.ActionType.Tap
import com.sean8.jeminai.core.actions.ActionType.Text
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MoshiConfig {
    val moshi: Moshi = Moshi.Builder()
        .add(
            PolymorphicJsonAdapterFactory.of(Action::class.java, "type")
                .withSubtype(TapAction::class.java, Tap.serverName)
                .withSubtype(LongClickAction::class.java, LongClick.serverName)
                .withSubtype(SwipeAction::class.java, Swipe.serverName)
                .withSubtype(ScrollAction::class.java, Scroll.serverName)
                .withSubtype(DeviceKeyAction::class.java, DeviceKey.serverName)
                .withSubtype(TextAction::class.java, Text.serverName)
                .withSubtype(
                    TextEditorAction::class.java,
                    ActionType.TextEditorAction.serverName,
                ),
        )
        .add(KotlinJsonAdapterFactory())
        .build()
}
