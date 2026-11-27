package com.anyremote.remote.ui.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anyremote.remote.R
import com.anyremote.remote.ir.RemoteKey
import com.anyremote.remote.ui.components.*

private const val MAX_VOLUME = 30

@Composable
fun AudioLayout(onKeyPressed: (RemoteKey) -> Unit) {
    var power by rememberSaveable { mutableStateOf(false) }
    var source by rememberSaveable { mutableStateOf(RemoteKey.SOURCE_BLUETOOTH) }
    var volume by rememberSaveable { mutableIntStateOf(15) }

    val sources =
        listOf(
            RemoteKey.SOURCE_BLUETOOTH to stringResource(R.string.audio_source_bluetooth),
            RemoteKey.SOURCE_HDMI to stringResource(R.string.audio_source_hdmi),
            RemoteKey.SOURCE_OPTICAL to stringResource(R.string.audio_source_optical),
            RemoteKey.SOURCE_AUX to stringResource(R.string.audio_source_aux),
        )
    val sourceName = sources.first { it.first == source }.second

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        StatusCard(
            title =
                if (power) stringResource(R.string.audio_volume_banner, volume)
                else stringResource(R.string.audio_standby_banner),
            subtitle =
                if (power) stringResource(R.string.audio_source_banner, sourceName) else null,
            active = power,
            icon = Icons.Default.Speaker,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PowerKey(
                onClick = {
                    power = !power
                    onKeyPressed(RemoteKey.POWER)
                },
                active = power,
                size = 72.dp,
            )
            RockerKey(
                label = stringResource(R.string.vol),
                upIcon = Icons.Default.Add,
                downIcon = Icons.Default.Remove,
                upDescription = stringResource(R.string.cd_volume_up),
                downDescription = stringResource(R.string.cd_volume_down),
                onUp = {
                    if (volume < MAX_VOLUME) volume++
                    onKeyPressed(RemoteKey.VOLUME_UP)
                },
                onDown = {
                    if (volume > 0) volume--
                    onKeyPressed(RemoteKey.VOLUME_DOWN)
                },
                enabled = power,
            )
            IconKey(
                Icons.AutoMirrored.Filled.VolumeOff,
                stringResource(R.string.cd_mute),
                { onKeyPressed(RemoteKey.MUTE) },
                size = 72.dp,
                enabled = power,
            )
        }

        RemoteSection(title = stringResource(R.string.cd_source)) {
            SegmentedSelector(
                options = sources,
                selected = source.takeIf { power },
                onSelect = {
                    source = it
                    onKeyPressed(it)
                },
                enabled = power,
            )
        }
    }
}
