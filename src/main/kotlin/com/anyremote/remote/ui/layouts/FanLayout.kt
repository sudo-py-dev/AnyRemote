package com.anyremote.remote.ui.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anyremote.remote.R
import com.anyremote.remote.ir.RemoteKey
import com.anyremote.remote.ui.components.*
import com.anyremote.remote.ui.theme.HighlightBlue

private enum class FanMode(val label: Int) {
    NORMAL(R.string.fan_mode_normal),
    NATURAL(R.string.fan_mode_natural),
    SLEEP(R.string.fan_mode_sleep);

    fun next() = entries[(ordinal + 1) % entries.size]
}

@Composable
fun FanLayout(onKeyPressed: (RemoteKey) -> Unit) {
    var power by remember { mutableStateOf(false) }
    var speed by remember { mutableIntStateOf(1) }
    var oscillate by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(FanMode.NORMAL) }

    val modeName = stringResource(mode.label)
    val oscillateStatus = stringResource(if (oscillate) R.string.on_word else R.string.off_word)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        StatusBanner(
            title =
                if (power) stringResource(R.string.fan_speed_banner, speed)
                else stringResource(R.string.fan_power_off_banner),
            subtitle =
                if (power) stringResource(R.string.fan_status_banner, modeName.uppercase(), oscillateStatus)
                else null,
            active = power,
        )

        PowerKey(
            onClick = {
                power = !power
                onKeyPressed(RemoteKey.POWER)
            },
            active = power,
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TextKey(
                stringResource(R.string.speed_minus),
                onClick = {
                    if (speed > 1) speed--
                    onKeyPressed(RemoteKey.SPEED_DOWN)
                },
                modifier = Modifier.weight(1f),
                enabled = power,
            )
            TextKey(
                stringResource(R.string.speed_plus),
                onClick = {
                    if (speed < 4) speed++
                    onKeyPressed(RemoteKey.SPEED_UP)
                },
                modifier = Modifier.weight(1f),
                enabled = power,
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TextKey(
                stringResource(R.string.oscillate),
                onClick = {
                    oscillate = !oscillate
                    onKeyPressed(RemoteKey.OSCILLATE)
                },
                modifier = Modifier.weight(1f),
                enabled = power,
                highlighted = oscillate && power,
            )
            TextKey(
                modeName.uppercase(),
                onClick = {
                    mode = mode.next()
                    onKeyPressed(RemoteKey.MODE)
                },
                modifier = Modifier.weight(1f),
                enabled = power,
                textColor = if (power) HighlightBlue else null,
            )
        }
    }
}
