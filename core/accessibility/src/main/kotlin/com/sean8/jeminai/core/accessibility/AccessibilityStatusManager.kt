package com.sean8.jeminai.core.accessibility

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Singleton

@Singleton
object AccessibilityStatusManager {
    private val _isServiceEnabled = MutableLiveData<Boolean>(false)
    val isServiceEnabled: LiveData<Boolean> get() = _isServiceEnabled

    fun setServiceEnabled(enabled: Boolean) {
        _isServiceEnabled.postValue(enabled)
    }
}
