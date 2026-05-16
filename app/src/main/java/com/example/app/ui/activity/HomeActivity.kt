package com.example.app.ui.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.example.app.R
import com.example.app.databinding.HomeActivityBinding
import com.example.app.ui.adapter.ViewPagerAdapter
import com.example.app.ui.base.BaseActivity
import com.example.app.ui.fragment.AddEventFragment
import com.example.app.ui.fragment.BluetoothFragment
import com.example.app.ui.fragment.HomeFragment
import com.example.app.ui.fragment.QRScannerFragment
import com.example.app.utils.AppUtil.applyEdgeToEdgeInsets
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity() {

    lateinit var binding: HomeActivityBinding


    override fun createViewBinding(): View {
        binding = HomeActivityBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun findFragmentPlaceHolder(): Int = R.id.placeHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpAdapter()
        setClickListener()

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            checkNotificationPermission() //Permission to check for android 13 notifications
//        }

//        binding.toolbar.textviewName.text = "Home"
//        load(HomeFragment::class.java).replace(false)
    }

    private fun setClickListener() = with(binding) {
        toolbar.imageviewAddEvent.setOnClickListener {
            loadActivity(
                IsolatedActivity::class.java,
                QRScannerFragment::class.java
            ).start()
        }

        toolbar.imageviewBluetooth.setOnClickListener {
            loadActivity(
                IsolatedActivity::class.java,
                BluetoothFragment::class.java
            ).start()
        }

        toolbar.imageviewLanguiage.setOnClickListener {

            val language =
                if (session.language == "en") "hi"
                else "en"

            changeLanguage(language)
        }

        toolbar.imageviewAddEvent.setOnClickListener {
            loadActivity(
                IsolatedActivity::class.java,
                AddEventFragment::class.java
            ).start()
        }

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
                        binding.toolbar.imageviewBluetooth.isVisible = true
                        binding.toolbar.imageviewLanguiage.isVisible = false

                       /* applyEdgeToEdgeInsets(
                            false,
                            true,
                            ContextCompat.getDrawable(this@HomeActivity, R.drawable.bg_statusbar)
                        )*/



                        binding.toolbar.textviewName.text = getString(R.string.home)
                    }

                    1 -> {
                        binding.toolbar.imageviewBluetooth.isVisible = false
                        binding.toolbar.imageviewLanguiage.isVisible = true
                       /* applyEdgeToEdgeInsets(
                            false,
                            true,
                            ContextCompat.getDrawable(this@HomeActivity, R.color.colorPrimary)
                        )*/


                        binding.toolbar.textviewName.text = getString(R.string.event)
                    }
                }

                binding.toolbar.imageviewAddEvent.isVisible = true/*(position == 1)*/

                Log.d("ViewPager", "Page selected: $position")
            }
        })

    }


    private fun changeLanguage(language: String) {

        session.language = language

        val intent = intent
        finish()
        startActivity(intent)
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
