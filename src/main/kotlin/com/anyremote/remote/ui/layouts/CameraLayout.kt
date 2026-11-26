package com.anyremote.remote.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anyremote.remote.R
import com.anyremote.remote.ir.RemoteKey
import com.anyremote.remote.ui.components.*
import com.anyremote.remote.ui.theme.CrimsonRed

@Composable
fun CameraLayout(onKeyPressed: (RemoteKey) -> Unit) {
    var shutterKey by remember { mutableStateOf(RemoteKey.SHUTTER) }
    val delays =
        listOf(
            RemoteKey.SHUTTER to stringResource(R.string.camera_delay_instant),
            RemoteKey.SHUTTER_2S to stringResource(R.string.camera_delay_2s),
            RemoteKey.SHUTTER_5S to stringResource(R.string.camera_delay_5s),
            RemoteKey.SHUTTER_10S to stringResource(R.string.camera_delay_10s),
        )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.camera_dslr_shutter),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
        )

        RemoteButton(
            onClick = { onKeyPressed(shutterKey) },
            modifier = Modifier.size(160.dp).border(4.dp, keyOutline(), CircleShape),
        ) {
            Box(
                modifier =
                    Modifier.size(110.dp)
                        .clip(CircleShape)
                        .background(CrimsonRed)
                        .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = stringResource(R.string.cd_shutter),
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(36.dp),
                )
            }
        }

        SegmentedSelector(options = delays, selected = shutterKey, onSelect = { shutterKey = it })
    }
}
