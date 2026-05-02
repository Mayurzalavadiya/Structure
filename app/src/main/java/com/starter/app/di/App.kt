package com.starter.app.di

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.starter.app.BuildConfig
import com.starter.app.R
import com.starter.app.utils.AppUtil.applyEdgeToEdgeInsets
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {


    companion object {
        @SuppressLint("StaticFieldLeak")
        var mContext: Context? = null
        const val FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".provider"
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycle()
    }


    private fun registerActivityLifecycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
               /* activity.applyEdgeToEdgeInsets(
                    false,
                    true,
                    ContextCompat.getDrawable(
                        activity,
                        R.drawable.bg_statusbar
                    )
                )*/
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}