package com.anyremote.remote.ui.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anyremote.remote.R
import com.anyremote.remote.ir.RemoteKey
import com.anyremote.remote.ir.RemoteKey.*
import com.anyremote.remote.ui.components.DPad
import com.anyremote.remote.ui.components.PowerKey
import com.anyremote.remote.ui.components.TextKey

private val directions = listOf(UP, LEFT, OK, RIGHT, DOWN)

private val labels = mapOf(UP to R.string.cd_up, DOWN to R.string.cd_down, LEFT to R.string.cd_left,
    RIGHT to R.string.cd_right, OK to R.string.ok, MENU to R.string.cd_menu,
    HOME to R.string.cd_home, BACK to R.string.back, MUTE to R.string.cd_mute,
    VOLUME_DOWN to R.string.cd_volume_down, VOLUME_UP to R.string.cd_volume_up,
    CHANNEL_DOWN to R.string.cd_channel_down, CHANNEL_UP to R.string.cd_channel_up,
    SOURCE to R.string.cd_source, FAN_SPEED to R.string.button_fan_speed, SPEED_UP to R.string.speed_plus,
    SPEED_DOWN to R.string.speed_minus, OSCILLATE to R.string.oscillate, MODE to R.string.ac_label_mode,
    TIMER to R.string.generic_timer, SHUTTER to R.string.cd_shutter,
    PLAY to R.string.media_play, PAUSE to R.string.media_pause, STOP to R.string.media_stop,
    PREVIOUS to R.string.media_previous, NEXT to R.string.media_next)

@Composable
internal fun ModelRemoteLayout(keys: Set<RemoteKey>, onKey: (RemoteKey) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (POWER in keys) PowerKey(onClick = { onKey(POWER) })
        val fullDpad = keys.containsAll(directions)
        if (fullDpad) DPad(onKey = onKey)
        val groups = listOf(
            if (fullDpad) emptyList() else directions,
            listOf(HOME, BACK, MENU, MUTE, SOURCE),
            listOf(VOLUME_DOWN, VOLUME_UP),
            listOf(CHANNEL_DOWN, CHANNEL_UP),
            listOf(PLAY, PAUSE, STOP),
            listOf(PREVIOUS, NEXT),
            listOf(FAN_SPEED, SPEED_DOWN, SPEED_UP),
            listOf(MODE, OSCILLATE, TIMER),
            listOf(SHUTTER),
        )
        for (group in groups) {
            val available = group.filter { it in keys }
            if (available.isEmpty()) continue
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                for (key in available) {
                    TextKey(
                        text = stringResource(labels.getValue(key)),
                        onClick = { onKey(key) },
                        height = 48.dp,
                        repeatOnHold = key in listOf(VOLUME_DOWN, VOLUME_UP, CHANNEL_DOWN, CHANNEL_UP, UP, DOWN, LEFT, RIGHT),
                    )
                }
            }
        }
        if (keys.any { it in DIGIT_0..DIGIT_9 || it == DASH || it == BACKSPACE }) NumberPad(onKey, keys)
    }
}
