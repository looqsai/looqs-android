package com.sean8.jeminai.core.data.repository

import com.sean8.jeminai.core.model.DarkThemeConfig
import com.sean8.jeminai.core.model.UserData
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