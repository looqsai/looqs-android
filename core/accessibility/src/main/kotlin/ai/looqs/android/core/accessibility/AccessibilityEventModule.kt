package ai.looqs.android.core.accessibility

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AccessibilityEventModule {

    @Provides
    @Singleton
    fun provideAccessibilityEventBus() = ai.looqs.android.core.accessibility.AccessibilityEventBus()
}
