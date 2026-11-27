package com.anyremote.remote.ui.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WindPower
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anyremote.remote.R
import com.anyremote.remote.ir.RemoteKey
import com.anyremote.remote.ui.components.*
import kotlinx.coroutines.launch

private enum class FanMode(val label: Int, val icon: ImageVector) {
    NORMAL(R.string.fan_mode_normal, Icons.Default.Air),
    NATURAL(R.string.fan_mode_natural, Icons.Default.Forest),
    SLEEP(R.string.fan_mode_sleep, Icons.Default.Bedtime);

    fun next() = entries[(ordinal + 1) % entries.size]
}

private const val MAX_SPEED = 4

/**
 * IR fans don't report state, so speed, mode and oscillation here are the app's best guess from
 * what it sent. They reset when the screen is recreated from scratch.
 */
@Composable
fun FanLayout(onKeyPressed: suspend (RemoteKey) -> Boolean) {
    var power by rememberSaveable { mutableStateOf(false) }
    var speed by rememberSaveable { mutableIntStateOf(1) }
    var oscillate by rememberSaveable { mutableStateOf(false) }
    var mode by rememberSaveable { mutableStateOf(FanMode.NORMAL) }

    val scope = rememberCoroutineScope()
    var sending by remember { mutableStateOf(false) }

    fun send(key: RemoteKey, onSent: () -> Unit) {
        if (sending) return
        sending = true
        scope.launch {
            try {
                if (onKeyPressed(key)) onSent()
            } finally {
                sending = false
            }
        }
    }

    val modeName = stringResource(mode.label)
    val oscillateStatus = stringResource(if (oscillate) R.string.on_word else R.string.off_word)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        StatusCard(
            title =
                if (power) stringResource(R.string.fan_speed_banner, speed)
                else stringResource(R.string.fan_power_off_banner),
            subtitle =
                if (power) stringResource(R.string.fan_status_banner, modeName, oscillateStatus)
                else null,
            active = power,
            icon = Icons.Default.WindPower,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconKey(
                Icons.Default.Remove,
                stringResource(R.string.speed_minus),
                {
                    send(RemoteKey.SPEED_DOWN) { if (speed > 1) speed-- }
                },
                size = 64.dp,
                enabled = power && !sending,
            )
            PowerKey(
                onClick = {
                    send(RemoteKey.POWER) { power = !power }
                },
                active = power,
                size = 80.dp,
            )
            IconKey(
                Icons.Default.Add,
                stringResource(R.string.speed_plus),
                {
                    send(RemoteKey.SPEED_UP) { if (speed < MAX_SPEED) speed++ }
                },
                size = 64.dp,
                enabled = power && !sending,
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            OptionKey(
                icon = Icons.AutoMirrored.Filled.CompareArrows,
                label = stringResource(R.string.oscillate),
                selected = power && oscillate,
                enabled = power && !sending,
                onClick = {
                    send(RemoteKey.OSCILLATE) { oscillate = !oscillate }
                },
            )
            OptionKey(
                icon = mode.icon,
                label = modeName,
                selected = power && mode != FanMode.NORMAL,
                enabled = power && !sending,
                onClick = {
                    send(RemoteKey.MODE) { mode = mode.next() }
                },
            )
        }
    }
}
