package com.sean8.jeminai.core.screenshot

import android.media.projection.MediaProjection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ProjectionState {
    private val _mediaProjection = MutableStateFlow<MediaProjection?>(null)
    val mediaProjection: StateFlow<MediaProjection?> = _mediaProjection.asStateFlow()

    fun setMediaProjection(mediaProjection: MediaProjection) {
        _mediaProjection.value = mediaProjection
    }
}
