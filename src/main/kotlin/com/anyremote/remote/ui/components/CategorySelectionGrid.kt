package com.anyremote.remote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Dvr
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.SettingsInputHdmi
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WindPower
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anyremote.remote.data.RemoteDatabase
import com.anyremote.remote.data.RemoteDatabase.DeviceType
import com.anyremote.remote.ui.helpers.*
import com.anyremote.remote.ui.theme.*
import com.anyremote.remote.ui.theme.ElectricBlue

@Composable
fun CategorySelectionGrid(onCategoryClick: (DeviceType) -> Unit) {
    val chunkedCategories =
        remember(RemoteDatabase.deviceTypes) { RemoteDatabase.deviceTypes.chunked(2) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(chunkedCategories) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CategoryCard(
                    device = pair[0],
                    onClick = { onCategoryClick(pair[0]) },
                    modifier = Modifier.weight(1f),
                )
                if (pair.size > 1) {
                    CategoryCard(
                        device = pair[1],
                        onClick = { onCategoryClick(pair[1]) },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    device: DeviceType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon =
        when (device.id) {
            1 -> Icons.Default.Tv
            2 -> Icons.Default.Router
            3 -> Icons.Default.AcUnit
            4 -> Icons.AutoMirrored.Filled.Dvr
            6 -> Icons.Default.WindPower
            8 -> Icons.Default.Speaker
            10 -> Icons.Default.SettingsInputHdmi
            11 -> Icons.Default.SettingsInputAntenna
            12 -> Icons.Default.Cast
            13 -> Icons.Default.PhotoCamera
            else -> Icons.Default.Power
        }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = device.nameEn,
                tint = ElectricBlue,
                modifier = Modifier.size(32.dp).padding(bottom = 8.dp),
            )

            Text(
                text = device.nameEn,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
