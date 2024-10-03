package com.sean8.jeminai.core.accessibility.screenshots

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import javax.inject.Singleton

@Singleton
object ProjectionState {
    val projectionToken = MutableLiveData<Intent?>()
}