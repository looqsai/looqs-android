package com.sean8.jeminai

import android.app.Application
import com.sean8.jeminai.core.orchestrator.OrchestratorManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BaseApp : Application() {

    @Inject
    lateinit var orchestratorManager: OrchestratorManager
}