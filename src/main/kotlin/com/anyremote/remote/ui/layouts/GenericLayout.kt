package com.anyremote.remote.ui.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anyremote.remote.R
import com.anyremote.remote.ir.RemoteKey
import com.anyremote.remote.ui.components.*

private val genericKeys =
    listOf(
        RemoteKey.TIMER to R.string.generic_timer,
        RemoteKey.MODE to R.string.generic_mode,
        RemoteKey.SPEED_UP to R.string.generic_speed_plus,
        RemoteKey.SPEED_DOWN to R.string.generic_speed_minus,
        RemoteKey.MUTE to R.string.generic_mute,
        RemoteKey.MENU to R.string.generic_menu,
    )

@Composable
fun GenericLayout(onKeyPressed: (RemoteKey) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        PowerKey(onClick = { onKeyPressed(RemoteKey.POWER) })

        genericKeys.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { (key, label) ->
                    TextKey(
                        stringResource(label),
                        onClick = { onKeyPressed(key) },
                        modifier = Modifier.weight(1f),
                        fontSize = 13,
                    )
                }
            }
        }
    }
}
