package com.anyremote.remote.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import com.anyremote.remote.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anyremote.remote.data.RemoteDatabase
import com.anyremote.remote.ir.IREncodingEngine
import androidx.compose.ui.hapticfeedback.HapticFeedback
import com.anyremote.remote.ir.IRTransmitter
import com.anyremote.remote.service.FavoriteManager
import com.anyremote.remote.service.ShortcutHelper
import com.anyremote.remote.ui.theme.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteControllerScreen(
    deviceId: Int,
    brandId: Int,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    // Find device and brand info
    val device = remember(deviceId) { RemoteDatabase.deviceTypes.firstOrNull { it.id == deviceId } }
    val brand = remember(brandId, device) { device?.brands?.firstOrNull { it.id == brandId } }

    var isFavorite by remember(deviceId, brandId) {
        mutableStateOf(FavoriteManager.isFavorite(context, deviceId, brandId))
    }

    // Register dynamic launcher shortcuts on screen load
    LaunchedEffect(deviceId, brandId, device, brand) {
        if (device != null && brand != null) {
            ShortcutHelper.registerDynamicShortcuts(
                context = context,
                deviceId = deviceId,
                brandId = brandId,
                deviceName = device.nameEn,
                brandName = brand.nameEn
            )
        }
    }
    
    // Hold transmitted pulses state for real-time oscilloscope sweep
    var currentPulses by remember { mutableStateOf(intArrayOf()) }

    // Transmit physical IR pulses via Android ConsumerIrManager whenever pulses change
    LaunchedEffect(currentPulses) {
        if (currentPulses.isNotEmpty()) {
            val freq = if (deviceId == 3) 38000 else 38200
            IRTransmitter.transmit(context, currentPulses, freq)
        }
    }
    
    // Hold active AC configurations
    var acPower by remember { mutableStateOf(true) }
    var acTemp by remember { mutableIntStateOf(24) }
    var acMode by remember { mutableStateOf("MC") } // Cool by default
    var acSpeed by remember { mutableStateOf("SA") } // Auto speed by default
    var acSwing by remember { mutableStateOf(true) }

    // Dynamic accent glow depending on selected Mode
    val activeGlow = remember(acMode, acPower) {
        if (!acPower) Color.DarkGray
        else when (acMode) {
            "MC" -> ElectricBlue
            "MH" -> WarmOrange
            "MD" -> AccentPurple
            "MV" -> AccentGreen
            else -> ElectricBlue
        }
    }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val controllerBg = remember(deviceId, surfaceColor, onSurfaceColor) {
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
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = device?.nameEn ?: "Controller",
                            color = subTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = contentColor)
                    }
                },
                actions = {
                    val haptic = rememberVibratingHapticFeedback()
                    
                    // 1. Interactive Favorite Toggle
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val added = FavoriteManager.toggleFavorite(context, deviceId, brandId)
                            isFavorite = added
                        }
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (isFavorite) Color(0xFFEF4444) else contentColor
                        )
                    }

                    // 2. Pin to Launcher Dynamic Shortcut
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            ShortcutHelper.pinRemoteShortcut(
                                context = context,
                                deviceId = deviceId,
                                brandId = brandId,
                                deviceName = device?.nameEn ?: "Controller",
                                brandName = brand?.nameEn ?: "Brand"
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pin to Home Screen",
                            tint = contentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = controllerBg
                )
            )
        },
        containerColor = controllerBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            // 2. Load custom interactive layout based on deviceId
            when (deviceId) {
                3 -> {
                    // Air Conditioner Layout
                    AcLayout(
                        power = acPower,
                        temp = acTemp,
                        mode = acMode,
                        speed = acSpeed,
                        swing = acSwing,
                        activeGlow = activeGlow,
                        onPowerToggle = {
                            acPower = !acPower
                            currentPulses = IREncodingEngine.compileGreeAc(acPower, acTemp, acMode, acSpeed, acSwing)
                        },
                        onTempChange = {
                            acTemp = it
                            currentPulses = IREncodingEngine.compileGreeAc(acPower, acTemp, acMode, acSpeed, acSwing)
                        },
                        onModeChange = {
                            acMode = it
                            currentPulses = IREncodingEngine.compileGreeAc(acPower, acTemp, acMode, acSpeed, acSwing)
                        },
                        onSpeedChange = {
                            acSpeed = it
                            currentPulses = IREncodingEngine.compileGreeAc(acPower, acTemp, acMode, acSpeed, acSwing)
                        },
                        onSwingToggle = {
                            acSwing = !acSwing
                            currentPulses = IREncodingEngine.compileGreeAc(acPower, acTemp, acMode, acSpeed, acSwing)
                        }
                    )
                }
                1, 2, 4, 11, 12 -> {
                    // TV / STB / DVD / Box Layout
                    TvLayout(
                        onKeyPressed = { cmdId ->
                            currentPulses = IREncodingEngine.compileGenericNec(address = 1, command = cmdId)
                        }
                    )
                }
                6 -> {
                    // Fan Layout
                    FanLayout(
                        onKeyPressed = { cmdId ->
                            currentPulses = IREncodingEngine.compileGenericNec(address = 6, command = cmdId)
                        }
                    )
                }
                8 -> {
                    // Audio / Speaker Layout
                    AudioLayout(
                        onKeyPressed = { cmdId ->
                            currentPulses = IREncodingEngine.compileGenericNec(address = 8, command = cmdId)
                        }
                    )
                }
                10 -> {
                    // Projector Layout
                    ProjectorLayout(
                        onKeyPressed = { cmdId ->
                            currentPulses = IREncodingEngine.compileGenericNec(address = 10, command = cmdId)
                        }
                    )
                }
                13 -> {
                    // Camera / DSLR Layout
                    CameraLayout(
                        onTrigger = { delay ->
                            currentPulses = IREncodingEngine.compileGenericNec(address = 15, command = delay)
                        }
                    )
                }
                else -> {
                    // Standard General Layout
                    GenericLayout(
                        onKeyPressed = { cmdId ->
                            currentPulses = IREncodingEngine.compileGenericNec(address = 1, command = cmdId)
                        }
                    )
                }
            }
        }
    }
}

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
    onSwingToggle: () -> Unit
) {
    val haptic = rememberVibratingHapticFeedback()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Digital Status Screen Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$temp",
                    color = if (power) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                        text = "°C",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 18.dp, start = 4.dp)
                    )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                    val (modeIcon, modeLabel) = when (mode) {
                        "MC" -> Icons.Default.AcUnit to "Cool"
                        "MH" -> Icons.Default.WbSunny to "Heat"
                        "MD" -> Icons.Default.Opacity to "Dry"
                        "MV" -> Icons.Default.Air to "Fan"
                        else -> Icons.Default.Autorenew to "Auto"
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(modeIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(modeLabel, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    val speedLabel = when (speed) {
                        "SA" -> "Auto Wind"
                        "SL" -> "Low Wind"
                        "SM" -> "Medium Wind"
                        else -> "High Wind"
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Air, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(speedLabel, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (swing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CompareArrows, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.swing), color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Tabular White Actions Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // ROW 1: Power & Mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPowerToggle()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Power",
                                tint = if (power) CrimsonRed else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "POWER",
                                color = if (power) CrimsonRed else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.5.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(enabled = power) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val nextMode = when (mode) {
                                    "MC" -> "MH"
                                    "MH" -> "MD"
                                    "MD" -> "MV"
                                    "MV" -> "MA"
                                    else -> "MC"
                                }
                                onModeChange(nextMode)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val modeIcon = when (mode) {
                                "MC" -> Icons.Default.AcUnit
                                "MH" -> Icons.Default.WbSunny
                                "MD" -> Icons.Default.Opacity
                                "MV" -> Icons.Default.Air
                                else -> Icons.Default.Autorenew
                            }
                            val modeLabel = when (mode) {
                                "MC" -> "COOL"
                                "MH" -> "HEAT"
                                "MD" -> "DRY"
                                "MV" -> "FAN"
                                else -> "AUTO"
                            }
                            Icon(
                                imageVector = modeIcon,
                                contentDescription = "Mode",
                                tint = if (power) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "MODE ($modeLabel)",
                                color = if (power) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                )

                // ROW 2: Temp Down, Label, Temp Up
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(enabled = power) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val newTemp = (temp - 1).coerceIn(16, 30)
                                if (newTemp != temp) onTempChange(newTemp)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Temp Down",
                            tint = if (power) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.5.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TEMPERATURE",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.5.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(enabled = power) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val newTemp = (temp + 1).coerceIn(16, 30)
                                if (newTemp != temp) onTempChange(newTemp)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Temp Up",
                            tint = if (power) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                )

                // ROW 3: Wind Speed, Direction, Sweep
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(enabled = power) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val nextSpeed = when (speed) {
                                    "SA" -> "SL"
                                    "SL" -> "SM"
                                    "SM" -> "SH"
                                    else -> "SA"
                                }
                                onSpeedChange(nextSpeed)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val speedLabel = when (speed) {
                                "SA" -> "AUTO"
                                "SL" -> "LOW"
                                "SM" -> "MED"
                                else -> "HIGH"
                            }
                            Icon(
                                imageVector = Icons.Default.Air,
                                contentDescription = "Wind Speed",
                                tint = if (power) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SPEED ($speedLabel)",
                                color = if (power) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.5.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(enabled = power) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSwingToggle()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SyncAlt,
                                contentDescription = "Wind Direct",
                                tint = if (power) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "DIRECTION",
                                color = if (power) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.5.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(enabled = power) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSwingToggle()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = "Wind Sweep",
                                tint = if (swing && power) ElectricBlue else if (power) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (swing && power) "SWEEP ON" else "SWEEP",
                                color = if (swing && power) ElectricBlue else if (power) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TvLayout(
    onKeyPressed: (Int) -> Unit
) {
    val haptic = rememberVibratingHapticFeedback()
    var showNumericPad by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Top Action Row (Power Red button, Source Center button, Numeric Pad toggle, Mute Right button)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyPressed(12) // TV Power code
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PowerSettingsNew, contentDescription = "Power", tint = CrimsonRed)
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyPressed(11) // Source / Input code
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Input, contentDescription = "Source", tint = MaterialTheme.colorScheme.onSurface)
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (showNumericPad) Color(0xFF00B0FF).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, if (showNumericPad) Color(0xFF00B0FF) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showNumericPad = !showNumericPad
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.num_pad), color = if (showNumericPad) Color(0xFF00B0FF) else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyPressed(13) // Mute code
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.VolumeMute, contentDescription = "Mute", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        if (showNumericPad) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                val numGrid = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("-", "0", "⌫")
                )
                
                numGrid.forEach { rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowKeys.forEach { key ->
                            val isSpecial = key == "-" || key == "⌫"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSpecial) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface)
                                    .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (key == "⌫") {
                                            onKeyPressed(67) // Backspace keycode
                                        } else if (key == "-") {
                                            onKeyPressed(69) // Minus keycode
                                        } else {
                                            val digit = key.toInt()
                                            onKeyPressed(7 + digit)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Concentric Dpad
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawLine(
                        color = dividerColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.5f
                    )
                    drawLine(
                        color = dividerColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.5f
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // Central OK button
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape)
                            .align(Alignment.Center)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onKeyPressed(20) // OK command
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.ok), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    // Up Arrow
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                            .size(54.dp)
                            .clip(CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onKeyPressed(16) // Up command
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(28.dp))
                    }

                    // Down Arrow
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .size(54.dp)
                            .clip(CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onKeyPressed(17) // Down command
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(28.dp))
                    }

                    // Left Arrow
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp)
                            .size(54.dp)
                            .clip(CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onKeyPressed(18) // Left command
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(28.dp))
                    }

                    // Right Arrow
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .size(54.dp)
                            .clip(CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onKeyPressed(19) // Right command
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(28.dp))
                    }
                }
            }

            // Utility Row (Back, Home, Menu)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onKeyPressed(4) // BACK code
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onKeyPressed(3) // HOME code
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.onSurface)
                }

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onKeyPressed(82) // MENU code
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Rockers (Volume / Channel pills)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyPressed(24) // Vol+ command
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Volume Up", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(stringResource(R.string.vol), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyPressed(25) // Vol- command
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = "Volume Down", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyPressed(26) // CH+ command
                    }) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Channel Up", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(stringResource(R.string.ch), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyPressed(27) // CH- command
                    }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Channel Down", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun FanLayout(
    onKeyPressed: (Int) -> Unit
) {
    val haptic = rememberVibratingHapticFeedback()
    var fanPower by remember { mutableStateOf(false) }
    var fanSpeed by remember { mutableIntStateOf(1) }
    var fanSwing by remember { mutableStateOf(false) }
    var fanMode by remember { mutableStateOf("Normal") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Digital Screen Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (fanPower) "FAN SPEED: $fanSpeed" else "FAN POWER OFF",
                    color = if (fanPower) Color(0xFF00B0FF) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (fanPower) {
                    Text(
                        text = "MODE: ${fanMode.uppercase()} | SWING: ${if (fanSwing) "ON" else "OFF"}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Concentric Power Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, if (fanPower) CrimsonRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        fanPower = !fanPower
                        onKeyPressed(12) // Power code
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Power",
                    tint = if (fanPower) CrimsonRed else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Speed Adjustment Keys
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .clickable(enabled = fanPower) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (fanSpeed > 1) fanSpeed--
                        onKeyPressed(25) // Speed Down
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.speed_minus), color = if (fanPower) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .clickable(enabled = fanPower) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (fanSpeed < 4) fanSpeed++
                        onKeyPressed(24) // Speed Up
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.speed_plus), color = if (fanPower) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        }

        // Swing and Mode Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (fanSwing && fanPower) Color(0xFF00B0FF).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, if (fanSwing && fanPower) Color(0xFF00B0FF) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .clickable(enabled = fanPower) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        fanSwing = !fanSwing
                        onKeyPressed(20) // Oscillate toggle
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.oscillate), color = if (fanSwing && fanPower) Color(0xFF00B0FF) else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .clickable(enabled = fanPower) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        fanMode = when (fanMode) {
                            "Normal" -> "Natural"
                            "Natural" -> "Sleep"
                            else -> "Normal"
                        }
                        onKeyPressed(1) // Mode toggle
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(fanMode.uppercase(), color = if (fanPower) Color(0xFF00B0FF) else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AudioLayout(
    onKeyPressed: (Int) -> Unit
) {
    val haptic = rememberVibratingHapticFeedback()
    var audioPower by remember { mutableStateOf(false) }
    var audioSource by remember { mutableStateOf("Bluetooth") }
    var audioVol by remember { mutableIntStateOf(15) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (audioPower) "VOLUME: $audioVol" else "SPEAKER STANDBY",
                    color = if (audioPower) Color(0xFF00B0FF) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (audioPower) {
                    Text(
                        text = "SOURCE: ${audioSource.uppercase()}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, if (audioPower) CrimsonRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        audioPower = !audioPower
                        onKeyPressed(12) // Power code
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Power",
                    tint = if (audioPower) CrimsonRed else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape)
                    .clickable(enabled = audioPower) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyPressed(13) // Mute code
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeMute,
                    contentDescription = "Mute",
                    tint = if (audioPower) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .clickable(enabled = audioPower) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (audioVol > 0) audioVol--
                        onKeyPressed(25) // Vol-
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.vol_minus), color = if (audioPower) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .clickable(enabled = audioPower) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (audioVol < 30) audioVol++
                        onKeyPressed(24) // Vol+
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.vol_plus), color = if (audioPower) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val sources = listOf("BT" to "Bluetooth", "HDMI" to "HDMI", "OPT" to "Optical", "AUX" to "AUX")
            sources.forEach { (sKey, sLabel) ->
                val isActive = audioSource == sLabel && audioPower
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isActive) Color(0xFF00B0FF).copy(alpha = 0.1f) else Color.Transparent)
                        .clickable(enabled = audioPower) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            audioSource = sLabel
                            onKeyPressed(20) // Select source
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sKey,
                        color = if (isActive) Color(0xFF00B0FF) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectorLayout(
    onKeyPressed: (Int) -> Unit
) {
    val haptic = rememberVibratingHapticFeedback()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, CrimsonRed, CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyPressed(12) // Projector Power
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PowerSettingsNew, contentDescription = "Power", tint = CrimsonRed, modifier = Modifier.size(28.dp))
            }
        }

        // Silver Dpad
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyPressed(20) // OK select
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.ok), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyPressed(16) // Up
                    },
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = MaterialTheme.colorScheme.onSurface)
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyPressed(17) // Down
                    },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = MaterialTheme.colorScheme.onSurface)
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyPressed(18) // Left
                    },
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = MaterialTheme.colorScheme.onSurface)
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyPressed(19) // Right
                    },
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onKeyPressed(4) // Return/Back
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }

            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onKeyPressed(3) // Home
            }) {
                Icon(Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.onSurface)
            }

            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onKeyPressed(82) // Menu
            }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyPressed(30) // Focus -
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.focus_minus), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKeyPressed(31) // Focus +
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.focus_plus), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CameraLayout(
    onTrigger: (Int) -> Unit
) {
    val haptic = rememberVibratingHapticFeedback()
    var delaySecs by remember { mutableIntStateOf(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "DSLR SHUTTER RELEASE",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        // Shutter Button
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(4.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTrigger(delaySecs)
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(CrimsonRed)
                    .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Shutter",
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val delays = listOf(0 to "Instant", 2 to "2s Delay", 5 to "5s Delay", 10 to "10s Delay")
            delays.forEach { (sec, label) ->
                val isActive = delaySecs == sec
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isActive) Color(0xFF00B0FF).copy(alpha = 0.1f) else Color.Transparent)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            delaySecs = sec
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isActive) Color(0xFF00B0FF) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GenericLayout(
    onKeyPressed: (Int) -> Unit
) {
    val haptic = rememberVibratingHapticFeedback()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, CrimsonRed, CircleShape)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onKeyPressed(0) // Power
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PowerSettingsNew, contentDescription = "Power", tint = CrimsonRed, modifier = Modifier.size(28.dp))
        }

        val keys = listOf(
            0 to "Timer", 1 to "Mode", 2 to "Speed +",
            3 to "Speed -", 4 to "Mute", 5 to "Menu"
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            items(keys) { (cmd, label) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onKeyPressed(cmd)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
