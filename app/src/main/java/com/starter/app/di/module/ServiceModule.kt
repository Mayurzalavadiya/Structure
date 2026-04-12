package com.starter.app.di.module

import com.starter.app.data.datasource.UserLiveDataSource
import com.starter.app.data.repository.UserRepository
import com.starter.app.data.service.AuthenticationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideUserRepository(userLiveDataSource: UserLiveDataSource): UserRepository {
        return userLiveDataSource
    }

    @Provides
    @Singleton
    fun provideAuthenticationService(retrofit: Retrofit): AuthenticationService {
        return retrofit.create(AuthenticationService::class.java)
    }
}