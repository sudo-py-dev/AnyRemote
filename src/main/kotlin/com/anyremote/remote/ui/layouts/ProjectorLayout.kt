package com.anyremote.remote.ui.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anyremote.remote.R
import com.anyremote.remote.ir.RemoteKey
import com.anyremote.remote.ui.components.*

@Composable
fun ProjectorLayout(onKeyPressed: (RemoteKey) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        PowerKey(onClick = { onKeyPressed(RemoteKey.POWER) })

        DPad(onKey = onKeyPressed, size = 180.dp, centerSize = 64.dp, showDividers = false)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            RoundIconKey(Icons.AutoMirrored.Filled.ArrowBack, "Back", { onKeyPressed(RemoteKey.BACK) })
            RoundIconKey(Icons.Default.Home, "Home", { onKeyPressed(RemoteKey.HOME) })
            RoundIconKey(Icons.Default.Menu, "Menu", { onKeyPressed(RemoteKey.MENU) })
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TextKey(
                stringResource(R.string.focus_minus),
                onClick = { onKeyPressed(RemoteKey.FOCUS_DOWN) },
                modifier = Modifier.weight(1f),
            )
            TextKey(
                stringResource(R.string.focus_plus),
                onClick = { onKeyPressed(RemoteKey.FOCUS_UP) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
