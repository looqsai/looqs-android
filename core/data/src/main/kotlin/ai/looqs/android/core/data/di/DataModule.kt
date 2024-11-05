package ai.looqs.android.core.data.di

import ai.looqs.android.core.data.repository.InitialUserDataRepository
import ai.looqs.android.core.data.repository.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    internal abstract fun bindsUserDataRepository(
        userDataRepository: InitialUserDataRepository,
    ): UserDataRepository
}