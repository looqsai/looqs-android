package com.sean8.jeminai.core.accessibility.actions

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MoshiConfig {
    val moshi: Moshi = Moshi.Builder()
        .add(
            PolymorphicJsonAdapterFactory.of(Action::class.java, "type")
                .withSubtype(Action.TapAction::class.java, ActionType.Tap.serverName)
                .withSubtype(Action.LongClickAction::class.java, ActionType.LongClick.serverName)
                .withSubtype(Action.SwipeAction::class.java, ActionType.Swipe.serverName)
                .withSubtype(Action.ScrollAction::class.java, ActionType.Scroll.serverName)
                .withSubtype(Action.DeviceKeyAction::class.java, ActionType.DeviceKey.serverName)
                .withSubtype(Action.TextAction::class.java, ActionType.Text.serverName)
                .withSubtype(
                    Action.TextEditorAction::class.java,
                    ActionType.TextEditorAction.serverName,
                ),
        )
        .add(KotlinJsonAdapterFactory())
        .build()
}
