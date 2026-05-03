package com.example.app.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.Typeface
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils.*
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import androidx.core.net.toUri

object Extensions {
    fun View.isVisible(isVisible: Boolean) {
        if (isVisible) {
            this.visibility = View.VISIBLE
        } else {
            this.visibility = View.GONE
        }
    }


    fun showView(vararg viewToShow: View) {
        viewToShow.forEach {
            it.isVisible(true)
        }
    }

    fun hideView(vararg viewToHide: View) {
        viewToHide.forEach {
            it.isVisible(false)
        }
    }

    fun setNormalStyle(vararg views: View) {
        views.forEach { view ->
            if (view is TextView) {
                if (!view.text.isNullOrEmpty()) {
                    view.setTypeface(null, Typeface.NORMAL) // Set normal style for non-empty text
                } else {
                    view.setTypeface(null, Typeface.ITALIC) // Set italic style for empty text
                }
            }
        }
    }




    fun checkEmpty(vararg views: View): Boolean {
        return views.all { view ->
            if (view is TextView) {
                !view.text.isNullOrEmpty() // Check if TextView is not empty
            } else {
                true // Ignore non-TextView elements
            }
        }
    }


    val TextView.trimmedText get() = text.toString().trim()

    fun setStatusBarColor(activity: Activity, color: Int, isLightStatusBar: Boolean = true) {
        val window = activity.window
        val decorView = window.decorView

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(activity, color)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+): Use WindowInsetsController
            val controller = ViewCompat.getWindowInsetsController(decorView)
            controller?.isAppearanceLightStatusBars = isLightStatusBar
        } else {
            // Android 10 and below: Use systemUiVisibility
            decorView.systemUiVisibility = if (isLightStatusBar) {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // Make icons black
            } else {
                0 // Default white icons
            }
        }
    }



    @SuppressLint("ClickableViewAccessibility")
    fun AppCompatEditText.setDrawableRightClickListener(onClick: () -> Unit) {
        this.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = 2
                if (this.compoundDrawables[drawableRight] != null) {
                    val bounds: Rect = this.compoundDrawables[drawableRight].bounds
                    if (event.rawX >= (this.right - bounds.width() - this.paddingEnd)) {
                        onClick()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    fun getThumbPath(context: Context, videoPath: String): String {
        try {
            val thumb = createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND)
            val thumbFile: File = createImageFile(context)
            try {
                FileOutputStream(thumbFile).use { out ->
                    thumb?.compress(
                        Bitmap.CompressFormat.JPEG,
                        100,
                        out
                    ) // bmp is your Bitmap instance
                    return thumbFile.path
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return ""
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(context: Context): File {
        val timeStamp = "JPEG_${
            SimpleDateFormat("dd,MMMM yyyy-hh:mm:ss a", Locale.getDefault()).format(Date())
        }_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }


    fun createVideoThumbnail(context: Context, videoUrl: String): File? {
        val retriever = MediaMetadataRetriever()

        return try {
            // Set the data source for the video
            retriever.setDataSource(context, videoUrl.toUri())

            // Retrieve a frame at 1 second (1,000,000 microseconds)
            val bitmap =
                retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

            if (bitmap != null) {
                // Generate a random file name using UUID
                val randomFileName = "${UUID.randomUUID()}.jpg"
                val imageFile = File(context.cacheDir, randomFileName) // Save it in cache directory

                FileOutputStream(imageFile).use { outputStream ->
                    bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        90,
                        outputStream
                    ) // 90 for good quality
                    outputStream.flush()
                }
                imageFile // Return the saved image file
            } else {
                null // Return null if thumbnail creation fails
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null if any exception occurs
        } finally {
            retriever.release() // Always release the retriever
        }
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap): File {
        val file = File(context.cacheDir, "pdf_thumbnail.jpg")
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out) }
        return file
    }

    fun displayVideoThumbnail(context: Context, videoUrl: String, imageView: ImageView) {
        Glide.with(context)
            .asBitmap()
            .load(videoUrl)
            .apply(RequestOptions().frame(1000000)) // Retrieves a frame at 1 second (1,000,000 microseconds)
            .into(imageView)
    }

    fun genericTextWatcher(
        currentView: View,
        nextView: View?,
        callback: (String) -> Unit
    ): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                val text = editable.toString()
                callback(text)
                if (text.length == 1) nextView?.requestFocus()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
    }

    val <T> T.checkAllMatched: T
        get() = this

    inline fun <reified V : ViewBinding> ViewGroup.toBinding(): V {
        return V::class.java.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        ).invoke(null, LayoutInflater.from(context), this, false) as V
    }



    fun textCopy(textView: TextView) {
        textView.setOnClickListener {
            val textToCopy = textView.text.toString()

            // Get ClipboardManager
            val clipboardManager =
                textView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            // Copy the text directly without a label
            clipboardManager.setPrimaryClip(ClipData.newPlainText("Copied Text", textToCopy))

            // Optional: Show a confirmation message
            Toast.makeText(textView.context, "copy", Toast.LENGTH_SHORT).show()
        }
    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        if (model.startsWith(manufacturer)) {
            return capitalize(model)
        }
        return capitalize(manufacturer) + " " + model
    }

    private fun capitalize(str: String): String {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true
        var phrase = ""
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += c.uppercaseChar()
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase += c
        }
        return phrase
    }



    inline fun <reified T : Serializable> Bundle.serializable(key: String): T? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            val result = getSerializable(key, T::class.java)
            requireNotNull(result) { "Failed to retrieve serialized object with key: $key" }
            result
        }

        else -> @Suppress("DEPRECATION") getSerializable(key) as? T
    }

    inline fun <reified T : Serializable> Intent.serializable(key: String): T = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            val result = getSerializableExtra(key, T::class.java)
            requireNotNull(result) { "Failed to retrieve serialized object with key: $key" }
            result
        }

        else -> @Suppress("DEPRECATION") getSerializableExtra(key) as T
    }
}