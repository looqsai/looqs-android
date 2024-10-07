package com.sean8.jeminai.core.accessibility

import com.sean8.jeminai.core.accessibility.util.AccessibilityServiceHelper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
abstract class AccessibilityModule {

    @Binds
    @ServiceScoped
    abstract fun bindAccessibilityHelper(
        impl: AccessibilityServiceHelperImpl,
    ): AccessibilityServiceHelper
}