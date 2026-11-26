package com.anyremote.remote.ui.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anyremote.remote.R
import com.anyremote.remote.ir.RemoteKey
import com.anyremote.remote.ui.components.*

private val numberPad =
    listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("-", "0", "⌫"),
    )

private fun padKey(label: String): RemoteKey =
    when (label) {
        "⌫" -> RemoteKey.BACKSPACE
        "-" -> RemoteKey.DASH
        else -> RemoteKey.digit(label.toInt())
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvLayout(onKeyPressed: (RemoteKey) -> Unit) {
    var showNumericPad by rememberSaveable { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PowerKey(onClick = { onKeyPressed(RemoteKey.POWER) })
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconKey(Icons.AutoMirrored.Filled.Input, stringResource(R.string.cd_source), { onKeyPressed(RemoteKey.SOURCE) })
                IconKey(Icons.AutoMirrored.Filled.VolumeOff, stringResource(R.string.cd_mute), { onKeyPressed(RemoteKey.MUTE) })
            }
        }

        // D-pad / number pad switch, like the Google TV remote's D-pad / touchpad toggle.
        SingleChoiceSegmentedButtonRow(modifier = Modifier.width(240.dp)) {
            SegmentedButton(
                selected = !showNumericPad,
                onClick = { showNumericPad = false },
                shape = SegmentedButtonDefaults.itemShape(0, 2),
                icon = { SegmentedButtonDefaults.Icon(!showNumericPad) { Icon(Icons.Default.Gamepad, null, Modifier.size(18.dp)) } },
                label = { Text(stringResource(R.string.dpad), maxLines = 1) },
            )
            SegmentedButton(
                selected = showNumericPad,
                onClick = { showNumericPad = true },
                shape = SegmentedButtonDefaults.itemShape(1, 2),
                icon = { SegmentedButtonDefaults.Icon(showNumericPad) { Icon(Icons.Default.Dialpad, null, Modifier.size(18.dp)) } },
                label = { Text(stringResource(R.string.num_pad), maxLines = 1) },
            )
        }

        // Fixed height so switching between the two pads doesn't move the keys below.
        Box(modifier = Modifier.fillMaxWidth().height(272.dp), contentAlignment = Alignment.Center) {
            if (showNumericPad) NumberPad(onKeyPressed) else DPad(onKey = onKeyPressed)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            IconKey(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), { onKeyPressed(RemoteKey.BACK) })
            IconKey(Icons.Default.Home, stringResource(R.string.cd_home), { onKeyPressed(RemoteKey.HOME) })
            IconKey(Icons.Default.Menu, stringResource(R.string.cd_menu), { onKeyPressed(RemoteKey.MENU) })
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            RockerKey(
                label = stringResource(R.string.vol),
                upIcon = Icons.Default.Add,
                downIcon = Icons.Default.Remove,
                upDescription = stringResource(R.string.cd_volume_up),
                downDescription = stringResource(R.string.cd_volume_down),
                onUp = { onKeyPressed(RemoteKey.VOLUME_UP) },
                onDown = { onKeyPressed(RemoteKey.VOLUME_DOWN) },
            )
            RockerKey(
                label = stringResource(R.string.ch),
                upIcon = Icons.Default.KeyboardArrowUp,
                downIcon = Icons.Default.KeyboardArrowDown,
                upDescription = stringResource(R.string.cd_channel_up),
                downDescription = stringResource(R.string.cd_channel_down),
                onUp = { onKeyPressed(RemoteKey.CHANNEL_UP) },
                onDown = { onKeyPressed(RemoteKey.CHANNEL_DOWN) },
            )
        }
    }
}

@Composable
private fun NumberPad(onKeyPressed: (RemoteKey) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        numberPad.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { label ->
                    val scheme = MaterialTheme.colorScheme
                    val special = label == "-" || label == "⌫"
                    RemoteButton(
                        onClick = { onKeyPressed(padKey(label)) },
                        modifier = Modifier.weight(1f).height(58.dp),
                        containerColor = if (special) scheme.secondaryContainer else scheme.surfaceContainerHighest,
                        contentColor = if (special) scheme.onSecondaryContainer else scheme.onSurface,
                        repeatOnHold = label == "⌫",
                    ) {
                        if (label == "⌫") {
                            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = stringResource(R.string.cd_backspace))
                        } else {
                            Text(label, style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }
        }
    }
}
