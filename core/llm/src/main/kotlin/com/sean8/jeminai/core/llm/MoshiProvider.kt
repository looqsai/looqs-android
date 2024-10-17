package com.sean8.jeminai.core.llm

import com.sean8.jeminai.core.actions.Action
import com.sean8.jeminai.core.actions.Action.DeviceKeyAction
import com.sean8.jeminai.core.actions.Action.LongClickAction
import com.sean8.jeminai.core.actions.Action.ScrollAction
import com.sean8.jeminai.core.actions.Action.SwipeAction
import com.sean8.jeminai.core.actions.Action.TapAction
import com.sean8.jeminai.core.actions.Action.TextAction
import com.sean8.jeminai.core.actions.Action.TextEditorAction
import com.sean8.jeminai.core.actions.ActionType
import com.sean8.jeminai.core.llm.models.RequestContentBlock
import com.sean8.jeminai.core.llm.models.ResponseContentBlock
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoshiProvider @Inject constructor() {

    val moshi: Moshi = Moshi.Builder()
        .add(
            PolymorphicJsonAdapterFactory.of(Action::class.java, "type")
                .withSubtype(DeviceKeyAction::class.java, ActionType.DeviceKey.serverName)
                .withSubtype(TapAction::class.java, ActionType.Tap.serverName)
                .withSubtype(LongClickAction::class.java, ActionType.LongClick.serverName)
                .withSubtype(SwipeAction::class.java, ActionType.Swipe.serverName)
                .withSubtype(ScrollAction::class.java, ActionType.Scroll.serverName)
                .withSubtype(TextAction::class.java, ActionType.Text.serverName)
                .withSubtype(TextEditorAction::class.java, ActionType.TextEditorAction.serverName),
        )
        .add(
            PolymorphicJsonAdapterFactory.of(RequestContentBlock::class.java, "type")
                .withSubtype(RequestContentBlock.TextBlock::class.java, "text")
                .withSubtype(RequestContentBlock.ImageFileBlock::class.java, "image_file"),
        )
        .add(
            PolymorphicJsonAdapterFactory.of(ResponseContentBlock::class.java, "type")
                .withSubtype(ResponseContentBlock.TextBlock::class.java, "text")
                .withSubtype(ResponseContentBlock.ImageFileBlock::class.java, "image_file"),
        )
        .add(KotlinJsonAdapterFactory())
        .build()
}
