package com.sean8.jeminai.core.accessibility

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent

@Module
@InstallIn(ServiceComponent::class)
object AccessibilityServiceModule {

    @Provides
    fun provideAppAccessibilityService(): AppAccessibilityService? {
        return AppAccessibilityService.getInstance()
    }
}