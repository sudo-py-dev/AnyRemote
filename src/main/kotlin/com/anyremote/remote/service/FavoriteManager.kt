package com.anyremote.remote.service

import android.content.Context

object FavoriteManager {
    private const val PREFS_NAME = "mi_favorites_prefs"
    private const val KEY_FAVORITES = "favorites_list"
    private const val KEY_VIBRATION = "vibration_enabled"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_IR_NOTICE_DISMISSED = "ir_notice_dismissed"
    private const val KEY_WIDGET_AC_POWER = "widget_ac_power"

    var currentThemeState = androidx.compose.runtime.mutableStateOf("light")
        private set

    var currentVibrationState = androidx.compose.runtime.mutableStateOf(true)
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentThemeState.value = prefs.getString(KEY_THEME, "light") ?: "light"
        currentVibrationState.value = prefs.getBoolean(KEY_VIBRATION, true)
    }

    fun getThemeMode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME, "light") ?: "light"
    }

    fun setThemeMode(
        context: Context,
        mode: String,
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, mode).apply()
        currentThemeState.value = mode
    }

    fun toggleFavorite(
        context: Context,
        deviceId: Int,
        brandId: Int,
    ): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val favorites = getFavorites(context).toMutableSet()
        val item = "${deviceId}_$brandId"
        val isAdded: Boolean
        if (favorites.contains(item)) {
            favorites.remove(item)
            isAdded = false
        } else {
            favorites.add(item)
            isAdded = true
        }
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply()
        return isAdded
    }

    fun isFavorite(
        context: Context,
        deviceId: Int,
        brandId: Int,
    ): Boolean = getFavorites(context).contains("${deviceId}_$brandId")

    fun getFavorites(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun isVibrationEnabled(context: Context): Boolean = currentVibrationState.value

    fun setVibrationEnabled(
        context: Context,
        enabled: Boolean,
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
        currentVibrationState.value = enabled
    }

    fun getCustomName(
        context: Context,
        deviceId: Int,
        brandId: Int,
        defaultName: String,
    ): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("name_${deviceId}_$brandId", defaultName) ?: defaultName
    }

    fun setCustomName(
        context: Context,
        deviceId: Int,
        brandId: Int,
        customName: String,
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("name_${deviceId}_$brandId", customName).apply()
    }

    /** Codeset the user confirmed in the match wizard for this device/brand (1 if never matched). */
    fun getCodeset(
        context: Context,
        deviceId: Int,
        brandId: Int,
    ): Int = prefs(context).getInt("codeset_${deviceId}_$brandId", 1)

    fun setCodeset(
        context: Context,
        deviceId: Int,
        brandId: Int,
        codeset: Int,
    ) {
        prefs(context).edit().putInt("codeset_${deviceId}_$brandId", codeset).apply()
    }

    /** Last AC power state the home-screen widget sent, so its Power button can toggle. */
    fun getWidgetAcPower(context: Context): Boolean = prefs(context).getBoolean(KEY_WIDGET_AC_POWER, false)

    fun setWidgetAcPower(
        context: Context,
        on: Boolean,
    ) {
        prefs(context).edit().putBoolean(KEY_WIDGET_AC_POWER, on).apply()
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isIrNoticeDismissed(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IR_NOTICE_DISMISSED, false)
    }

    fun setIrNoticeDismissed(
        context: Context,
        dismissed: Boolean,
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IR_NOTICE_DISMISSED, dismissed).apply()
    }
}
