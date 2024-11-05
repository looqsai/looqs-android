package ai.looqs.android.core.orchestrator

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.sean8.core.hiltcoroutines.di.ApplicationScope
import ai.looqs.android.core.accessibility.AccessibilityStatusManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrchestratorManager @Inject constructor(
    @ApplicationContext private val context: Context,
    accessibilityStatusManager: AccessibilityStatusManager,
    @ApplicationScope private val scope: CoroutineScope,
) {
    private val TAG = "OrchestratorManager"

    init {
        scope.launch {
            accessibilityStatusManager.isServiceEnabled.collectLatest { isEnabled ->
                Log.d(TAG, "Accessibility Service Enabled: $isEnabled")
                if (isEnabled) {
                    startOrchestratorService()
                } else {
                    stopOrchestratorService()
                }
            }
        }
    }

    private fun startOrchestratorService() {
        val orchestratorIntent = Intent(context, OrchestratorService::class.java)
        ContextCompat.startForegroundService(context, orchestratorIntent)
        Log.d(TAG, "OrchestratorService started")
    }

    private fun stopOrchestratorService() {
        val orchestratorIntent = Intent(context, OrchestratorService::class.java)
        context.stopService(orchestratorIntent)
        Log.d(TAG, "OrchestratorService stopped")
    }
}
