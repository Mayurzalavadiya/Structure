package com.starter.app.ui.activity

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.starter.app.databinding.SplashActivityBinding
import com.starter.app.ui.base.BaseActivity

class SplashActivity : BaseActivity() {
    //Data store on after user login
    lateinit var splashActivityBinding: SplashActivityBinding
    override fun findFragmentPlaceHolder(): Int {
        return 0
    }

    override fun createViewBinding(): View {
        splashActivityBinding = SplashActivityBinding.inflate(layoutInflater)
        return splashActivityBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Handler(Looper.getMainLooper()).postDelayed({

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkNotificationPermission() //Permission to check for android 13 notifications
            }

            loadActivity(
               HomeActivity::class.java
            ).byFinishingCurrent().start()

            //loadActivity(HomeActivity::class.java).byFinishingCurrent().start()

        }, 2000)
    }

}