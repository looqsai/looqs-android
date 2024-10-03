package com.sean8.jeminai.core.data.repository

import com.sean8.jeminai.core.model.DarkThemeConfig
import com.sean8.jeminai.core.model.UserData
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    val userData: Flow<UserData>
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)
    suspend fun setProjectionApproved(projectionApproved: Boolean)
}