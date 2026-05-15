package com.example.app.ui.activity

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.example.app.databinding.SplashActivityBinding
import com.example.app.ui.base.BaseActivity

class SplashActivity : BaseActivity() {

    lateinit var splashActivityBinding: SplashActivityBinding

    private val handler = Handler(Looper.getMainLooper())

    private val runnable = Runnable {

        if (isFinishing || isDestroyed) return@Runnable

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission()
        }

        loadActivity(
            HomeActivity::class.java
        ).byFinishingCurrent().start()
    }

    override fun findFragmentPlaceHolder(): Int {
        return 0
    }

    override fun createViewBinding(): View {
        splashActivityBinding = SplashActivityBinding.inflate(layoutInflater)
        return splashActivityBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler.postDelayed(runnable, 2000)
    }

    override fun goBack() {
        super.goBack()
        handler.removeCallbacks(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Prevent callback after activity closed
        handler.removeCallbacks(runnable)
    }
}