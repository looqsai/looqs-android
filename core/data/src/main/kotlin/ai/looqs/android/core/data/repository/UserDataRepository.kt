package ai.looqs.android.core.data.repository

import ai.looqs.android.core.model.DarkThemeConfig
import ai.looqs.android.core.model.UserData
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    val userData: Flow<UserData>
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)
    suspend fun setProjectionApproved(projectionApproved: Boolean)
    suspend fun setAssistantId(assistantId: String)
    suspend fun setThreadId(threadId: String)
}