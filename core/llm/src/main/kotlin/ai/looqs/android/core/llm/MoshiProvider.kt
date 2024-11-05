package ai.looqs.android.core.llm

import ai.looqs.android.core.actions.Action
import ai.looqs.android.core.actions.Action.DeviceKeyAction
import ai.looqs.android.core.actions.Action.LongClickAction
import ai.looqs.android.core.actions.Action.ScrollAction
import ai.looqs.android.core.actions.Action.SwipeAction
import ai.looqs.android.core.actions.Action.TapAction
import ai.looqs.android.core.actions.Action.TextAction
import ai.looqs.android.core.actions.ActionType
import ai.looqs.android.core.llm.models.RequestContentBlock
import ai.looqs.android.core.llm.models.ResponseContentBlock
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
