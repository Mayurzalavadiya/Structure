package com.example.app.utils.localization

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleManager {

    fun setLocale(context: Context, languageCode: String): Context {

        val locale = Locale(languageCode)

        Locale.setDefault(locale)

        val configuration = Configuration(
            context.resources.configuration
        )

        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }
}