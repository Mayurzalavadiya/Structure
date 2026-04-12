package com.starter.app.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class AppPreferences @Inject
constructor(context: Context) {

    companion object {
        const val SHARED_PREF_NAME = "app_preference"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)


    @SuppressLint("CommitPrefEdits")
    fun putString(name: String, value: String) {
        sharedPreferences.edit {
            this.putString(name, value)
        }
    }


    @SuppressLint("CommitPrefEdits")
    fun putBoolean(name: String, value: Boolean) {
        sharedPreferences.edit {
            this.putBoolean(name, value)
        }
    }

    fun getBoolean(name: String): Boolean {
        return sharedPreferences.getBoolean(name, false)
    }

    fun getString(name: String): String {
        return sharedPreferences.getString(name,"")?:""
    }

    fun getInt(name: String): Int {
        return sharedPreferences.getInt(name, 0)
    }

    fun clear(name: String) {
        sharedPreferences.edit {
            remove(name)
        }
    }

    fun clearAll() {
        sharedPreferences.edit {
            clear()
        }
    }

    fun putFloat(name: String, value: Float) {
        sharedPreferences.edit {
            this.putFloat(name, value)
        }
    }

    fun getFloat(name: String): Float {
        return sharedPreferences.getFloat(name, 0f)
    }


}
