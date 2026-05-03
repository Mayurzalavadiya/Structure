package com.example.app.utils

import android.util.Log.WARN
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import okhttp3.internal.platform.Platform
import okhttp3.internal.platform.Platform.Companion.INFO
import okhttp3.logging.HttpLoggingInterceptor

object PrettyLogger : HttpLoggingInterceptor.Logger {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    override fun log(message: String) {
        val trimMessage = message.trim { it <= ' ' }
        if (trimMessage.startsWith("{") && trimMessage.endsWith("}") || trimMessage.startsWith("[") && trimMessage.endsWith(
                "]"
            )
        ) {
            try {
                val prettyJson = gson.toJson(JsonParser().parse(message))
                Platform.get().log(
                    level = WARN,
                    message = "(👉ﾟヮﾟ)👉 =====> Start <===== 👈(ﾟヮﾟ👈)",
                    t = null
                )
                Platform.get().log(level = INFO, message = prettyJson, t = null)
                Platform.get().log(
                    level = WARN,
                    message = "(👉ﾟヮﾟ)👉 =====> End: <====== 👈(ﾟヮﾟ👈)",
                    t = null
                )
            } catch (e: java.lang.Exception) {
                Platform.get().log(level = WARN, message = message, t = e)
            }
        } else {
            Platform.get().log(level = INFO, message = message, t = null)
        }
    }
}