package com.anyremote.remote.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anyremote.remote.data.RemoteDatabase
import com.anyremote.remote.data.RemoteDatabase.DeviceType
import com.anyremote.remote.data.RemoteDatabase.Brand
import com.anyremote.remote.service.FavoriteManager
import androidx.compose.ui.res.stringResource
import com.anyremote.remote.R
import com.anyremote.remote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onDeviceSelected: (deviceId: Int, brandId: Int) -> Unit,
    onFavoriteSelected: (deviceId: Int, brandId: Int) -> Unit,
    onSettingsClick: () -> Unit
) {
    var selectedDevice by remember { mutableStateOf<DeviceType?>(null) }
    var forceShowCategories by remember { mutableStateOf(false) }
    val context = LocalContext.current
    RemoteDatabase.init(context)
    
    // Dynamically retrieve favorite remotes on launch/resume
    var favoriteKeys by remember { mutableStateOf(emptySet<String>()) }
    LaunchedEffect(Unit) {
        favoriteKeys = FavoriteManager.getFavorites(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (selectedDevice != null) {
                            selectedDevice!!.nameEn.uppercase()
                        } else if (forceShowCategories) {
                            stringResource(R.string.select_device_type)
                        } else {
                            stringResource(R.string.app_name).uppercase()
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    if (selectedDevice != null) {
                        IconButton(onClick = { selectedDevice = null }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    } else if (forceShowCategories) {
                         IconButton(onClick = { forceShowCategories = false }) {
                             Icon(
                                 imageVector = Icons.Default.ArrowBack,
                                 contentDescription = stringResource(R.string.back),
                                 tint = MaterialTheme.colorScheme.onBackground
                             )
                         }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!forceShowCategories) {
                // First Page/Main Page: Lists saved devices or remains beautifully blank with buttons
                MainDashboardPage(
                    favoriteKeys = favoriteKeys,
                    onRemoteClick = onFavoriteSelected,
                    onFavoritesChanged = {
                        favoriteKeys = FavoriteManager.getFavorites(context)
                    },
                    onAddNewClick = { forceShowCategories = true },
                    onSettingsClick = onSettingsClick
                )
            } else if (selectedDevice == null) {
                // Stage 1 of adding device: Device Category Grid
                CategorySelectionGrid(
                    onCategoryClick = { selectedDevice = it }
                )
            } else {
                // Stage 2 of adding device: Brand Selection Panel with Letter filter & search
                BrandSelectionPanel(
                    device = selectedDevice!!,
                    onBrandClick = { brand ->
                        onDeviceSelected(selectedDevice!!.id, brand.id)
                    }
                )
            }
        }
    }
}

@Composable
fun MainDashboardPage(
    favoriteKeys: Set<String>,
    onRemoteClick: (deviceId: Int, brandId: Int) -> Unit,
    onFavoritesChanged: () -> Unit,
    onAddNewClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedItemForOptions by remember { mutableStateOf<Triple<DeviceType, Brand, String>?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (favoriteKeys.isEmpty()) {
            // Elegant Blank Page Layout
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(108.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsRemote,
                            contentDescription = null,
                            tint = ElectricBlue.copy(alpha = 0.6f),
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = stringResource(R.string.main_page_empty),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.main_page_empty_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            // Elegant Main Screen Added Remotes List
            Text(
                text = stringResource(R.string.paired_remotes_title),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, top = 8.dp)
            )

            val favoritesList = remember(favoriteKeys) {
                favoriteKeys.mapNotNull { key ->
                    val parts = key.split("_")
                    if (parts.size == 2) {
                        val deviceId = parts[0].toIntOrNull() ?: return@mapNotNull null
                        val brandId = parts[1].toIntOrNull() ?: return@mapNotNull null
                        val device = RemoteDatabase.deviceTypes.firstOrNull { it.id == deviceId } ?: return@mapNotNull null
                        val brand = device.brands.firstOrNull { it.id == brandId } ?: return@mapNotNull null
                        Triple(device, brand, key)
                    } else null
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(favoritesList) { (device, brand, key) ->
                    FavoriteRemoteListItem(
                        device = device,
                        brand = brand,
                        onClick = { onRemoteClick(device.id, brand.id) },
                        onLongClick = {
                            selectedItemForOptions = Triple(device, brand, key)
                        }
                    )
                }
            }
        }

        // Bottom action buttons container visible at all times
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Settings Button
            OutlinedButton(
                onClick = onSettingsClick,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings), tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            // Add New Device Button
            Button(
                onClick = onAddNewClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_new), tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_new), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
            }
        }
    }

    // 1. Long-press options dialog
    if (selectedItemForOptions != null) {
        val (device, brand, _) = selectedItemForOptions!!
        val defaultName = "${brand.nameEn} ${device.nameEn.substringBefore(" ")}"
        val currentCustomName = FavoriteManager.getCustomName(context, device.id, brand.id, defaultName)

        AlertDialog(
            onDismissRequest = { selectedItemForOptions = null },
            title = {
                Text(
                    text = currentCustomName.uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Option 1: Rename Remote
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showRenameDialog = true
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.rename_remote), tint = ElectricBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(R.string.rename_remote), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    // Option 2: Pin to Home Screen
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedItemForOptions = null
                                com.anyremote.remote.service.ShortcutHelper.pinRemoteShortcut(
                                    context = context,
                                    deviceId = device.id,
                                    brandId = brand.id,
                                    deviceName = device.nameEn,
                                    brandName = brand.nameEn
                                )
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PushPin, contentDescription = stringResource(R.string.pin_to_home), tint = ElectricBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(R.string.pin_to_home), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    // Option 3: Unpin from Favorites
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedItemForOptions = null
                                FavoriteManager.toggleFavorite(context, device.id, brand.id)
                                onFavoritesChanged()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove_remote), tint = CrimsonRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(R.string.remove_remote), color = CrimsonRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedItemForOptions = null }) {
                    Text(stringResource(R.string.cancel), color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 2. Rename TextInput Dialog
    if (showRenameDialog && selectedItemForOptions != null) {
        val (device, brand, _) = selectedItemForOptions!!
        val defaultName = "${brand.nameEn} ${device.nameEn.substringBefore(" ")}"
        val currentCustomName = FavoriteManager.getCustomName(context, device.id, brand.id, defaultName)
        var nameInput by remember { mutableStateOf(currentCustomName) }

        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.rename_remote).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
            },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(stringResource(R.string.remote_name_label), color = ElectricBlue) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            FavoriteManager.setCustomName(context, device.id, brand.id, nameInput.trim())
                            onFavoritesChanged()
                        }
                        showRenameDialog = false
                        selectedItemForOptions = null
                    }
                ) {
                    Text(stringResource(R.string.save), color = ElectricBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteRemoteListItem(
    device: DeviceType,
    brand: Brand,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    val displayName = remember(context, device, brand) {
        FavoriteManager.getCustomName(context, device.id, brand.id, "${brand.nameEn} ${device.nameEn.substringBefore(" ")}")
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = brand.nameEn.take(1).uppercase(),
                    color = ElectricBlue,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column {
                Text(
                    text = displayName.uppercase(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Text(
                    text = device.nameEn.uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun CategorySelectionGrid(
    onCategoryClick: (DeviceType) -> Unit
) {
    val chunkedCategories = remember(RemoteDatabase.deviceTypes) { RemoteDatabase.deviceTypes.chunked(2) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(chunkedCategories) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CategoryCard(
                    device = pair[0],
                    onClick = { onCategoryClick(pair[0]) },
                    modifier = Modifier.weight(1f)
                )
                if (pair.size > 1) {
                    CategoryCard(
                        device = pair[1],
                        onClick = { onCategoryClick(pair[1]) },
                        modifier = Modifier.weight(1f)
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
    modifier: Modifier = Modifier
) {
    val icon = when (device.id) {
        1 -> Icons.Default.Tv
        2 -> Icons.Default.Router
        3 -> Icons.Default.AcUnit
        4 -> Icons.Default.Dvr
        6 -> Icons.Default.WindPower
        8 -> Icons.Default.Speaker
        10 -> Icons.Default.SettingsInputHdmi
        11 -> Icons.Default.SettingsInputAntenna
        12 -> Icons.Default.Cast
        13 -> Icons.Default.PhotoCamera
        else -> Icons.Default.Power
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = device.nameEn,
                tint = ElectricBlue,
                modifier = Modifier
                    .size(32.dp)
                    .padding(bottom = 8.dp)
            )
            
            Text(
                text = device.nameEn,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BrandSelectionPanel(
    device: DeviceType,
    onBrandClick: (Brand) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }

    val filteredBrands = remember(searchQuery, selectedLetter, device.brands) {
        device.brands.filter { brand ->
            val matchesSearch = if (searchQuery.isEmpty()) {
                true
            } else {
                brand.nameEn.contains(searchQuery, ignoreCase = true)
            }
            
            val matchesLetter = if (selectedLetter == null) {
                true
            } else {
                brand.nameEn.startsWith(selectedLetter!!, ignoreCase = true)
            }
            
            matchesSearch && matchesLetter
        }.sortedBy { it.nameEn }
    }

    val alphabet = remember(device.brands) {
        ('A'..'Z').filter { ch ->
            device.brands.any { it.nameEn.startsWith(ch, ignoreCase = true) }
        }
    }

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Sticky Search Field
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(stringResource(R.string.search_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = ElectricBlue) }
        )

        // List Header
        Text(
            text = "BRANDS",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Group brands by first letter
            val groupedBrands = remember(filteredBrands) {
                filteredBrands.groupBy { it.nameEn.take(1).uppercase() }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredBrands.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No brands found",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    groupedBrands.forEach { (initial, brandsInGroup) ->
                        // Styled Letter Section Header like in a Premium Contacts app
                        item(key = "header_$initial") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, bottom = 4.dp)
                            ) {
                                Text(
                                    text = initial,
                                    color = ElectricBlue,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        items(brandsInGroup) { brand ->
                            BrandListItem(
                                brand = brand,
                                onClick = { onBrandClick(brand) }
                            )
                        }
                    }
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Sleek Contacts-style Vertical Alphabetical Fast Index Sidebar
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(30.dp)
                    .background(getAppButtonBackground(), shape = RoundedCornerShape(15.dp))
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // "ALL" button at the top of the sidebar
                val isAllSelected = selectedLetter == null
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isAllSelected) ElectricBlue else Color.Transparent)
                        .clickable {
                            if (FavoriteManager.isVibrationEnabled(context)) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            selectedLetter = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "•",
                        color = if (isAllSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Scrollable letter options inside the sidebar
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(alphabet) { ch ->
                        val isSelected = selectedLetter == ch
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) ElectricBlue else Color.Transparent)
                                .clickable {
                                    if (FavoriteManager.isVibrationEnabled(context)) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    selectedLetter = ch
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ch.toString(),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BrandListItem(
    brand: Brand,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = brand.nameEn.take(1).uppercase(),
                    color = ElectricBlue,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = brand.nameEn,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
