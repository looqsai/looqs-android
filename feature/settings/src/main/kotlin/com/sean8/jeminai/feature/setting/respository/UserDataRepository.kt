package com.sean8.jeminai.feature.setting.respository

import com.sean8.jeminai.feature.settings.model.data.DarkThemeConfig
import com.sean8.jeminai.feature.settings.model.data.UserData
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {

    /**
     * Stream of [UserData]
     */
    val userData: Flow<UserData>

    /**
     * Sets the desired dark theme config.
     */
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)
}