package com.anyremote.remote.ui.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
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

private val labels = mapOf(HOME to R.string.cd_home, BACK to R.string.back, MUTE to R.string.cd_mute,
    VOLUME_DOWN to R.string.cd_volume_down, VOLUME_UP to R.string.cd_volume_up,
    CHANNEL_DOWN to R.string.cd_channel_down, CHANNEL_UP to R.string.cd_channel_up)

@Composable
internal fun ModelRemoteLayout(keys: Set<RemoteKey>, onKey: (RemoteKey) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (POWER in keys) PowerKey(onClick = { onKey(POWER) })
        if (OK in keys) DPad(onKey = onKey)
        for (group in listOf(listOf(HOME, BACK, MUTE), listOf(VOLUME_DOWN, VOLUME_UP), listOf(CHANNEL_DOWN, CHANNEL_UP))) {
            val available = group.filter { it in keys }
            if (available.isEmpty()) continue
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                for (key in available) {
                    FilledTonalButton(onClick = { onKey(key) }) { Text(stringResource(labels.getValue(key))) }
                }
            }
        }
    }
}
