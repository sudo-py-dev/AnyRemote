package com.anyremote.remote.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

@Composable
fun CameraLayout(onKeyPressed: (RemoteKey) -> Unit) {
    var shutterKey by rememberSaveable { mutableStateOf(RemoteKey.SHUTTER) }
    val delays =
        listOf(
            RemoteKey.SHUTTER to stringResource(R.string.camera_delay_instant),
            RemoteKey.SHUTTER_2S to stringResource(R.string.camera_delay_2s),
            RemoteKey.SHUTTER_5S to stringResource(R.string.camera_delay_5s),
            RemoteKey.SHUTTER_10S to stringResource(R.string.camera_delay_10s),
        )
    val scheme = MaterialTheme.colorScheme

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.camera_dslr_shutter),
            style = MaterialTheme.typography.titleMedium,
            color = scheme.onSurfaceVariant,
        )

        // Camera-app style shutter: tonal ring around a solid primary disc.
        RemoteButton(
            onClick = { onKeyPressed(shutterKey) },
            modifier = Modifier.size(176.dp),
            containerColor = scheme.primaryContainer,
            contentColor = scheme.onPrimary,
        ) {
            Box(
                modifier = Modifier.size(128.dp).background(scheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = stringResource(R.string.cd_shutter),
                    modifier = Modifier.size(44.dp),
                )
            }
        }

        RemoteSection(title = stringResource(R.string.delay)) {
            SegmentedSelector(
                options = delays,
                selected = shutterKey,
                onSelect = { shutterKey = it },
            )
        }
    }
}
