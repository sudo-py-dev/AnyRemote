package com.anyremote.remote.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anyremote.remote.R
import com.anyremote.remote.data.RemoteDatabase
import com.anyremote.remote.ir.AcState
import com.anyremote.remote.ir.IrCodebook
import com.anyremote.remote.ir.RemoteKey
import com.anyremote.remote.ir.IRTransmitter
import com.anyremote.remote.service.FavoriteManager
import com.anyremote.remote.service.ShortcutHelper
import com.anyremote.remote.ui.components.*
import com.anyremote.remote.ui.helpers.*
import com.anyremote.remote.ui.layouts.*
import com.anyremote.remote.ui.theme.*
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
        remember(deviceId, brandId) {
            mutableStateOf(FavoriteManager.isFavorite(context, deviceId, brandId))
        }

    LaunchedEffect(deviceId, brandId, device, brand) {
        if (device != null && brand != null) {
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
    val onKey: (RemoteKey) -> Unit = { key -> send(IrCodebook.pulses(deviceId, codeset, key)) }

    var ac by rememberSaveable(stateSaver = AcState.Saver) { mutableStateOf(AcState()) }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val controllerBg =
        remember(deviceId, surfaceColor, onSurfaceColor) {
            if (deviceId == 3) surfaceColor else onSurfaceColor.copy(alpha = 0.05f)
        }
    val contentColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = brand?.nameEn?.uppercase() ?: "REMOTE",
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp,
                        )
                        Text(
                            text =
                                device?.localizedName()
                                    ?: stringResource(R.string.controller_fallback_subtitle),
                            color = subTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = contentColor)
                    }
                },
                actions = {
                    val haptic = rememberVibratingHapticFeedback()

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val added = FavoriteManager.toggleFavorite(context, deviceId, brandId)
                            isFavorite = added
                        },
                    ) {
                        Icon(
                            imageVector =
                                if (isFavorite) Icons.Default.Favorite
                                else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (isFavorite) Color(0xFFEF4444) else contentColor,
                        )
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            ShortcutHelper.pinRemoteShortcut(
                                context = context,
                                deviceId = deviceId,
                                brandId = brandId,
                                deviceName =
                                    device?.getLocalizedName(context)
                                        ?: context.getString(R.string.controller_fallback_subtitle),
                                brandName = brand?.nameEn ?: "Brand",
                            )
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pin to Home Screen",
                            tint = contentColor,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = controllerBg,
                    ),
            )
        },
        containerColor = controllerBg,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (deviceId) {
                3 ->
                    AcLayout(
                        state = ac,
                        onChange = {
                            ac = it
                            send(it.toPulses())
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
