package com.sean8.jeminai.feature.setup

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sean8.jeminai.core.accessibility.AccessibilityStatusManager
import com.sean8.jeminai.core.accessibility.connectivity.NetworkMonitor
import com.sean8.jeminai.core.accessibility.screenshots.ProjectionState
import com.sean8.jeminai.core.data.repository.UserDataRepository
import com.sean8.jeminai.core.orchestrator.OrchestratorService
import com.sean8.jeminai.core.voice.AudioRecordingService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkMonitor: NetworkMonitor,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    // Network Status
    val networkStatus: StateFlow<Boolean> = networkMonitor.isOnline.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = false,
    )

    // Accessibility Status
    val accessibilityStatus: StateFlow<Boolean> =
        AccessibilityStatusManager.isServiceEnabled.stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000),
            initialValue = false,
        )

    // Projection Token
    val projectionToken: StateFlow<Intent?> = ProjectionState.projectionToken

    // Latest Message
    private val _latestMessage = MutableStateFlow("Server is WIP")
    val latestMessage: StateFlow<String> = _latestMessage.asStateFlow()

    val appVersion: String =
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "v${packageInfo.versionName}" // or use versionCode if needed: packageInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            "Version not found"
        }

    // Listening Status
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    // Audio Permission Status
    private val _isAudioPermissionGranted = MutableStateFlow(false)
    val isAudioPermissionGranted: StateFlow<Boolean> = _isAudioPermissionGranted.asStateFlow()

    // Navigation and Permission Events (One-time Events)
    private val _navigateToAccessibility = MutableSharedFlow<Unit>()
    val navigateToAccessibility: SharedFlow<Unit> = _navigateToAccessibility.asSharedFlow()

    private val _requestProjectionPermission = MutableSharedFlow<Unit>()
    val requestProjectionPermission: SharedFlow<Unit> = _requestProjectionPermission.asSharedFlow()

    private val _requestAudioPermission = MutableSharedFlow<Unit>()
    val requestAudioPermission: SharedFlow<Unit> = _requestAudioPermission.asSharedFlow()

    init {
        // Initial checks
        checkAudioPermission()

        viewModelScope.launch {
            accessibilityStatus.collect { statusEnabled ->
                if (statusEnabled) {
                    startForegroundService(
                        context,
                        Intent(context, OrchestratorService::class.java),
                    )
                }
            }
        }
    }

    // Methods
    fun navigateToAccessibility() {
        viewModelScope.launch {
            _navigateToAccessibility.emit(Unit)
        }
    }

    fun askProjection() {
        viewModelScope.launch {
            _requestProjectionPermission.emit(Unit)
        }
    }

    fun onProjectionPermissionResult(data: Intent) {
        ProjectionState.setProjectionToken(data)
        viewModelScope.launch {
            userDataRepository.setProjectionApproved(true)
        }
    }

    fun startListeningService() {
        val intent = Intent(context, AudioRecordingService::class.java)
        context.startForegroundService(intent)
        _isListening.value = true
    }

    fun stopListeningService() {
        val intent = Intent(context, AudioRecordingService::class.java)
        context.stopService(intent)
        _isListening.value = false
    }

    fun checkAudioPermission() {
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        _isAudioPermissionGranted.value = isGranted
    }

    fun requestAudioPermission() {
        viewModelScope.launch {
            _requestAudioPermission.emit(Unit)
        }
    }

    fun onAudioPermissionResult(granted: Boolean) {
        _isAudioPermissionGranted.value = granted
    }
}
