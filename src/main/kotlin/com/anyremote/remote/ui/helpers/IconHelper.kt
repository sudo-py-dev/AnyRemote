package com.anyremote.remote.ui.helpers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Dvr
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun getDeviceIcon(deviceId: Int): ImageVector {
    return when (deviceId) {
        1 -> Icons.Default.Tv
        2 -> Icons.Default.Router
        3 -> Icons.Default.AcUnit
        4 -> Icons.AutoMirrored.Filled.Dvr
        6 -> Icons.Default.WindPower
        8 -> Icons.Default.Speaker
        10 -> Icons.Default.SettingsInputHdmi
        11 -> Icons.Default.SettingsInputAntenna
        12 -> Icons.Default.Cast
        13 -> Icons.Default.PhotoCamera
        else -> Icons.Default.Power
    }
}
