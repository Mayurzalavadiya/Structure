package com.starter.app.di

import com.starter.app.data.dao.EventDao
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.starter.app.data.dao.UserDao
import com.starter.app.data.pojo.dataclass.Event
import com.starter.app.data.pojo.dataclass.UserEntity

@Database(entities = [Event::class, UserEntity::class], version = 1, exportSchema = false)
abstract class UserDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "event_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
