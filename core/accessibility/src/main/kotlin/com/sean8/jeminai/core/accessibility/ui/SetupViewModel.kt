package com.sean8.jeminai.core.accessibility.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.sean8.jeminai.core.accessibility.AccessibilityStatusManager
import com.sean8.jeminai.core.accessibility.AppAccessibilityService
import com.sean8.jeminai.core.accessibility.connectivity.NetworkMonitor
import com.sean8.jeminai.core.accessibility.screenshots.ProjectionState
import com.sean8.jeminai.core.accessibility.util.Event
import com.sean8.jeminai.core.data.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SetupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkMonitor: NetworkMonitor,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    val networkStatus: LiveData<Boolean> = networkMonitor.isOnline.asLiveData()
    val projectionToken = ProjectionState.projectionToken
    val accessibilityStatus: LiveData<Boolean> = AccessibilityStatusManager.isServiceEnabled
    val latestMessage: MutableLiveData<String> = MutableLiveData("Server is WIP")
    val navigateToAccessibility = MutableLiveData<Event<Unit>>()

    val hasApprovedProjection: StateFlow<Boolean> =
        userDataRepository.userData
            .map { userData ->
                userData.isProjectionApproved
            }
            .stateIn(
                scope = viewModelScope,
                started = WhileSubscribed(5.seconds.inWholeMilliseconds),
                initialValue = false,
            )

    private val _requestProjectionPermission = MutableLiveData<Event<Unit>>()
    val requestProjectionPermission: LiveData<Event<Unit>> get() = _requestProjectionPermission

    // New LiveData to track assistant status
    private val _isDefaultAssistant = MutableLiveData<Boolean>()
    val isDefaultAssistant: LiveData<Boolean> get() = _isDefaultAssistant

    // Event to navigate to assistant settings
    private val _navigateToAssistantSettings = MutableLiveData<Event<Unit>>()
    val navigateToAssistantSettings: LiveData<Event<Unit>> get() = _navigateToAssistantSettings

    init {
        checkIfDefaultAssistant()
    }

    // Method to check if the app is the default assistant
    fun checkIfDefaultAssistant() {
        val assistantComponent = Settings.Secure.getString(
            context.contentResolver,
            "assistant", // TODO: Settings.Secure.ASSISTANT
        )
        val defaultAssistant = ComponentName.unflattenFromString(assistantComponent ?: "")
        val myAssistant = ComponentName(
            context,
            "com.sean8.jeminai/com.sean8.jeminai.core.voice.MyVoiceInteractionService",
        )
        val isDefault = defaultAssistant == myAssistant
        _isDefaultAssistant.value = isDefault
    }

    // Method to navigate to assistant settings
    fun navigateToAssistantSettings() {
        _navigateToAssistantSettings.value = Event(Unit)
    }

    fun navigateToAccessibility() {
        // TODO: open accessibility window
    }

    fun revokeAccessibility() {
        AppAccessibilityService.startActionDisableAccessibility(context)
    }

    fun askProjection() {
        _requestProjectionPermission.value = Event(Unit)
    }

    fun onProjectionPermissionResult(data: Intent) {
        ProjectionState.projectionToken.value = data

        viewModelScope.launch {
            userDataRepository.setProjectionApproved(true)
        }
    }

    val appVersion: String =
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "v${packageInfo.versionName}" // or use versionCode if needed: packageInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            "Version not found"
        }
}
