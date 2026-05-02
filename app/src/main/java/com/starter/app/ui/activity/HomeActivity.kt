package com.starter.app.ui.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.starter.app.R
import com.starter.app.databinding.HomeActivityBinding
import com.starter.app.ui.adapter.ViewPagerAdapter
import com.starter.app.ui.base.BaseActivity
import com.starter.app.ui.fragment.AddEventFragment
import com.starter.app.ui.fragment.QRScannerFragment
import com.starter.app.ui.home.fragment.MainFragment
import com.starter.app.ui.home.fragment.OrderFragment
import com.starter.app.utils.AppUtil.applyEdgeToEdgeInsets
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity() {

    lateinit var binding: HomeActivityBinding


    override fun createViewBinding(): View {
        binding = HomeActivityBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun findFragmentPlaceHolder(): Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpAdapter()
        setClickListener()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission() //Permission to check for android 13 notifications
        }
    }

    private fun setClickListener() = with(binding) {

    }

    private fun setUpAdapter() = with(binding) {

        val adapter = ViewPagerAdapter(this@HomeActivity)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "API"
                1 -> "Event"
                else -> ""
            }
        }.attach()


        // 🚀 Add tab selected listener
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)


                when (position) {
                    0 -> {
                        applyEdgeToEdgeInsets(
                            false,
                            true,
                            ContextCompat.getDrawable(this@HomeActivity, R.drawable.bg_statusbar)
                        )
                        toolbar.imageviewAddEvent.setOnClickListener {
                            loadActivity(
                                IsolatedActivity::class.java,
                                QRScannerFragment::class.java
                            ).start()
                        }
                        binding.toolbar.textviewName.text = "Home"
                    }

                    1 -> {
                        applyEdgeToEdgeInsets(
                            false,
                            true,
                            ContextCompat.getDrawable(this@HomeActivity, R.color.colorPrimary)
                        )
                        toolbar.imageviewAddEvent.setOnClickListener {
                            loadActivity(
                                IsolatedActivity::class.java,
                                AddEventFragment::class.java
                            ).start()
                        }
                        binding.toolbar.textviewName.text = "Event"
                    }
                }
                binding.toolbar.imageviewAddEvent.isVisible = true/*(position == 1)*/

                Log.d("ViewPager", "Page selected: $position")
            }
        })

    }


    override fun onBackActionPerform(): Boolean = with(binding)
    {
        if (binding.viewPager.currentItem != 0) {
            // Not on first tab → go to first tab, don't close activity
            binding.viewPager.currentItem = 0
            return false
        } else {
            return true
        }
    }
}
