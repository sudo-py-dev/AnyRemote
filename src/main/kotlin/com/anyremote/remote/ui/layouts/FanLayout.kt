package com.anyremote.remote.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
fun FanLayout(onKeyPressed: (Int) -> Unit) {
    val haptic = rememberVibratingHapticFeedback()
    var fanPower by remember { mutableStateOf(false) }
    var fanSpeed by remember { mutableIntStateOf(1) }
    var fanSwing by remember { mutableStateOf(false) }
    var fanMode by remember { mutableStateOf("Normal") }

    val localizedMode =
        when (fanMode) {
            "Natural" -> stringResource(R.string.fan_mode_natural)
            "Sleep" -> stringResource(R.string.fan_mode_sleep)
            else -> stringResource(R.string.fan_mode_normal)
        }

    val swingStatus =
        if (fanSwing) stringResource(R.string.on_word) else stringResource(R.string.off_word)

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
                        if (fanPower) {
                            stringResource(R.string.fan_speed_banner, fanSpeed)
                        } else {
                            stringResource(R.string.fan_power_off_banner)
                        },
                    color =
                        if (fanPower) {
                            Color(0xFF00B0FF)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                if (fanPower) {
                    Text(
                        text =
                            stringResource(
                                R.string.fan_status_banner,
                                localizedMode.uppercase(),
                                swingStatus,
                            ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier =
                    Modifier.size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.5.dp,
                            if (fanPower) {
                                CrimsonRed
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            },
                            CircleShape,
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            fanPower = !fanPower
                            onKeyPressed(12)
                        },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Power",
                    tint = if (fanPower) CrimsonRed else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp),
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
                        .clickable(enabled = fanPower) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (fanSpeed > 1) fanSpeed--
                            onKeyPressed(25)
                        },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.speed_minus),
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
                        .clickable(enabled = fanPower) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (fanSpeed < 4) fanSpeed++
                            onKeyPressed(24)
                        },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.speed_plus),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
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
                        .background(
                            if (fanSwing && fanPower) {
                                Color(0xFF00B0FF).copy(alpha = 0.1f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                        )
                        .border(
                            1.5.dp,
                            if (fanSwing && fanPower) {
                                Color(0xFF00B0FF)
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            },
                            RoundedCornerShape(12.dp),
                        )
                        .clickable(enabled = fanPower) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            fanSwing = !fanSwing
                            onKeyPressed(20)
                        },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.oscillate),
                    color =
                        if (fanSwing && fanPower) {
                            Color(0xFF00B0FF)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
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
                        .clickable(enabled = fanPower) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            fanMode =
                                when (fanMode) {
                                    "Normal" -> "Natural"
                                    "Natural" -> "Sleep"
                                    else -> "Normal"
                                }
                            onKeyPressed(1)
                        },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    localizedMode.uppercase(),
                    color =
                        if (fanPower) Color(0xFF00B0FF) else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
