package ai.looqs.android.core.datastore

import androidx.datastore.core.DataStore
import ai.looqs.android.core.datastore.DarkThemeConfigProto.DARK_THEME_CONFIG_DARK
import ai.looqs.android.core.datastore.DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM
import ai.looqs.android.core.datastore.DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT
import ai.looqs.android.core.datastore.DarkThemeConfigProto.DARK_THEME_CONFIG_UNSPECIFIED
import ai.looqs.android.core.datastore.DarkThemeConfigProto.UNRECOGNIZED
import ai.looqs.android.core.datastore.UserPreferences
import ai.looqs.android.core.datastore.copy
import ai.looqs.android.core.model.DarkThemeConfig
import ai.looqs.android.core.model.DarkThemeConfig.DARK
import ai.looqs.android.core.model.DarkThemeConfig.FOLLOW_SYSTEM
import ai.looqs.android.core.model.DarkThemeConfig.LIGHT
import ai.looqs.android.core.model.UserData
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
) {
    val userData = userPreferences.data
        .map {
            UserData(
                darkThemeConfig = when (it.darkThemeConfig) {
                    null,
                    DARK_THEME_CONFIG_UNSPECIFIED,
                    UNRECOGNIZED,
                    DARK_THEME_CONFIG_FOLLOW_SYSTEM,
                    -> FOLLOW_SYSTEM

                    DARK_THEME_CONFIG_LIGHT -> LIGHT

                    DARK_THEME_CONFIG_DARK -> DARK
                },
                isProjectionApproved = it.isProjectionApproved,
                assistantId = it.assistantId.takeIf { id -> id.isNotBlank() },
                threadId = it.threadId.takeIf { id -> id.isNotBlank() },
            )
        }

    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        userPreferences.updateData {
            it.copy {
                this.darkThemeConfig = when (darkThemeConfig) {
                    DarkThemeConfig.FOLLOW_SYSTEM ->
                        DARK_THEME_CONFIG_FOLLOW_SYSTEM

                    DarkThemeConfig.LIGHT -> DARK_THEME_CONFIG_LIGHT
                    DarkThemeConfig.DARK -> DARK_THEME_CONFIG_DARK
                }
            }
        }
    }

    suspend fun setProjectionApproved(projectionApproved: Boolean) {
        userPreferences.updateData {
            it.copy {
                this.isProjectionApproved = projectionApproved
            }
        }
    }

    suspend fun setAssistantId(assistantId: String) {
        userPreferences.updateData {
            it.copy {
                this.assistantId = assistantId
            }
        }
    }

    suspend fun setThreadId(threadId: String) {
        userPreferences.updateData {
            it.copy {
                this.threadId = threadId
            }
        }
    }
}