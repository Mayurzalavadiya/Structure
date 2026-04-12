package com.starter.app.utils

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.starter.app.R
import androidx.core.graphics.toColorInt

object ProgressHelper {

    private var dialog: AlertDialog? = null

    fun showDialog(context: Context, message: String?) {
        if (dialog == null) {
            val padding = 30

            val linearLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(padding, padding, padding, padding)
                gravity = Gravity.CENTER
                setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                }
            }

            val progressBar = ProgressBar(context).apply {
                isIndeterminate = true
                setPadding(0, 0, padding, 0)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                }
            }

            val tvText = TextView(context).apply {
                text = message
                setTextColor("#000000".toColorInt())
                textSize = 20f
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                }
            }

            linearLayout.apply {
//                addView(progressBar)
                addView(tvText)
            }

            val builder = AlertDialog.Builder(context).apply {
                setCancelable(true)
                setView(linearLayout)
            }

            dialog = builder.create()
            dialog?.show()

            dialog?.window?.apply {
                val layoutParams = attributes
                layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                attributes = layoutParams
            }
        }
    }

    val isDialogVisible: Boolean
        get() = if (dialog != null) {
            dialog?.isShowing == true
        } else {
            false
        }

    fun dismissDialog() {
        if (dialog != null) {
            dialog?.dismiss()
            dialog = null
        }
    }
}