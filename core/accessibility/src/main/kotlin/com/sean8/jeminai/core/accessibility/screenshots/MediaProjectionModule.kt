package com.sean8.jeminai.core.accessibility.screenshots

import android.content.Context
import android.media.projection.MediaProjectionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object MediaProjectionModule {

    @Provides
    @ServiceScoped
    fun provideMediaProjectionManager(
        @ApplicationContext context: Context,
    ): MediaProjectionManager {
        return context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }
}
