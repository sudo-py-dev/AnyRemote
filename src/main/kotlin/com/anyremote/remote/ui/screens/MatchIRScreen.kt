package com.anyremote.remote.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anyremote.remote.R
import com.anyremote.remote.data.RemoteDatabase
import com.anyremote.remote.ir.AcState
import com.anyremote.remote.ir.IrCodebook
import com.anyremote.remote.ir.RemoteKey
import com.anyremote.remote.ir.IRTransmitter
import com.anyremote.remote.service.FavoriteManager
import kotlinx.coroutines.launch
import com.anyremote.remote.ui.components.*
import com.anyremote.remote.ui.helpers.*
import com.anyremote.remote.ui.layouts.*
import com.anyremote.remote.ui.theme.*

private val AccentCyan = Color(0xFF00E5FF)
private val AccentGlow = Color(0xFF00B0FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchIRScreen(
    deviceId: Int,
    brandId: Int,
    onMatchSuccess: () -> Unit,
    onSaveAndReturnToDashboard: () -> Unit,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    RemoteDatabase.init(context)
    val haptic = rememberVibratingHapticFeedback()
    val sheetState =
        rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
        )

    val category =
        remember(deviceId, RemoteDatabase.deviceTypes) {
            RemoteDatabase.deviceTypes.firstOrNull { it.id == deviceId }
        }
    val brand = remember(brandId, category) { category?.brands?.firstOrNull { it.id == brandId } }

    val totalCodesets = IrCodebook.CODESET_COUNT
    var currentCodesetIndex by remember { mutableIntStateOf(1) }
    var showEditRemoteScreen by remember { mutableStateOf(false) }
    var isTestButtonPressed by remember { mutableStateOf(false) }
    var showMatchSuccessModal by remember { mutableStateOf(false) }
    var showResponsePopup by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "ButtonPulse")
    val buttonScale by
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.04f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "Pulse",
        )

    val (testButtonLabelRes, testButtonIcon) =
        remember(deviceId) {
            when (deviceId) {
                1 -> R.string.button_power to Icons.Default.PowerSettingsNew
                3 -> R.string.button_power to Icons.Default.PowerSettingsNew
                6 -> R.string.button_fan_speed to Icons.Default.Air
                8 -> R.string.button_mute to Icons.AutoMirrored.Filled.VolumeMute
                10 -> R.string.button_power to Icons.Default.PowerSettingsNew
                else -> R.string.button_power to Icons.Default.PowerSettingsNew
            }
        }

    val testKey =
        when (deviceId) {
            6 -> RemoteKey.SPEED_UP
            8 -> RemoteKey.MUTE
            else -> RemoteKey.POWER
        }
    val scope = rememberCoroutineScope()

    // Same codebook as RemoteControllerScreen, so the codeset that works here is the one the remote uses.
    fun transmitTestPulse() {
        haptic.performHapticFeedback(
            androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        val pulses =
            if (deviceId == 3) AcState(power = true, swing = false).toPulses()
            else IrCodebook.pulses(deviceId, currentCodesetIndex, testKey)
        scope.launch { IRTransmitter.send(context, pulses) }
    }

    Scaffold(
        topBar = {
            if (!showEditRemoteScreen) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.pair_new_remote),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize().padding(24.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text =
                            "${brand?.nameEn ?: "GENERIC"} - ${category?.localizedName() ?: stringResource(R.string.remote_fallback_title)}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.codeset_test_wizard),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier =
                            Modifier.background(
                                    MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .border(
                                    0.5.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text =
                                stringResource(
                                    R.string.config_codeset_index,
                                    currentCodesetIndex,
                                    totalCodesets),
                            color = ElectricBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.point_blaster_instruction),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 20.dp),
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier.size(160.dp)
                                .scale(if (isTestButtonPressed) 0.95f else buttonScale)
                                .clip(CircleShape)
                                .background(
                                    brush =
                                        Brush.radialGradient(
                                            colors =
                                                listOf(
                                                    Color(0xFF1E283C),
                                                    Color(0xFF131A28),
                                                ),
                                        ),
                                )
                                .border(
                                    width = 2.dp,
                                    brush =
                                        Brush.linearGradient(
                                            colors = listOf(AccentCyan, AccentGlow),
                                        ),
                                    shape = CircleShape,
                                )
                                .clickable {
                                    isTestButtonPressed = true
                                    transmitTestPulse()
                                    isTestButtonPressed = false
                                    showResponsePopup = true
                                },
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = testButtonIcon,
                                contentDescription = stringResource(R.string.cd_test_signal),
                                tint = AccentCyan,
                                modifier = Modifier.size(54.dp),
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = stringResource(testButtonLabelRes),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(
                                    androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress,
                                )
                                if (currentCodesetIndex > 1) {
                                    currentCodesetIndex--
                                } else {
                                    currentCodesetIndex = totalCodesets
                                }
                            },
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_previous_code),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }

                        Text(
                            text = stringResource(R.string.manual_cycle_config),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(
                                    androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress,
                                )
                                if (currentCodesetIndex < totalCodesets) {
                                    currentCodesetIndex++
                                } else {
                                    currentCodesetIndex = 1
                                }
                            },
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = stringResource(R.string.cd_next_code),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier =
                                Modifier.size(36.dp)
                                    .background(ElectricBlue.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = stringResource(R.string.info_banner_text),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            if (showResponsePopup) {
                ModalBottomSheet(
                    onDismissRequest = { showResponsePopup = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = {
                        Box(
                            modifier =
                                Modifier.padding(top = 16.dp, bottom = 8.dp)
                                    .width(40.dp)
                                    .height(4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(2.dp)),
                        )
                    },
                ) {
                    Column(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                                .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier =
                                Modifier.size(56.dp)
                                    .background(ElectricBlue.copy(alpha = 0.1f), CircleShape),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(28.dp),
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.did_device_respond),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            letterSpacing = 0.5.sp,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text =
                                stringResource(
                                    R.string.did_device_respond_detail,
                                    brand?.nameEn ?: stringResource(R.string.device_word)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            val noBtnBg =
                                if (isAppDarkTheme()) Color(0xFF451A1A) else Color(0xFFFFEBEE)
                            val noBtnBorder =
                                if (isAppDarkTheme()) Color(0xFF7F1D1D) else Color(0xFFFFCDD2)
                            val noBtnText =
                                if (isAppDarkTheme()) Color(0xFFFECACA) else Color(0xFFC62828)

                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(
                                        androidx.compose.ui.hapticfeedback.HapticFeedbackType
                                            .LongPress,
                                    )
                                    showResponsePopup = false

                                    if (currentCodesetIndex < totalCodesets) {
                                        currentCodesetIndex++
                                    } else {
                                        currentCodesetIndex = 1
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = noBtnBg),
                                shape = RoundedCornerShape(12.dp),
                                modifier =
                                    Modifier.weight(1f)
                                        .height(56.dp)
                                        .border(1.dp, noBtnBorder, RoundedCornerShape(12.dp)),
                            ) {
                                Text(
                                    text = stringResource(R.string.no_try_next),
                                    color = noBtnText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                )
                            }

                            val yesBtnBg =
                                if (isAppDarkTheme()) Color(0xFF14301D) else Color(0xFFE8F5E9)
                            val yesBtnBorder =
                                if (isAppDarkTheme()) Color(0xFF166534) else Color(0xFFC8E6C9)
                            val yesBtnText =
                                if (isAppDarkTheme()) Color(0xFFBBF7D0) else Color(0xFF2E7D32)

                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(
                                        androidx.compose.ui.hapticfeedback.HapticFeedbackType
                                            .LongPress,
                                    )
                                    showResponsePopup = false
                                    FavoriteManager.setCodeset(
                                        context, deviceId, brandId, currentCodesetIndex)
                                    showMatchSuccessModal = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = yesBtnBg),
                                shape = RoundedCornerShape(12.dp),
                                modifier =
                                    Modifier.weight(1f)
                                        .height(56.dp)
                                        .border(1.dp, yesBtnBorder, RoundedCornerShape(12.dp)),
                            ) {
                                Text(
                                    text = stringResource(R.string.yes_it_works),
                                    color = yesBtnText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    }
                }
            }

            if (showMatchSuccessModal) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)).clickable(
                            enabled = false) {},
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier =
                            Modifier.fillMaxWidth(0.85f)
                                .background(
                                    MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(24.dp))
                                .padding(28.dp),
                    ) {
                        val successIconBg =
                            if (isAppDarkTheme()) Color(0xFF14301D) else Color(0xFFE8F5E9)
                        val successIconBorder =
                            if (isAppDarkTheme()) Color(0xFF166534) else Color(0xFF2E7D32)
                        val successIconTint =
                            if (isAppDarkTheme()) Color(0xFFBBF7D0) else Color(0xFF2E7D32)

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier =
                                Modifier.size(64.dp)
                                    .background(successIconBg, CircleShape)
                                    .border(2.dp, successIconBorder, CircleShape),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.test_successful),
                                tint = successIconTint,
                                modifier = Modifier.size(32.dp),
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = stringResource(R.string.test_successful),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(R.string.add_to_main_page),
                            color = ElectricBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.save_remote_dialog_text),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Button(
                                onClick = {
                                    showMatchSuccessModal = false

                                    val isFav =
                                        FavoriteManager.isFavorite(context, deviceId, brandId)
                                    if (!isFav) {
                                        FavoriteManager.toggleFavorite(context, deviceId, brandId)
                                    }
                                    showEditRemoteScreen = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.yes_add_to_main_page),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    showMatchSuccessModal = false
                                    onMatchSuccess()
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                colors =
                                    ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                    ),
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.just_test_controller),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                )
                            }
                        }
                    }
                }
            }

            if (showEditRemoteScreen && category != null && brand != null) {
                EditRemoteScreen(
                    device = category,
                    brand = brand,
                    onDismiss = {
                        showEditRemoteScreen = false
                        onSaveAndReturnToDashboard()
                    },
                    onSave = { newName ->
                        FavoriteManager.setCustomName(context, deviceId, brandId, newName)
                        showEditRemoteScreen = false
                        onSaveAndReturnToDashboard()
                    })
            }
        }
    }
}
