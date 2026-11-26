package com.anyremote.remote.ui.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anyremote.remote.R
import com.anyremote.remote.ir.RemoteKey
import com.anyremote.remote.ui.components.*
import com.anyremote.remote.ui.theme.HighlightBlue

private val numberPad =
    listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("-", "0", "⌫"),
    )

private val specialKeys = setOf("-", "⌫")

private fun padKey(label: String): RemoteKey =
    when (label) {
        "⌫" -> RemoteKey.BACKSPACE
        "-" -> RemoteKey.DASH
        else -> RemoteKey.digit(label.toInt())
    }

@Composable
fun TvLayout(onKeyPressed: (RemoteKey) -> Unit) {
    var showNumericPad by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            PowerKey(onClick = { onKeyPressed(RemoteKey.POWER) }, size = 54.dp)
            RoundIconKey(Icons.AutoMirrored.Filled.Input, stringResource(R.string.cd_source), { onKeyPressed(RemoteKey.SOURCE) })
            RemoteButton(
                onClick = { showNumericPad = !showNumericPad },
                modifier = Modifier.size(54.dp),
                highlighted = showNumericPad,
            ) {
                Text(
                    stringResource(R.string.num_pad),
                    color = if (showNumericPad) HighlightBlue else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
            RoundIconKey(Icons.AutoMirrored.Filled.VolumeMute, stringResource(R.string.cd_mute), { onKeyPressed(RemoteKey.MUTE) })
        }

        if (showNumericPad) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            ) {
                numberPad.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        row.forEach { label ->
                            val isSpecial = label in specialKeys
                            RemoteButton(
                                onClick = { onKeyPressed(padKey(label)) },
                                modifier = Modifier.weight(1f).height(58.dp),
                                shape = RoundedCornerShape(14.dp),
                                background =
                                    if (isSpecial) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                    else null,
                            ) {
                                Text(
                                    label,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                )
                            }
                        }
                    }
                }
            }
        } else {
            DPad(onKey = onKeyPressed)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            RoundIconKey(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), { onKeyPressed(RemoteKey.BACK) })
            RoundIconKey(Icons.Default.Home, stringResource(R.string.cd_home), { onKeyPressed(RemoteKey.HOME) })
            RoundIconKey(Icons.Default.Menu, stringResource(R.string.cd_menu), { onKeyPressed(RemoteKey.MENU) })
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            RockerKey(
                label = stringResource(R.string.vol),
                upIcon = Icons.Default.Add,
                downIcon = Icons.Default.Remove,
                upDescription = stringResource(R.string.cd_volume_up),
                downDescription = stringResource(R.string.cd_volume_down),
                onUp = { onKeyPressed(RemoteKey.VOLUME_UP) },
                onDown = { onKeyPressed(RemoteKey.VOLUME_DOWN) },
                modifier = Modifier.weight(1f),
            )
            RockerKey(
                label = stringResource(R.string.ch),
                upIcon = Icons.Default.KeyboardArrowUp,
                downIcon = Icons.Default.KeyboardArrowDown,
                upDescription = stringResource(R.string.cd_channel_up),
                downDescription = stringResource(R.string.cd_channel_down),
                onUp = { onKeyPressed(RemoteKey.CHANNEL_UP) },
                onDown = { onKeyPressed(RemoteKey.CHANNEL_DOWN) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
