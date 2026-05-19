package com.anyremote.remote.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anyremote.remote.R
import com.anyremote.remote.ui.helpers.*
import com.anyremote.remote.ui.theme.*
import com.anyremote.remote.ui.theme.CrimsonRed

@Composable
fun AudioLayout(onKeyPressed: (Int) -> Unit) {
    val haptic = rememberVibratingHapticFeedback()
    var audioPower by remember { mutableStateOf(false) }
    var audioSource by remember { mutableStateOf("Bluetooth") }
    var audioVol by remember { mutableIntStateOf(15) }

    val localizedSource =
        when (audioSource) {
            "Bluetooth" -> stringResource(R.string.audio_source_bluetooth)
            "HDMI" -> stringResource(R.string.audio_source_hdmi)
            "Optical" -> stringResource(R.string.audio_source_optical)
            else -> stringResource(R.string.audio_source_aux)
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        1.5.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        RoundedCornerShape(16.dp),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text =
                        if (audioPower) {
                            stringResource(R.string.audio_volume_banner, audioVol)
                        } else {
                            stringResource(R.string.audio_standby_banner)
                        },
                    color =
                        if (audioPower) {
                            Color(0xFF00B0FF)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                if (audioPower) {
                    Text(
                        text =
                            stringResource(
                                R.string.audio_source_banner, localizedSource.uppercase()),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier.size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.5.dp,
                            if (audioPower) {
                                CrimsonRed
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            },
                            CircleShape,
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            audioPower = !audioPower
                            onKeyPressed(12) // Power code
                        },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Power",
                    tint = if (audioPower) CrimsonRed else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp),
                )
            }

            Box(
                modifier =
                    Modifier.size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.5.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            CircleShape,
                        )
                        .clickable(enabled = audioPower) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onKeyPressed(13) // Mute code
                        },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeMute,
                    contentDescription = "Mute",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier =
                    Modifier.weight(1f)
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.5.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            RoundedCornerShape(12.dp),
                        )
                        .clickable(enabled = audioPower) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (audioVol > 0) audioVol--
                            onKeyPressed(25) // Vol-
                        },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.vol_minus),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }

            Box(
                modifier =
                    Modifier.weight(1f)
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.5.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            RoundedCornerShape(12.dp),
                        )
                        .clickable(enabled = audioPower) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (audioVol < 30) audioVol++
                            onKeyPressed(24) // Vol+
                        },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.vol_plus),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        1.5.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        RoundedCornerShape(12.dp),
                    )
                    .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val sources =
                listOf("BT" to "Bluetooth", "HDMI" to "HDMI", "OPT" to "Optical", "AUX" to "AUX")
            sources.forEach { (sKey, sLabel) ->
                val isActive = audioSource == sLabel && audioPower
                Box(
                    modifier =
                        Modifier.weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isActive) {
                                    Color(0xFF00B0FF).copy(alpha = 0.1f)
                                } else {
                                    Color.Transparent
                                },
                            )
                            .clickable(enabled = audioPower) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                audioSource = sLabel
                                onKeyPressed(20) // Select source
                            }
                            .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = sKey,
                        color =
                            if (isActive) {
                                Color(0xFF00B0FF)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}
