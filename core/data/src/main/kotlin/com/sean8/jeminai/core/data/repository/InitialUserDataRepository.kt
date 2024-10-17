package com.sean8.jeminai.core.data.repository

import com.sean8.jeminai.core.datastore.AppPreferencesDataSource
import com.sean8.jeminai.core.model.DarkThemeConfig
import com.sean8.jeminai.core.model.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class InitialUserDataRepository @Inject constructor(
    private val appPreferenceDataSource: AppPreferencesDataSource,
) : UserDataRepository {

    override val userData: Flow<UserData> = appPreferenceDataSource.userData

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        appPreferenceDataSource.setDarkThemeConfig(darkThemeConfig)
    }

    override suspend fun setProjectionApproved(projectionApproved: Boolean) {
        appPreferenceDataSource.setProjectionApproved(projectionApproved)
    }

    override suspend fun setAssistantId(assistantId: String) {
        appPreferenceDataSource.setAssistantId(assistantId)
    }

    override suspend fun setThreadId(threadId: String) {
        appPreferenceDataSource.setThreadId(threadId)
    }
}