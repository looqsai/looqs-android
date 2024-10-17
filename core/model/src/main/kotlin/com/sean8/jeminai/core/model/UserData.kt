package com.sean8.jeminai.core.model

data class UserData(
    val darkThemeConfig: DarkThemeConfig,
    val isProjectionApproved: Boolean,
    val assistantId: String? = null,
    val threadId: String? = null,
)
