package ai.looqs.android.core.data.repository

import ai.looqs.android.core.datastore.AppPreferencesDataSource
import ai.looqs.android.core.model.DarkThemeConfig
import ai.looqs.android.core.model.UserData
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