package com.anyremote.remote.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.anyremote.remote.ir.IREncodingEngine
import com.anyremote.remote.ir.IRTransmitter
import com.anyremote.remote.service.FavoriteManager
import com.anyremote.remote.service.ShortcutHelper
import com.anyremote.remote.ui.components.*
import com.anyremote.remote.ui.helpers.*
import com.anyremote.remote.ui.layouts.*
import com.anyremote.remote.ui.theme.*

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

    var currentPulses by remember { mutableStateOf(intArrayOf()) }

    LaunchedEffect(currentPulses) {
        if (currentPulses.isNotEmpty()) {
            val freq = if (deviceId == 3) 38000 else 38200
            IRTransmitter.transmit(context, currentPulses, freq)
        }
    }

    var acPower by remember { mutableStateOf(true) }
    var acTemp by remember { mutableIntStateOf(24) }
    var acMode by remember { mutableStateOf("MC") }
    var acSpeed by remember { mutableStateOf("SA") }
    var acSwing by remember { mutableStateOf(true) }
    var acTurbo by remember { mutableStateOf(false) }
    var acLight by remember { mutableStateOf(true) }
    var acAuxHeat by remember { mutableStateOf(false) }
    var acHealth by remember { mutableStateOf(false) }
    var acVentilation by remember { mutableStateOf(false) }
    var acEco by remember { mutableStateOf(false) }
    var acSwingH by remember { mutableStateOf(false) }
    var acMute by remember { mutableStateOf(false) }
    var acTempDisplay by remember { mutableStateOf(false) }

    val activeGlow =
        remember(acMode, acPower) {
            if (!acPower) {
                Color.DarkGray
            } else {
                when (acMode) {
                    "MC" -> ElectricBlue
                    "MH" -> WarmOrange
                    "MD" -> AccentPurple
                    "MV" -> AccentGreen
                    else -> ElectricBlue
                }
            }
        }

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
                3 -> {

                    AcLayout(
                        power = acPower,
                        temp = acTemp,
                        mode = acMode,
                        speed = acSpeed,
                        swing = acSwing,
                        turbo = acTurbo,
                        light = acLight,
                        auxHeat = acAuxHeat,
                        health = acHealth,
                        ventilation = acVentilation,
                        eco = acEco,
                        swingH = acSwingH,
                        mute = acMute,
                        tempDisplay = acTempDisplay,
                        activeGlow = activeGlow,
                        onPowerToggle = {
                            acPower = !acPower
                            currentPulses =
                                IREncodingEngine.compileGreeAc(
                                    power = acPower,
                                    temp = acTemp,
                                    mode = acMode,
                                    fanSpeed = acSpeed,
                                    swing = acSwing,
                                    turbo = acTurbo,
                                    light = acLight,
                                    auxHeat = acAuxHeat,
                                    health = acHealth,
                                    ventilation = acVentilation,
                                    eco = acEco,
                                    swingH = acSwingH,
                                    mute = acMute,
                                    tempDisplay = acTempDisplay,
                                )
                        },
                        onTempChange = {
                            acTemp = it
                            currentPulses =
                                IREncodingEngine.compileGreeAc(
                                    power = acPower,
                                    temp = acTemp,
                                    mode = acMode,
                                    fanSpeed = acSpeed,
                                    swing = acSwing,
                                    turbo = acTurbo,
                                    light = acLight,
                                    auxHeat = acAuxHeat,
                                    health = acHealth,
                                    ventilation = acVentilation,
                                    eco = acEco,
                                    swingH = acSwingH,
                                    mute = acMute,
                                    tempDisplay = acTempDisplay,
                                )
                        },
                        onModeChange = {
                            acMode = it
                            currentPulses =
                                IREncodingEngine.compileGreeAc(
                                    power = acPower,
                                    temp = acTemp,
                                    mode = acMode,
                                    fanSpeed = acSpeed,
                                    swing = acSwing,
                                    turbo = acTurbo,
                                    light = acLight,
                                    auxHeat = acAuxHeat,
                                    health = acHealth,
                                    ventilation = acVentilation,
                                    eco = acEco,
                                    swingH = acSwingH,
                                    mute = acMute,
                                    tempDisplay = acTempDisplay,
                                )
                        },
                        onSpeedChange = {
                            acSpeed = it
                            currentPulses =
                                IREncodingEngine.compileGreeAc(
                                    power = acPower,
                                    temp = acTemp,
                                    mode = acMode,
                                    fanSpeed = acSpeed,
                                    swing = acSwing,
                                    turbo = acTurbo,
                                    light = acLight,
                                    auxHeat = acAuxHeat,
                                    health = acHealth,
                                    ventilation = acVentilation,
                                    eco = acEco,
                                    swingH = acSwingH,
                                    mute = acMute,
                                    tempDisplay = acTempDisplay,
                                )
                        },
                        onSwingToggle = {
                            acSwing = !acSwing
                            currentPulses =
                                IREncodingEngine.compileGreeAc(
                                    power = acPower,
                                    temp = acTemp,
                                    mode = acMode,
                                    fanSpeed = acSpeed,
                                    swing = acSwing,
                                    turbo = acTurbo,
                                    light = acLight,
                                    auxHeat = acAuxHeat,
                                    health = acHealth,
                                    ventilation = acVentilation,
                                    eco = acEco,
                                    swingH = acSwingH,
                                    mute = acMute,
                                    tempDisplay = acTempDisplay,
                                )
                        },
                        onTurboToggle = {
                            acTurbo = !acTurbo
                            currentPulses =
                                IREncodingEngine.compileGreeAc(
                                    power = acPower,
                                    temp = acTemp,
                                    mode = acMode,
                                    fanSpeed = acSpeed,
                                    swing = acSwing,
                                    turbo = acTurbo,
                                    light = acLight,
                                    auxHeat = acAuxHeat,
                                    health = acHealth,
                                    ventilation = acVentilation,
                                    eco = acEco,
                                    swingH = acSwingH,
                                    mute = acMute,
                                    tempDisplay = acTempDisplay,
                                )
                        },
                        onLightToggle = {
                            acLight = !acLight
                            currentPulses =
                                IREncodingEngine.compileGreeAc(
                                    power = acPower,
                                    temp = acTemp,
                                    mode = acMode,
                                    fanSpeed = acSpeed,
                                    swing = acSwing,
                                    turbo = acTurbo,
                                    light = acLight,
                                    auxHeat = acAuxHeat,
                                    health = acHealth,
                                    ventilation = acVentilation,
                                    eco = acEco,
                                    swingH = acSwingH,
                                    mute = acMute,
                                    tempDisplay = acTempDisplay,
                                )
                        },
                        onAuxHeatToggle = {
                            acAuxHeat = !acAuxHeat
                            currentPulses =
                                IREncodingEngine.compileGreeAc(
                                    power = acPower,
                                    temp = acTemp,
                                    mode = acMode,
                                    fanSpeed = acSpeed,
                                    swing = acSwing,
                                    turbo = acTurbo,
                                    light = acLight,
                                    auxHeat = acAuxHeat,
                                    health = acHealth,
                                    ventilation = acVentilation,
                                    eco = acEco,
                                    swingH = acSwingH,
                                    mute = acMute,
                                    tempDisplay = acTempDisplay,
                                )
                        },
                        onHealthToggle = {
                            acHealth = !acHealth
                            currentPulses =
                                IREncodingEngine.compileGreeAc(
                                    power = acPower,
                                    temp = acTemp,
                                    mode = acMode,
                                    fanSpeed = acSpeed,
                                    swing = acSwing,
                                    turbo = acTurbo,
                                    light = acLight,
                                    auxHeat = acAuxHeat,
                                    health = acHealth,
                                    ventilation = acVentilation,
                                    eco = acEco,
                                    swingH = acSwingH,
                                    mute = acMute,
                                    tempDisplay = acTempDisplay,
                                )
                        },
                        onVentilationToggle = {
                            acVentilation = !acVentilation
                            currentPulses =
                                IREncodingEngine.compileGreeAc(
                                    power = acPower,
                                    temp = acTemp,
                                    mode = acMode,
                                    fanSpeed = acSpeed,
                                    swing = acSwing,
                                    turbo = acTurbo,
                                    light = acLight,
                                    auxHeat = acAuxHeat,
                                    health = acHealth,
                                    ventilation = acVentilation,
                                    eco = acEco,
                                    swingH = acSwingH,
                                    mute = acMute,
                                    tempDisplay = acTempDisplay,
                                )
                        },
                        onEcoToggle = {
                            acEco = !acEco
                            currentPulses =
                                IREncodingEngine.compileGreeAc(
                                    power = acPower,
                                    temp = acTemp,
                                    mode = acMode,
                                    fanSpeed = acSpeed,
                                    swing = acSwing,
                                    turbo = acTurbo,
                                    light = acLight,
                                    auxHeat = acAuxHeat,
                                    health = acHealth,
                                    ventilation = acVentilation,
                                    eco = acEco,
                                    swingH = acSwingH,
                                    mute = acMute,
                                    tempDisplay = acTempDisplay,
                                )
                        },
                        onSwingHToggle = {
                            acSwingH = !acSwingH
                            currentPulses =
                                IREncodingEngine.compileGreeAc(
                                    power = acPower,
                                    temp = acTemp,
                                    mode = acMode,
                                    fanSpeed = acSpeed,
                                    swing = acSwing,
                                    turbo = acTurbo,
                                    light = acLight,
                                    auxHeat = acAuxHeat,
                                    health = acHealth,
                                    ventilation = acVentilation,
                                    eco = acEco,
                                    swingH = acSwingH,
                                    mute = acMute,
                                    tempDisplay = acTempDisplay,
                                )
                        },
                        onMuteToggle = {
                            acMute = !acMute
                            currentPulses =
                                IREncodingEngine.compileGreeAc(
                                    power = acPower,
                                    temp = acTemp,
                                    mode = acMode,
                                    fanSpeed = acSpeed,
                                    swing = acSwing,
                                    turbo = acTurbo,
                                    light = acLight,
                                    auxHeat = acAuxHeat,
                                    health = acHealth,
                                    ventilation = acVentilation,
                                    eco = acEco,
                                    swingH = acSwingH,
                                    mute = acMute,
                                    tempDisplay = acTempDisplay,
                                )
                        },
                        onTempDisplayToggle = {
                            acTempDisplay = !acTempDisplay
                            currentPulses =
                                IREncodingEngine.compileGreeAc(
                                    power = acPower,
                                    temp = acTemp,
                                    mode = acMode,
                                    fanSpeed = acSpeed,
                                    swing = acSwing,
                                    turbo = acTurbo,
                                    light = acLight,
                                    auxHeat = acAuxHeat,
                                    health = acHealth,
                                    ventilation = acVentilation,
                                    eco = acEco,
                                    swingH = acSwingH,
                                    mute = acMute,
                                    tempDisplay = acTempDisplay,
                                )
                        },
                    )
                }
                1,
                2,
                4,
                11,
                12 -> {

                    TvLayout(
                        onKeyPressed = { cmdId ->
                            currentPulses =
                                IREncodingEngine.compileGenericNec(address = 1, command = cmdId)
                        },
                    )
                }
                6 -> {

                    FanLayout(
                        onKeyPressed = { cmdId ->
                            currentPulses =
                                IREncodingEngine.compileGenericNec(address = 6, command = cmdId)
                        },
                    )
                }
                8 -> {

                    AudioLayout(
                        onKeyPressed = { cmdId ->
                            currentPulses =
                                IREncodingEngine.compileGenericNec(address = 8, command = cmdId)
                        },
                    )
                }
                10 -> {

                    ProjectorLayout(
                        onKeyPressed = { cmdId ->
                            currentPulses =
                                IREncodingEngine.compileGenericNec(address = 10, command = cmdId)
                        },
                    )
                }
                13 -> {

                    CameraLayout(
                        onTrigger = { delay ->
                            currentPulses =
                                IREncodingEngine.compileGenericNec(address = 15, command = delay)
                        },
                    )
                }
                else -> {

                    GenericLayout(
                        onKeyPressed = { cmdId ->
                            currentPulses =
                                IREncodingEngine.compileGenericNec(address = 1, command = cmdId)
                        },
                    )
                }
            }
        }
    }
}
