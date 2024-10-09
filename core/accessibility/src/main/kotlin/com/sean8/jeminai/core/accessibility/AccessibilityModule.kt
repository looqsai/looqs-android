package com.sean8.jeminai.core.accessibility

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AccessibilityModule {

    @Provides
    @Singleton
    fun provideAccessibilityStatusManager(): AccessibilityStatusManager {
        return AccessibilityStatusManager()
    }
}
