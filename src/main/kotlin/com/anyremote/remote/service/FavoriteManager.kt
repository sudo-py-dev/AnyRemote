package com.anyremote.remote.service

import android.content.Context
import androidx.core.content.edit

object FavoriteManager {
    internal const val PREFS_NAME = "mi_favorites_prefs"
    private const val KEY_FAVORITES = "favorites_list"
    private const val KEY_VIBRATION = "vibration_enabled"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_DYNAMIC_COLOR = "dynamic_color"
    private const val KEY_IR_NOTICE_DISMISSED = "ir_notice_dismissed"
    private const val KEY_WIDGET_AC_POWER = "widget_ac_power"

    var currentThemeState = androidx.compose.runtime.mutableStateOf("system")
        private set

    var currentDynamicColorState = androidx.compose.runtime.mutableStateOf(true)
        private set

    var currentVibrationState = androidx.compose.runtime.mutableStateOf(true)
        private set

    fun init(context: Context) {
        val prefs = prefs(context)
        currentThemeState.value = prefs.getString(KEY_THEME, "system") ?: "system"
        currentDynamicColorState.value = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        currentVibrationState.value = prefs.getBoolean(KEY_VIBRATION, true)
    }

    fun setThemeMode(
        context: Context,
        mode: String,
    ) {
        val prefs = prefs(context)
        prefs.edit { putString(KEY_THEME, mode) }
        currentThemeState.value = mode
    }

    fun setDynamicColor(
        context: Context,
        enabled: Boolean,
    ) {
        prefs(context).edit { putBoolean(KEY_DYNAMIC_COLOR, enabled) }
        currentDynamicColorState.value = enabled
    }

    fun toggleFavorite(
        context: Context,
        deviceId: Int,
        brandId: Int,
    ): Boolean {
        val prefs = prefs(context)
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
        prefs.edit { putStringSet(KEY_FAVORITES, favorites) }
        ShortcutHelper.syncSavedShortcut(context, deviceId, brandId)
        return isAdded
    }

    fun isFavorite(
        context: Context,
        deviceId: Int,
        brandId: Int,
    ): Boolean = getFavorites(context).contains("${deviceId}_$brandId")

    fun getFavorites(context: Context): Set<String> {
        val prefs = prefs(context)
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun setVibrationEnabled(
        context: Context,
        enabled: Boolean,
    ) {
        val prefs = prefs(context)
        prefs.edit { putBoolean(KEY_VIBRATION, enabled) }
        currentVibrationState.value = enabled
    }

    fun getCustomName(
        context: Context,
        deviceId: Int,
        brandId: Int,
        defaultName: String,
    ): String {
        val prefs = prefs(context)
        return prefs.getString("name_${deviceId}_$brandId", defaultName) ?: defaultName
    }

    fun setCustomName(
        context: Context,
        deviceId: Int,
        brandId: Int,
        customName: String,
    ) {
        if (!RemoteName.isValid(customName)) return
        prefs(context).edit { putString("name_${deviceId}_$brandId", customName.trim()) }
    }

    /**
     * Codeset the user confirmed in the match wizard for this device/brand (1 if never matched).
     */
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
        prefs(context).edit { putInt("codeset_${deviceId}_$brandId", codeset) }
    }

    /** Last AC power state the home-screen widget sent, so its Power button can toggle. */
    fun getWidgetAcPower(context: Context): Boolean =
        prefs(context).getBoolean(KEY_WIDGET_AC_POWER, false)

    fun setWidgetAcPower(
        context: Context,
        on: Boolean,
    ) {
        prefs(context).edit { putBoolean(KEY_WIDGET_AC_POWER, on) }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isIrNoticeDismissed(context: Context): Boolean {
        val prefs = prefs(context)
        return prefs.getBoolean(KEY_IR_NOTICE_DISMISSED, false)
    }

    fun setIrNoticeDismissed(
        context: Context,
        dismissed: Boolean,
    ) {
        val prefs = prefs(context)
        prefs.edit { putBoolean(KEY_IR_NOTICE_DISMISSED, dismissed) }
    }
}
