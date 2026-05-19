package com.anyremote.remote.ui.layouts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
fun TvLayout(onKeyPressed: (Int) -> Unit) {
    val haptic = rememberVibratingHapticFeedback()
    var showNumericPad by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Top Action Row (Power Red button, Source Center button, Numeric Pad toggle, Mute Right
        // button)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier =
                    Modifier.size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.5.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onKeyPressed(12) // TV Power code
                        },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.PowerSettingsNew, contentDescription = "Power", tint = CrimsonRed)
            }

            Box(
                modifier =
                    Modifier.size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.5.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onKeyPressed(11) // Source / Input code
                        },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Input,
                    contentDescription = "Source",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            Box(
                modifier =
                    Modifier.size(54.dp)
                        .clip(CircleShape)
                        .background(
                            if (showNumericPad) {
                                Color(0xFF00B0FF).copy(alpha = 0.1f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                        )
                        .border(
                            1.5.dp,
                            if (showNumericPad) {
                                Color(0xFF00B0FF)
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            },
                            CircleShape,
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showNumericPad = !showNumericPad
                        },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.num_pad),
                    color =
                        if (showNumericPad) Color(0xFF00B0FF)
                        else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
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
                            CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onKeyPressed(13) // Mute code
                        },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeMute,
                    contentDescription = "Mute",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        if (showNumericPad) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            ) {
                val numGrid =
                    listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("-", "0", "⌫"),
                    )

                numGrid.forEach { rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        rowKeys.forEach { key ->
                            val isSpecial = key == "-" || key == "⌫"
                            Box(
                                modifier =
                                    Modifier.weight(1f)
                                        .height(58.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSpecial) {
                                                MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.05f,
                                                )
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            },
                                        )
                                        .border(
                                            1.5.dp,
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                            RoundedCornerShape(14.dp),
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(
                                                HapticFeedbackType.LongPress)
                                            if (key == "⌫") {
                                                onKeyPressed(67) // Backspace keycode
                                            } else if (key == "-") {
                                                onKeyPressed(69) // Minus keycode
                                            } else {
                                                val digit = key.toInt()
                                                onKeyPressed(7 + digit)
                                            }
                                        },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = key,
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
            // Concentric Dpad
            Box(
                modifier =
                    Modifier.size(200.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.5.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawLine(
                        color = dividerColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.5f,
                    )
                    drawLine(
                        color = dividerColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.5f,
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // Central OK button
                    Box(
                        modifier =
                            Modifier.size(76.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                .border(
                                    1.5.dp,
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    CircleShape)
                                .align(Alignment.Center)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onKeyPressed(20) // OK command
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            stringResource(R.string.ok),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }

                    // Up Arrow
                    Box(
                        modifier =
                            Modifier.align(Alignment.TopCenter)
                                .padding(top = 12.dp)
                                .size(54.dp)
                                .clip(CircleShape)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onKeyPressed(16) // Up command
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "Up",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp),
                        )
                    }

                    // Down Arrow
                    Box(
                        modifier =
                            Modifier.align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                                .size(54.dp)
                                .clip(CircleShape)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onKeyPressed(17) // Down command
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Down",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp),
                        )
                    }

                    // Left Arrow
                    Box(
                        modifier =
                            Modifier.align(Alignment.CenterStart)
                                .padding(start = 12.dp)
                                .size(54.dp)
                                .clip(CircleShape)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onKeyPressed(18) // Left command
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Left",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp),
                        )
                    }

                    // Right Arrow
                    Box(
                        modifier =
                            Modifier.align(Alignment.CenterEnd)
                                .padding(end = 12.dp)
                                .size(54.dp)
                                .clip(CircleShape)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onKeyPressed(19) // Right command
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Right",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }

            // Utility Row (Back, Home, Menu)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Box(
                    modifier =
                        Modifier.size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                1.5.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onKeyPressed(4) // BACK code
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
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
                                CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onKeyPressed(3) // HOME code
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Home",
                        tint = MaterialTheme.colorScheme.onSurface)
                }

                Box(
                    modifier =
                        Modifier.size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                1.5.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onKeyPressed(82) // MENU code
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Rockers (Volume / Channel pills)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                Column(
                    modifier =
                        Modifier.weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                1.5.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                RoundedCornerShape(24.dp),
                            )
                            .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onKeyPressed(24) // Vol+ command
                        }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Volume Up",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    Text(
                        stringResource(R.string.vol),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onKeyPressed(25) // Vol- command
                        }) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Volume Down",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                }

                Column(
                    modifier =
                        Modifier.weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                1.5.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                RoundedCornerShape(24.dp),
                            )
                            .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onKeyPressed(26) // CH+ command
                        }) {
                            Icon(
                                Icons.Default.KeyboardArrowUp,
                                contentDescription = "Channel Up",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    Text(
                        stringResource(R.string.ch),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onKeyPressed(27) // CH- command
                        }) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Channel Down",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                }
            }
        }
    }
}
