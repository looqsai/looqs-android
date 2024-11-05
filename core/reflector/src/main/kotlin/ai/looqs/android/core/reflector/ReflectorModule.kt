package ai.looqs.android.core.reflector

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReflectorModule {

    @Provides
    @Singleton
    fun provideReflectorController(): ReflectorController {
        return ReflectorController()
    }
}
