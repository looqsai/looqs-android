package ai.looqs.android

import android.app.Application
import ai.looqs.android.core.orchestrator.OrchestratorManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BaseApp : Application() {

    @Inject
    lateinit var orchestratorManager: OrchestratorManager
}