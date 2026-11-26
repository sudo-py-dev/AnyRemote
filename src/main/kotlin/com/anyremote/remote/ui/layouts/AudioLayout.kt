package com.anyremote.remote.ui.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anyremote.remote.R
import com.anyremote.remote.ir.RemoteKey
import com.anyremote.remote.ui.components.*

private val audioSources =
    listOf(
        RemoteKey.SOURCE_BLUETOOTH to "BT",
        RemoteKey.SOURCE_HDMI to "HDMI",
        RemoteKey.SOURCE_OPTICAL to "OPT",
        RemoteKey.SOURCE_AUX to "AUX",
    )

@Composable
fun AudioLayout(onKeyPressed: (RemoteKey) -> Unit) {
    var power by remember { mutableStateOf(false) }
    var source by remember { mutableStateOf(RemoteKey.SOURCE_BLUETOOTH) }
    var volume by remember { mutableIntStateOf(15) }

    val sourceName =
        when (source) {
            RemoteKey.SOURCE_BLUETOOTH -> stringResource(R.string.audio_source_bluetooth)
            RemoteKey.SOURCE_HDMI -> stringResource(R.string.audio_source_hdmi)
            RemoteKey.SOURCE_OPTICAL -> stringResource(R.string.audio_source_optical)
            else -> stringResource(R.string.audio_source_aux)
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        StatusBanner(
            title =
                if (power) stringResource(R.string.audio_volume_banner, volume)
                else stringResource(R.string.audio_standby_banner),
            subtitle =
                if (power) stringResource(R.string.audio_source_banner, sourceName.uppercase()) else null,
            active = power,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PowerKey(
                onClick = {
                    power = !power
                    onKeyPressed(RemoteKey.POWER)
                },
                active = power,
            )
            RoundIconKey(
                Icons.AutoMirrored.Filled.VolumeMute,
                "Mute",
                { onKeyPressed(RemoteKey.MUTE) },
                enabled = power,
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TextKey(
                stringResource(R.string.vol_minus),
                onClick = {
                    if (volume > 0) volume--
                    onKeyPressed(RemoteKey.VOLUME_DOWN)
                },
                modifier = Modifier.weight(1f),
                enabled = power,
            )
            TextKey(
                stringResource(R.string.vol_plus),
                onClick = {
                    if (volume < 30) volume++
                    onKeyPressed(RemoteKey.VOLUME_UP)
                },
                modifier = Modifier.weight(1f),
                enabled = power,
            )
        }

        SegmentedSelector(
            options = audioSources,
            selected = source.takeIf { power },
            onSelect = {
                source = it
                onKeyPressed(it)
            },
            enabled = power,
        )
    }
}
