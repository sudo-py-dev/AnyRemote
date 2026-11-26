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
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            PowerKey(onClick = { onKeyPressed(RemoteKey.POWER) })
        }

        DPad(onKey = onKeyPressed)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            IconKey(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), { onKeyPressed(RemoteKey.BACK) })
            IconKey(Icons.Default.Home, stringResource(R.string.cd_home), { onKeyPressed(RemoteKey.HOME) })
            IconKey(Icons.Default.Menu, stringResource(R.string.cd_menu), { onKeyPressed(RemoteKey.MENU) })
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextKey(
                stringResource(R.string.focus_minus),
                onClick = { onKeyPressed(RemoteKey.FOCUS_DOWN) },
                modifier = Modifier.weight(1f),
                repeatOnHold = true,
            )
            TextKey(
                stringResource(R.string.focus_plus),
                onClick = { onKeyPressed(RemoteKey.FOCUS_UP) },
                modifier = Modifier.weight(1f),
                repeatOnHold = true,
            )
        }
    }
}
