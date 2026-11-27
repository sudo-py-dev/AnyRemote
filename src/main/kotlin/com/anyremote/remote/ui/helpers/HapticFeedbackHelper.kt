package com.anyremote.remote.ui.helpers

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalView
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.anyremote.remote.service.FavoriteManager

@Composable
fun rememberVibratingHapticFeedback(): HapticFeedback {
    val view = LocalView.current
    return remember(view) {
        val vibrator = view.context.getSystemService(Vibrator::class.java)
        object : HapticFeedback {
            override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
                if (!FavoriteManager.currentVibrationState.value) return
                val feedback = when (hapticFeedbackType) {
                    HapticFeedbackType.Confirm -> HapticFeedbackConstantsCompat.CONFIRM
                    HapticFeedbackType.Reject -> HapticFeedbackConstantsCompat.REJECT
                    HapticFeedbackType.LongPress -> HapticFeedbackConstantsCompat.LONG_PRESS
                    HapticFeedbackType.SegmentTick -> HapticFeedbackConstantsCompat.SEGMENT_TICK
                    HapticFeedbackType.ToggleOn -> HapticFeedbackConstantsCompat.TOGGLE_ON
                    else -> HapticFeedbackConstantsCompat.KEYBOARD_TAP
                }
                if (ViewCompat.performHapticFeedback(view, feedback)) return
                if (view.isHapticFeedbackEnabled) {
                    vibrator?.playTouchFallback(view.context)
                }
            }
        }
    }
}

private fun Vibrator.playTouchFallback(context: Context) {
    if (!hasVibrator()) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        vibrate(
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK),
            VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH),
        )
    } else {
        vibrateLegacyTouch(context)
    }
}

@Suppress("DEPRECATION")
private fun Vibrator.vibrateLegacyTouch(context: Context) {
    val enabled = Settings.System.getInt(
        context.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1,
    ) != 0
    if (!enabled) return

    val effect =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
        } else {
            VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE)
        }
    val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
    vibrate(effect, attributes)
}
