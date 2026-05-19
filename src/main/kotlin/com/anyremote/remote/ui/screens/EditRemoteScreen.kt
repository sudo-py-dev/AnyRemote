package com.anyremote.remote.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anyremote.remote.R
import com.anyremote.remote.data.RemoteDatabase.Brand
import com.anyremote.remote.data.RemoteDatabase.DeviceType
import com.anyremote.remote.service.FavoriteManager
import com.anyremote.remote.ui.components.*
import com.anyremote.remote.ui.helpers.*
import com.anyremote.remote.ui.layouts.*
import com.anyremote.remote.ui.theme.*
import com.anyremote.remote.ui.theme.ElectricBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRemoteScreen(
    device: DeviceType,
    brand: Brand,
    onDismiss: () -> Unit,
    onSave: (newName: String) -> Unit,
) {
    val context = LocalContext.current
    val defaultName = "${brand.nameEn} ${device.nameEn.substringBefore(" ")}"
    val currentCustomName =
        remember(device, brand) {
            FavoriteManager.getCustomName(context, device.id, brand.id, defaultName)
        }
    var nameInput by remember { mutableStateOf(currentCustomName) }

    BackHandler { onDismiss() }

    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF121212) else Color(0xFFF7F8FA)
    val cardColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val labelColor = if (isDark) Color(0xFF90A4AE) else Color(0xFF8E9AA7)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.edit_remote).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (nameInput.isNotBlank()) {
                                onSave(nameInput.trim())
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.save),
                                tint = ElectricBlue,
                            )
                        }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = bgColor,
                    ),
            )
        },
        containerColor = bgColor,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.device_name_section).uppercase(),
                    color = labelColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border =
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            singleLine = true,
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        )
                    }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.quick_rename_section).uppercase(),
                    color = labelColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 4.dp))

                val locations =
                    listOf(
                        stringResource(R.string.location_default),
                        stringResource(R.string.location_living_room),
                        stringResource(R.string.location_bedroom),
                        stringResource(R.string.location_study_room),
                        stringResource(R.string.location_dining_room),
                        stringResource(R.string.location_office))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (i in locations.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                for (j in 0..1) {
                                    val index = i + j
                                    if (index < locations.size) {
                                        val loc = locations[index]
                                        Card(
                                            modifier =
                                                Modifier.weight(1f).clickable { nameInput = loc },
                                            shape = RoundedCornerShape(14.dp),
                                            colors =
                                                CardDefaults.cardColors(containerColor = cardColor),
                                            elevation =
                                                CardDefaults.cardElevation(defaultElevation = 0.dp),
                                            border =
                                                BorderStroke(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline.copy(
                                                        alpha = 0.12f))) {
                                                Box(
                                                    modifier =
                                                        Modifier.fillMaxWidth()
                                                            .padding(
                                                                vertical = 16.dp,
                                                                horizontal = 12.dp),
                                                    contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = loc,
                                                            color =
                                                                MaterialTheme.colorScheme.onSurface,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 14.sp,
                                                            textAlign = TextAlign.Center)
                                                    }
                                            }
                                    }
                                }
                            }
                    }
                }
            }
        }
    }
}
