package ai.looqs.android.core.accessibility

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent

@Module
@InstallIn(ServiceComponent::class)
object AccessibilityServiceModule {

    @Provides
    fun provideAppAccessibilityService(): ai.looqs.android.core.accessibility.AppAccessibilityService? {
        return ai.looqs.android.core.accessibility.AppAccessibilityService.Companion.getInstance()
    }
}