package com.anyremote.remote.ui.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.anyremote.remote.service.FavoriteManager

@Composable
fun rememberVibratingHapticFeedback(): HapticFeedback {
    val rawHaptic = LocalHapticFeedback.current
    val context = LocalContext.current
    return remember(rawHaptic, context) {
        object : HapticFeedback {
            override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
                if (FavoriteManager.isVibrationEnabled(context)) {
                    rawHaptic.performHapticFeedback(hapticFeedbackType)
                }
            }
        }
    }
}
