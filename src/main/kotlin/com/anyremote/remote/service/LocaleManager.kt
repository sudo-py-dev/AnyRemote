package com.anyremote.remote.service

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import java.util.Locale

object LocaleManager {
    private const val PREFS_NAME = FavoriteManager.PREFS_NAME
    private const val KEY_LANG = "selected_language"

    var currentLanguage = mutableStateOf("system")
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentLanguage.value = prefs.getString(KEY_LANG, "system") ?: "system"
    }

    fun setLanguage(
        context: Context,
        lang: String,
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_LANG, lang) }
        currentLanguage.value = lang

        if (context is android.app.Activity) {
            context.recreate()
        }
    }

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, "system") ?: "system"
    }

    fun applyLocale(
        context: Context,
        lang: String,
    ): Context {
        if (lang == "system") return context

        val locale = Locale.forLanguageTag(lang)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}
