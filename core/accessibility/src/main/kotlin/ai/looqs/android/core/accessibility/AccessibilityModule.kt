package ai.looqs.android.core.accessibility

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
    fun provideAccessibilityStatusManager(): ai.looqs.android.core.accessibility.AccessibilityStatusManager {
        return ai.looqs.android.core.accessibility.AccessibilityStatusManager()
    }
}
