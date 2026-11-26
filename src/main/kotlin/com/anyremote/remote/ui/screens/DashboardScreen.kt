package com.anyremote.remote.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anyremote.remote.R
import com.anyremote.remote.data.RemoteDatabase
import com.anyremote.remote.data.RemoteDatabase.Brand
import com.anyremote.remote.data.RemoteDatabase.DeviceType
import com.anyremote.remote.service.FavoriteManager
import com.anyremote.remote.ui.components.*
import com.anyremote.remote.ui.helpers.*
import com.anyremote.remote.ui.layouts.*
import com.anyremote.remote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onDeviceSelected: (deviceId: Int, brandId: Int) -> Unit,
    onFavoriteSelected: (deviceId: Int, brandId: Int) -> Unit,
    onSettingsClick: () -> Unit,
) {
    var selectedDevice by remember { mutableStateOf<DeviceType?>(null) }
    var forceShowCategories by remember { mutableStateOf(false) }
    val context = LocalContext.current
    RemoteDatabase.init(context)

    var favoriteKeys by remember { mutableStateOf(emptySet<String>()) }
    LaunchedEffect(Unit) { favoriteKeys = FavoriteManager.getFavorites(context) }

    val hasIr = remember {
        try {
            val irManager =
                context.getSystemService(android.content.Context.CONSUMER_IR_SERVICE)
                    as? android.hardware.ConsumerIrManager
            irManager != null && irManager.hasIrEmitter()
        } catch (e: Exception) {
            false
        }
    }
    var showIrNotice by remember {
        mutableStateOf(!hasIr && !FavoriteManager.isIrNoticeDismissed(context))
    }

    var selectedItemForOptions by remember {
        mutableStateOf<Triple<DeviceType, Brand, String>?>(null)
    }
    var selectedItemForEditing by remember {
        mutableStateOf<Triple<DeviceType, Brand, String>?>(null)
    }
    var renameTrigger by remember { mutableStateOf(0) }

    if (selectedItemForEditing != null) {
        BackHandler { selectedItemForEditing = null }
    } else if (selectedItemForOptions != null) {
        BackHandler { selectedItemForOptions = null }
    } else if (selectedDevice != null) {
        BackHandler { selectedDevice = null }
    } else if (forceShowCategories) {
        BackHandler { forceShowCategories = false }
    }

    val editingItem = selectedItemForEditing
    if (editingItem != null) {
        val (device, brand, _) = editingItem
        EditRemoteScreen(
            device = device,
            brand = brand,
            onDismiss = { selectedItemForEditing = null },
            onSave = { newName ->
                FavoriteManager.setCustomName(context, device.id, brand.id, newName)
                favoriteKeys = FavoriteManager.getFavorites(context)
                renameTrigger++
                selectedItemForEditing = null
            })
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text =
                                selectedDevice?.localizedName()?.uppercase()
                                    ?: if (forceShowCategories) {
                                        stringResource(R.string.select_device_type)
                                    } else {
                                        stringResource(R.string.app_name).uppercase()
                                    },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp,
                        )
                    },
                    navigationIcon = {
                        if (selectedDevice != null) {
                            IconButton(onClick = { selectedDevice = null }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back),
                                    tint = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        } else if (forceShowCategories) {
                            IconButton(onClick = { forceShowCategories = false }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back),
                                    tint = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            ) {
                if (!forceShowCategories) {

                    MainDashboardPage(
                        favoriteKeys = favoriteKeys,
                        renameTrigger = renameTrigger,
                        showIrNotice = showIrNotice,
                        onDismissIrNotice = {
                            FavoriteManager.setIrNoticeDismissed(context, true)
                            showIrNotice = false
                        },
                        onRemoteClick = onFavoriteSelected,
                        onFavoritesChanged = {
                            favoriteKeys = FavoriteManager.getFavorites(context)
                        },
                        onAddNewClick = { forceShowCategories = true },
                        onSettingsClick = onSettingsClick,
                        onLongClick = { selectedItemForOptions = it })
                } else if (selectedDevice == null) {

                    CategorySelectionGrid(
                        onCategoryClick = { selectedDevice = it },
                    )
                } else {

                    val dev = selectedDevice
                    if (dev != null) {
                        BrandSelectionPanel(
                            device = dev,
                            onBrandClick = { brand -> onDeviceSelected(dev.id, brand.id) },
                        )
                    }
                }
            }
        }

        val optionsItem = selectedItemForOptions
        if (optionsItem != null) {
            val (device, brand, key) = optionsItem
            val defaultName =
                "${brand.nameEn} ${device.getLocalizedName(context).substringBefore(" ")}"
            val currentCustomName =
                FavoriteManager.getCustomName(context, device.id, brand.id, defaultName)

            AlertDialog(
                onDismissRequest = { selectedItemForOptions = null },
                title = {
                    Text(
                        text = currentCustomName.uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .clickable {
                                        selectedItemForEditing = selectedItemForOptions
                                        selectedItemForOptions = null
                                    }
                                    .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_remote),
                                tint = ElectricBlue,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = stringResource(R.string.edit_remote),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .clickable {
                                        selectedItemForOptions = null
                                        com.anyremote.remote.service.ShortcutHelper
                                            .pinRemoteShortcut(
                                                context = context,
                                                deviceId = device.id,
                                                brandId = brand.id,
                                                deviceName = device.getLocalizedName(context),
                                                brandName = brand.nameEn,
                                            )
                                    }
                                    .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = stringResource(R.string.pin_to_home),
                                tint = ElectricBlue,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = stringResource(R.string.pin_to_home),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .clickable {
                                        selectedItemForOptions = null
                                        FavoriteManager.toggleFavorite(context, device.id, brand.id)
                                        favoriteKeys = FavoriteManager.getFavorites(context)
                                    }
                                    .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.remove_remote),
                                tint = CrimsonRed,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = stringResource(R.string.remove_remote),
                                color = CrimsonRed,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedItemForOptions = null }) {
                        Text(
                            text = stringResource(R.string.cancel),
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
            )
        }
    }
}

@Composable
fun MainDashboardPage(
    favoriteKeys: Set<String>,
    renameTrigger: Int,
    showIrNotice: Boolean,
    onDismissIrNotice: () -> Unit,
    onRemoteClick: (deviceId: Int, brandId: Int) -> Unit,
    onFavoritesChanged: () -> Unit,
    onAddNewClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLongClick: (Triple<DeviceType, Brand, String>) -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (RemoteDatabase.loadFailed) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CrimsonRed.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonRed)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.db_load_error),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                    )
                }
            }
        }
        if (showIrNotice) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CrimsonRed.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier =
                                    Modifier.size(36.dp)
                                        .background(CrimsonRed.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = CrimsonRed,
                                        modifier = Modifier.size(20.dp))
                                }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text =
                                        stringResource(R.string.ir_sensor_missing_title)
                                            .uppercase(),
                                    color = CrimsonRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.ir_sensor_missing_message),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 15.sp)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = onDismissIrNotice, modifier = Modifier.size(28.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.cancel),
                                        tint =
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.7f),
                                        modifier = Modifier.size(16.dp))
                                }
                        }
                }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (favoriteKeys.isEmpty()) {

            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp),
                ) {
                    Box(
                        modifier =
                            Modifier.size(108.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsRemote,
                            contentDescription = null,
                            tint = ElectricBlue.copy(alpha = 0.6f),
                            modifier = Modifier.size(50.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = stringResource(R.string.main_page_empty),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.main_page_empty_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        } else {

            Text(
                text = stringResource(R.string.paired_remotes_title),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp, top = 8.dp),
            )

            val favoritesList =
                remember(favoriteKeys) {
                    favoriteKeys.mapNotNull { key ->
                        val parts = key.split("_")
                        if (parts.size == 2) {
                            val deviceId = parts[0].toIntOrNull() ?: return@mapNotNull null
                            val brandId = parts[1].toIntOrNull() ?: return@mapNotNull null
                            val device =
                                RemoteDatabase.deviceTypes.firstOrNull { it.id == deviceId }
                                    ?: return@mapNotNull null
                            val brand =
                                device.brands.firstOrNull { it.id == brandId }
                                    ?: return@mapNotNull null
                            Triple(device, brand, key)
                        } else {
                            null
                        }
                    }
                }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(favoritesList) { (device, brand, key) ->
                    val defaultName =
                        "${brand.nameEn} ${device.getLocalizedName(context).substringBefore(" ")}"
                    val displayName =
                        remember(device, brand, renameTrigger) {
                            FavoriteManager.getCustomName(context, device.id, brand.id, defaultName)
                        }
                    FavoriteRemoteListItem(
                        device = device,
                        brand = brand,
                        displayName = displayName,
                        onClick = { onRemoteClick(device.id, brand.id) },
                        onLongClick = { onLongClick(Triple(device, brand, key)) },
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedButton(
                onClick = onSettingsClick,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.weight(1f).height(48.dp),
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.settings),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp)
            }

            Button(
                onClick = onAddNewClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                modifier = Modifier.weight(1.2f).height(48.dp),
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_new),
                    tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.add_new),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteRemoteListItem(
    device: DeviceType,
    brand: Brand,
    displayName: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current

    Box(
        modifier =
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .padding(14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier.size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = getDeviceIcon(device.id),
                    contentDescription = device.localizedName(),
                    tint = ElectricBlue,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = displayName.uppercase(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                )
                Text(
                    text = device.localizedName().uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }
}
