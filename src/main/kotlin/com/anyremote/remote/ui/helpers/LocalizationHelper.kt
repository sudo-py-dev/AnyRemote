package com.anyremote.remote.ui.helpers

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.anyremote.remote.R
import com.anyremote.remote.data.RemoteDatabase.DeviceType

fun getLocalizedDeviceName(context: Context, deviceId: Int, fallback: String): String {
    val resId =
        when (deviceId) {
            1 -> R.string.category_tv
            2 -> R.string.category_set_top_box
            3 -> R.string.category_ac
            4 -> R.string.category_dvd_player
            6 -> R.string.category_fan
            8 -> R.string.category_av_receiver
            10 -> R.string.category_projector
            11 -> R.string.category_cable_satellite_box
            12 -> R.string.category_box
            13 -> R.string.category_camera
            else -> null
        }
    return if (resId != null) context.getString(resId) else fallback
}

@Composable
fun DeviceType.localizedName(): String {
    val resId =
        when (id) {
            1 -> R.string.category_tv
            2 -> R.string.category_set_top_box
            3 -> R.string.category_ac
            4 -> R.string.category_dvd_player
            6 -> R.string.category_fan
            8 -> R.string.category_av_receiver
            10 -> R.string.category_projector
            11 -> R.string.category_cable_satellite_box
            12 -> R.string.category_box
            13 -> R.string.category_camera
            else -> null
        }
    return if (resId != null) stringResource(resId) else nameEn
}

fun DeviceType.getLocalizedName(context: Context): String {
    return getLocalizedDeviceName(context, id, nameEn)
}
