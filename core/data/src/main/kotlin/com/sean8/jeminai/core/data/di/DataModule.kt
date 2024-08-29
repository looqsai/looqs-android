package com.sean8.jeminai.core.data.di

import com.sean8.jeminai.core.data.repository.InitialUserDataRepository
import com.sean8.jeminai.core.data.repository.UserDataRepository
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