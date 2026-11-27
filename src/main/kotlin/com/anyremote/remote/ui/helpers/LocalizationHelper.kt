package com.anyremote.remote.ui.helpers

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.anyremote.remote.R
import com.anyremote.remote.data.RemoteDatabase.DeviceType

private fun deviceNameRes(deviceId: Int): Int? =
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

fun getLocalizedDeviceName(context: Context, deviceId: Int, fallback: String): String =
    deviceNameRes(deviceId)?.let(context::getString) ?: fallback

@Composable
fun DeviceType.localizedName(): String = deviceNameRes(id)?.let { stringResource(it) } ?: nameEn

fun DeviceType.getLocalizedName(context: Context): String {
    return getLocalizedDeviceName(context, id, nameEn)
}

/** Name a saved remote shows until the user renames it, e.g. "Samsung TV". */
fun defaultRemoteName(
    context: Context,
    device: DeviceType,
    brand: com.anyremote.remote.data.RemoteDatabase.Brand,
): String = "${brand.nameEn} ${device.getLocalizedName(context).substringBefore(" ")}"
