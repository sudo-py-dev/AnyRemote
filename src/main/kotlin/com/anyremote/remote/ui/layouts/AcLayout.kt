package com.anyremote.remote.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anyremote.remote.R
import com.anyremote.remote.ui.helpers.*
import com.anyremote.remote.ui.theme.*
import com.anyremote.remote.ui.theme.CrimsonRed
import com.anyremote.remote.ui.theme.ElectricBlue

@Composable
fun AcLayout(
    power: Boolean,
    temp: Int,
    mode: String,
    speed: String,
    swing: Boolean,
    activeGlow: Color,
    onPowerToggle: () -> Unit,
    onTempChange: (Int) -> Unit,
    onModeChange: (String) -> Unit,
    onSpeedChange: (String) -> Unit,
    onSwingToggle: () -> Unit,
) {
    val haptic = rememberVibratingHapticFeedback()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "$temp",
                    color =
                        if (power) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                )
                Text(
                    text = "°C",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 18.dp, start = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier =
                    Modifier.clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val (modeIcon, modeLabel) =
                    when (mode) {
                        "MC" -> Icons.Default.AcUnit to stringResource(R.string.ac_mode_cool)
                        "MH" -> Icons.Default.WbSunny to stringResource(R.string.ac_mode_heat)
                        "MD" -> Icons.Default.Opacity to stringResource(R.string.ac_mode_dry)
                        "MV" -> Icons.Default.Air to stringResource(R.string.ac_mode_fan)
                        else -> Icons.Default.Autorenew to stringResource(R.string.ac_mode_auto)
                    }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        modeIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        modeLabel,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                val speedLabel =
                    when (speed) {
                        "SA" -> stringResource(R.string.ac_wind_auto)
                        "SL" -> stringResource(R.string.ac_wind_low)
                        "SM" -> stringResource(R.string.ac_wind_medium)
                        else -> stringResource(R.string.ac_wind_high)
                    }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Air,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        speedLabel,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (swing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.swing),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier.weight(1f).fillMaxHeight().clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPowerToggle()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Power",
                                tint =
                                    if (power) CrimsonRed else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.ac_label_power),
                                color =
                                    if (power) CrimsonRed else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Box(
                        modifier =
                            Modifier.width(1.5.dp)
                                .fillMaxHeight()
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                ),
                    )

                    Box(
                        modifier =
                            Modifier.weight(1f).fillMaxHeight().clickable(enabled = power) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val nextMode =
                                    when (mode) {
                                        "MC" -> "MH"
                                        "MH" -> "MD"
                                        "MD" -> "MV"
                                        "MV" -> "MA"
                                        else -> "MC"
                                    }
                                onModeChange(nextMode)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val modeIcon =
                                when (mode) {
                                    "MC" -> Icons.Default.AcUnit
                                    "MH" -> Icons.Default.WbSunny
                                    "MD" -> Icons.Default.Opacity
                                    "MV" -> Icons.Default.Air
                                    else -> Icons.Default.Autorenew
                                }
                            val modeLabel =
                                when (mode) {
                                    "MC" -> stringResource(R.string.ac_mode_cool)
                                    "MH" -> stringResource(R.string.ac_mode_heat)
                                    "MD" -> stringResource(R.string.ac_mode_dry)
                                    "MV" -> stringResource(R.string.ac_mode_fan)
                                    else -> stringResource(R.string.ac_mode_auto)
                                }
                            Icon(
                                imageVector = modeIcon,
                                contentDescription = "Mode",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${stringResource(R.string.ac_label_mode)} ($modeLabel)",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(1.5.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                )

                Row(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier.weight(1f).fillMaxHeight().clickable(enabled = power) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val newTemp = (temp - 1).coerceIn(16, 30)
                                if (newTemp != temp) onTempChange(newTemp)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Temp Down",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    Box(
                        modifier =
                            Modifier.width(1.5.dp)
                                .fillMaxHeight()
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                ),
                    )

                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.ac_label_temperature),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Box(
                        modifier =
                            Modifier.width(1.5.dp)
                                .fillMaxHeight()
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                ),
                    )

                    Box(
                        modifier =
                            Modifier.weight(1f).fillMaxHeight().clickable(enabled = power) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val newTemp = (temp + 1).coerceIn(16, 30)
                                if (newTemp != temp) onTempChange(newTemp)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Temp Up",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }

                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(1.5.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                )

                Row(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier.weight(1f).fillMaxHeight().clickable(enabled = power) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val nextSpeed =
                                    when (speed) {
                                        "SA" -> "SL"
                                        "SL" -> "SM"
                                        "SM" -> "SH"
                                        else -> "SA"
                                    }
                                onSpeedChange(nextSpeed)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val speedLabel =
                                when (speed) {
                                    "SA" -> stringResource(R.string.ac_wind_auto)
                                    "SL" -> stringResource(R.string.ac_wind_low)
                                    "SM" -> stringResource(R.string.ac_wind_medium)
                                    else -> stringResource(R.string.ac_wind_high)
                                }
                            Icon(
                                imageVector = Icons.Default.Air,
                                contentDescription = "Wind Speed",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${stringResource(R.string.ac_label_speed)} ($speedLabel)",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Box(
                        modifier =
                            Modifier.width(1.5.dp)
                                .fillMaxHeight()
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                ),
                    )

                    Box(
                        modifier =
                            Modifier.weight(1f).fillMaxHeight().clickable(enabled = power) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSwingToggle()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SyncAlt,
                                contentDescription = "Wind Direct",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.ac_label_direction),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Box(
                        modifier =
                            Modifier.width(1.5.dp)
                                .fillMaxHeight()
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                ),
                    )

                    Box(
                        modifier =
                            Modifier.weight(1f).fillMaxHeight().clickable(enabled = power) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSwingToggle()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                contentDescription = "Wind Sweep",
                                tint =
                                    if (swing && power) {
                                        ElectricBlue
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text =
                                    if (swing && power) {
                                        stringResource(R.string.ac_label_sweep_on)
                                    } else {
                                        stringResource(R.string.ac_label_sweep)
                                    },
                                color =
                                    if (swing && power) {
                                        ElectricBlue
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}
