package com.starter.app.di.module

import com.starter.app.data.dao.EventDao
import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.room.Room
import com.starter.app.ui.fcm.PushNotificationManager
import com.starter.app.core.AppSession
import com.starter.app.core.Session
import com.starter.app.data.dao.UserDao
import com.starter.app.di.DiConstants
import com.starter.app.di.UserDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Named(DiConstants.CACHE)
    internal fun provideCacheDir(application: Application): File {
        return application.cacheDir
    }

    @Provides
    @Singleton
    internal fun provideResources(application: Application): Resources {
        return application.resources
    }

    @Provides
    @Singleton
    internal fun provideCurrentLocale(resources: Resources): Locale {
        val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales.get(0)
        } else {
            resources.configuration.locale
        }

        return locale
    }

    @Provides
    @Singleton
    internal fun provideApplicationContext(@ApplicationContext applicationContext: Context): Context {
        return applicationContext
    }

    @Provides
    @Singleton
    @Named(DiConstants.API_KEY)
    internal fun provideApiKey(): String {
        return "ApiKey"
    }

    @Provides
    @Singleton
    internal fun provideSession(session: AppSession): Session = session

    @Provides
    @Singleton
    @Named(DiConstants.AES_KEY)
    internal fun provideAESKey(): String {
        return "xDzIhXLeo9sdwe1qukb9BSSmMpqwsd2h"
    }

    @Provides
    @Singleton
    fun provideUserDatabase(context: Context): UserDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            UserDatabase::class.java,
            "event_database"
        ).build()
    }

    @Provides
    fun provideUserDao(database: UserDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideEventDao(database: UserDatabase): EventDao {
        return database.eventDao()
    }


    @Singleton
    @Provides
    fun providePushNotificationManager(context: Context): PushNotificationManager {
        return PushNotificationManager(context)
    }
}