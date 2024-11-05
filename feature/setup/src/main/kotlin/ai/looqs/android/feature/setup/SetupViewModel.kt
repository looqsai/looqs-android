package ai.looqs.android.feature.setup

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.looqs.android.core.accessibility.AccessibilityStatusManager
import ai.looqs.android.core.accessibility.connectivity.NetworkMonitor
import ai.looqs.android.core.data.repository.UserDataRepository
import ai.looqs.android.core.screenshot.ProjectionState
import ai.looqs.android.core.screenshot.ScreenshotService
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
    private val accessibilityStatusManager: AccessibilityStatusManager,
) : ViewModel() {

    // Network Status
    val networkStatus: StateFlow<Boolean> = networkMonitor.isOnline.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = false,
    )

    // Accessibility Status
    val accessibilityStatus: StateFlow<Boolean> =
        accessibilityStatusManager.isServiceEnabled.stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000),
            initialValue = false,
        )

    // Projection Status
    val projectionStatus = ProjectionState.mediaProjection.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = null,
    )

    // Audio Permission Status
    private val _isAudioPermissionGranted = MutableStateFlow(false)
    val isAudioPermissionGranted: StateFlow<Boolean> = _isAudioPermissionGranted.asStateFlow()

    private val _requestAudioPermission = MutableSharedFlow<Unit>()
    val requestAudioPermission: SharedFlow<Unit> = _requestAudioPermission.asSharedFlow()

    // Application Version
    val appVersion: String = try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        "v${packageInfo.versionName}"
    } catch (e: PackageManager.NameNotFoundException) {
        "Version not found"
    }

    // Navigation and Permission Events (One-time Events)
    private val _navigateToAccessibility = MutableSharedFlow<Unit>()
    val navigateToAccessibility: SharedFlow<Unit> = _navigateToAccessibility.asSharedFlow()

    private val _requestProjectionPermission = MutableSharedFlow<Unit>()
    val requestProjectionPermission: SharedFlow<Unit> = _requestProjectionPermission.asSharedFlow()

    init {
        // Initial checks
        checkAudioPermission()
    }

    // Methods
    fun navigateToAccessibility() {
        viewModelScope.launch {
            _navigateToAccessibility.emit(Unit)
        }
    }

    fun askProjectionPermission() {
        viewModelScope.launch {
            _requestProjectionPermission.emit(Unit)
        }
    }

    fun onProjectionPermissionResult(resultCode: Int, data: Intent) {
        val serviceIntent = Intent(context, ScreenshotService::class.java).apply {
            putExtra("resultCode", resultCode)
            putExtra("data", data)
        }
        startForegroundService(context, serviceIntent)
    }

    private fun checkAudioPermission() {
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
