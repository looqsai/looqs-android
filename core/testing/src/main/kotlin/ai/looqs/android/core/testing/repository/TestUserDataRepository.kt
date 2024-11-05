package ai.looqs.android.core.testing.repository

import ai.looqs.android.core.data.repository.UserDataRepository
import ai.looqs.android.core.model.DarkThemeConfig
import ai.looqs.android.core.model.DarkThemeConfig.FOLLOW_SYSTEM
import ai.looqs.android.core.model.UserData
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull

val emptyUserData = UserData(
    darkThemeConfig = FOLLOW_SYSTEM,
    isProjectionApproved = false,
)

class TestUserDataRepository : UserDataRepository {
    /**
     * The backing hot flow for the list of followed topic ids for testing.
     */
    private val _userData = MutableSharedFlow<UserData>(replay = 1, onBufferOverflow = DROP_OLDEST)

    private val currentUserData get() = _userData.replayCache.firstOrNull() ?: emptyUserData

    override val userData: Flow<UserData> = _userData.filterNotNull()

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        currentUserData.let { current ->
            _userData.tryEmit(current.copy(darkThemeConfig = darkThemeConfig))
        }
    }

    override suspend fun setProjectionApproved(projectionApproved: Boolean) {
        currentUserData.let { current ->
            _userData.tryEmit(current.copy(isProjectionApproved = projectionApproved))
        }
    }

    override suspend fun setAssistantId(assistantId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun setThreadId(threadId: String) {
        TODO("Not yet implemented")
    }

    /**
     * A test-only API to allow setting of user data directly.
     */
    fun setUserData(userData: UserData) {
        _userData.tryEmit(userData)
    }
}
