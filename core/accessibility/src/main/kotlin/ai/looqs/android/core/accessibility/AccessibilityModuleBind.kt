package ai.looqs.android.core.accessibility

import ai.looqs.android.core.accessibility.util.AccessibilityServiceHelper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
abstract class AccessibilityModuleBind {

    @Binds
    @ServiceScoped
    abstract fun bindAccessibilityHelper(
        impl: ai.looqs.android.core.accessibility.AccessibilityServiceHelperImpl,
    ): AccessibilityServiceHelper
}