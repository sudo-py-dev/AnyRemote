package com.anyremote.remote.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anyremote.remote.ui.helpers.*
import com.anyremote.remote.ui.theme.*
import com.anyremote.remote.ui.theme.CrimsonRed

@Composable
fun CameraLayout(onTrigger: (Int) -> Unit) {
    val haptic = rememberVibratingHapticFeedback()
    var delaySecs by remember { mutableIntStateOf(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "DSLR SHUTTER RELEASE",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
        )

        Box(
            modifier =
                Modifier.size(160.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        4.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTrigger(delaySecs)
                    },
            contentAlignment = Alignment.Center,
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
                    contentDescription = "Shutter",
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(36.dp),
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
                        RoundedCornerShape(12.dp))
                    .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val delays = listOf(0 to "Instant", 2 to "2s Delay", 5 to "5s Delay", 10 to "10s Delay")
            delays.forEach { (sec, label) ->
                val isActive = delaySecs == sec
                Box(
                    modifier =
                        Modifier.weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isActive) Color(0xFF00B0FF).copy(alpha = 0.1f)
                                else Color.Transparent)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                delaySecs = sec
                            }
                            .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        color =
                            if (isActive) Color(0xFF00B0FF)
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}
