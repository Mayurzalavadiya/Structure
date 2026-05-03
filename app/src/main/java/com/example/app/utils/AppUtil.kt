package com.example.app.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.core.view.isGone

object AppUtil {

    const val STATUS_BAR_TAG = "status_bar"

    /**
     * Applies edge-to-edge insets and draws a custom status bar background.
     *
     * @param statusBarDrawable Drawable to be shown behind the status bar.
     */
    fun Activity.applyEdgeToEdgeInsets(
        isLight: Boolean = false,
        isSetPadding: Boolean = true,
        statusBarDrawable: Drawable?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val view = findViewById<View>(android.R.id.content)

            ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
                val bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or
                            WindowInsetsCompat.Type.displayCutout() or
                            WindowInsetsCompat.Type.ime()
                )

                val statusBarHeight = windowInsets
                    .getInsets(WindowInsetsCompat.Type.statusBars())
                    .top

                applyStatusBarColor(
                    window,
                    statusBarDrawable,
                    isDecor = true,
                    height = statusBarHeight
                )

                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = isLight
                    isAppearanceLightNavigationBars = true
                }

                if (isSetPadding) {
                    v.updatePadding(
                        left = bars.left,
                        top = bars.top,
                        right = bars.right,
                        bottom = bars.bottom
                    )
                } else {
                    v.setPadding(0, 0, 0, 0)
                }

                windowInsets
            }

            // 👇 Force reapply of insets immediately
            ViewCompat.requestApplyInsets(view)
        }
    }

    fun Fragment.applyEdgeToEdgeInsets(
        isLight: Boolean = false,
        isSetPadding: Boolean = true,
        statusBarDrawable: Drawable?
    ) {
        if (isAdded) {
            requireActivity().applyEdgeToEdgeInsets(isLight, isSetPadding, statusBarDrawable)
        } else {
            Log.w("StatusBar", "Fragment not attached. Skipping applyEdgeToEdgeInsets")
        }
    }

    /**
     * Adds or updates a fake status bar view with a given background.
     */
    fun applyStatusBarColor(
        window: Window,
        statusBarBackground: Drawable?,
        isDecor: Boolean,
        height: Int
    ): View {
        val parent = if (isDecor) {
            window.decorView as ViewGroup
        } else {
            window.findViewById<View>(android.R.id.content) as ViewGroup
        }

        var fakeStatusBarView = parent.findViewWithTag<View>(STATUS_BAR_TAG)

        if (fakeStatusBarView != null) {
            if (fakeStatusBarView.isGone) {
                fakeStatusBarView.visibility = View.VISIBLE
            }
            fakeStatusBarView.background = statusBarBackground
        } else {
            fakeStatusBarView = createStatusBarView(window.context, statusBarBackground, height)
            parent.addView(fakeStatusBarView)
        }

        return fakeStatusBarView
    }

    /**
     * Creates a fake status bar view with specified height and background.
     */
    private fun createStatusBarView(
        context: Context,
        statusBarBackground: Drawable?,
        height: Int
    ): View {
        return View(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
            )
            background = statusBarBackground
            tag = STATUS_BAR_TAG
        }
    }
}
