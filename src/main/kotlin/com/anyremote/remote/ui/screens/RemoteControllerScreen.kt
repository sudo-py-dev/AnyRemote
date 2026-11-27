package com.anyremote.remote.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anyremote.remote.R
import com.anyremote.remote.data.RemoteDatabase
import com.anyremote.remote.data.RemoteDatabase.RemoteProfile
import com.anyremote.remote.ir.AcProtocol
import com.anyremote.remote.ir.AcState
import com.anyremote.remote.ir.IRTransmitter
import com.anyremote.remote.ir.IrCodebook
import com.anyremote.remote.ir.RemoteKey
import com.anyremote.remote.service.FavoriteManager
import com.anyremote.remote.service.ShortcutHelper
import com.anyremote.remote.ui.components.RemoteDefaults
import com.anyremote.remote.ui.helpers.*
import com.anyremote.remote.ui.layouts.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteControllerScreen(
    deviceId: Int,
    brandId: Int,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current

    val device = remember(deviceId) { RemoteDatabase.deviceTypes.firstOrNull { it.id == deviceId } }
    val brand = remember(brandId, device) { device?.brands?.firstOrNull { it.id == brandId } }

    var isFavorite by
        remember(deviceId, brandId) { mutableStateOf(FavoriteManager.isFavorite(context, deviceId, brandId)) }

    // Previews (screenshot tests) have no launcher to register shortcuts with.
    val inPreview = LocalInspectionMode.current
    LaunchedEffect(deviceId, brandId, device, brand) {
        if (device != null && brand != null && !inPreview) {
            ShortcutHelper.registerDynamicShortcuts(
                context = context,
                deviceId = deviceId,
                brandId = brandId,
                deviceName = device.getLocalizedName(context),
                brandName = brand.nameEn,
            )
        }
    }

    val scope = rememberCoroutineScope()
    val codeset = remember(deviceId, brandId) { FavoriteManager.getCodeset(context, deviceId, brandId) }
    fun send(pulses: IntArray) {
        scope.launch { IRTransmitter.send(context, pulses) }
    }
    val profile = brand?.profile
    val commands = (profile as? RemoteProfile.Nec)?.commands
    val onKey: (RemoteKey) -> Unit = { key -> send(commands?.pulses(key) ?: IrCodebook.pulses(deviceId, codeset, key)) }

    var ac by rememberSaveable(stateSaver = AcState.Saver) { mutableStateOf(AcState()) }

    val deviceName = device?.localizedName() ?: stringResource(R.string.controller_fallback_subtitle)
    val fallbackTitle = stringResource(R.string.remote_fallback_title)
    val title =
        remember(deviceId, brandId, isFavorite) {
            if (device != null && brand != null) {
                FavoriteManager.getCustomName(context, deviceId, brandId, defaultRemoteName(context, device, brand))
            } else {
                fallbackTitle
            }
        }
    var menuOpen by remember { mutableStateOf(false) }
    val haptic = rememberVibratingHapticFeedback()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            deviceName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconToggleButton(
                        checked = isFavorite,
                        onCheckedChange = {
                            haptic.performHapticFeedback(
                                androidx.compose.ui.hapticfeedback.HapticFeedbackType.ToggleOn)
                            isFavorite = FavoriteManager.toggleFavorite(context, deviceId, brandId)
                        },
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.cd_toggle_favorite),
                            tint =
                                if (isFavorite) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options))
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.pin_to_home)) },
                                leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) },
                                onClick = {
                                    menuOpen = false
                                    ShortcutHelper.pinRemoteShortcut(
                                        context = context,
                                        deviceId = deviceId,
                                        brandId = brandId,
                                        deviceName = deviceName,
                                        brandName = brand?.nameEn ?: fallbackTitle,
                                    )
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier =
                    Modifier.widthIn(max = RemoteDefaults.ContentMaxWidth)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (commands != null) ModelRemoteLayout(commands.keys.keys, onKey) else when (deviceId) {
                    3 ->
                        AcLayout(
                            state = ac,
                            protocol = (profile as? RemoteProfile.AirConditioner)?.protocol ?: AcProtocol.GREE,
                            onChange = {
                                ac = it
                                send(it.toPulses((profile as? RemoteProfile.AirConditioner)?.protocol ?: AcProtocol.GREE))
                            },
                        )
                    1, 2, 4, 11, 12 -> TvLayout(onKeyPressed = onKey)
                    6 -> FanLayout(onKeyPressed = onKey)
                    8 -> AudioLayout(onKeyPressed = onKey)
                    10 -> ProjectorLayout(onKeyPressed = onKey)
                    13 -> CameraLayout(onKeyPressed = onKey)
                    else -> GenericLayout(onKeyPressed = onKey)
                }
            }
        }
    }
}
