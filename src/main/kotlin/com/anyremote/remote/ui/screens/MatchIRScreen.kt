package com.anyremote.remote.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anyremote.remote.R
import com.anyremote.remote.data.RemoteDatabase
import com.anyremote.remote.ir.AcState
import com.anyremote.remote.ir.IRTransmitter
import com.anyremote.remote.ir.IrCodebook
import com.anyremote.remote.ir.RemoteKey
import com.anyremote.remote.service.FavoriteManager
import com.anyremote.remote.ui.components.RemoteButton
import com.anyremote.remote.ui.components.RemoteDefaults
import com.anyremote.remote.ui.helpers.*
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val category = remember(deviceId) { RemoteDatabase.deviceTypes.firstOrNull { it.id == deviceId } }
    val brand = remember(brandId, category) { category?.brands?.firstOrNull { it.id == brandId } }

    val totalCodesets = IrCodebook.CODESET_COUNT
    var codeset by rememberSaveable { mutableIntStateOf(1) }
    var showResponseSheet by rememberSaveable { mutableStateOf(false) }
    var showSuccessDialog by rememberSaveable { mutableStateOf(false) }
    var showEditRemote by rememberSaveable { mutableStateOf(false) }

    fun nextCodeset() {
        codeset = if (codeset < totalCodesets) codeset + 1 else 1
    }
    fun previousCodeset() {
        codeset = if (codeset > 1) codeset - 1 else totalCodesets
    }

    val (testLabelRes, testIcon) =
        when (deviceId) {
            6 -> R.string.button_fan_speed to Icons.Default.Air
            8 -> R.string.button_mute to Icons.AutoMirrored.Filled.VolumeOff
            else -> R.string.button_power to Icons.Default.PowerSettingsNew
        }
    val testKey =
        when (deviceId) {
            6 -> RemoteKey.SPEED_UP
            8 -> RemoteKey.MUTE
            else -> RemoteKey.POWER
        }

    // Same codebook as RemoteControllerScreen, so the code set that works here is the one the remote uses.
    fun transmitTestPulse() {
        val pulses =
            if (deviceId == 3) AcState(power = true, swing = false).toPulses()
            else IrCodebook.pulses(deviceId, codeset, testKey)
        scope.launch { IRTransmitter.send(context, pulses) }
    }

    if (showEditRemote && category != null && brand != null) {
        EditRemoteScreen(
            device = category,
            brand = brand,
            onDismiss = {
                showEditRemote = false
                onSaveAndReturnToDashboard()
            },
            onSave = { newName ->
                FavoriteManager.setCustomName(context, deviceId, brandId, newName)
                showEditRemote = false
                onSaveAndReturnToDashboard()
            },
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pair_new_remote)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                // Which device we're pairing.
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(64.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(getDeviceIcon(deviceId), contentDescription = null, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        brand?.nameEn ?: stringResource(R.string.remote_fallback_title),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        category?.localizedName() ?: stringResource(R.string.controller_fallback_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    stringResource(R.string.point_blaster_instruction),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )

                RemoteButton(
                    onClick = {
                        transmitTestPulse()
                        showResponseSheet = true
                    },
                    modifier = Modifier.size(168.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(testIcon, contentDescription = stringResource(R.string.cd_test_signal), modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(testLabelRes), style = MaterialTheme.typography.titleMedium)
                    }
                }

                // Code set stepper.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            previousCodeset()
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.cd_previous_code),
                        )
                    }
                    Text(
                        stringResource(R.string.code_set_of, codeset, totalCodesets),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier.widthIn(min = 160.dp).padding(horizontal = 16.dp).semantics {
                                liveRegion = LiveRegionMode.Polite
                            },
                    )
                    FilledTonalIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            nextCodeset()
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.cd_next_code),
                        )
                    }
                }

                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            stringResource(R.string.info_banner_text),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    if (showResponseSheet) {
        ModalBottomSheet(onDismissRequest = { showResponseSheet = false }, sheetState = sheetState) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.did_device_respond),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(
                        R.string.did_device_respond_detail,
                        brand?.nameEn ?: stringResource(R.string.device_word),
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    stringResource(R.string.code_set_of, codeset, totalCodesets),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.Reject)
                            scope.launch { sheetState.hide() }.invokeOnCompletion { showResponseSheet = false }
                            nextCodeset()
                        },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(R.string.no_try_next), textAlign = TextAlign.Center)
                    }
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                            FavoriteManager.setCodeset(context, deviceId, brandId, codeset)
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showResponseSheet = false
                                showSuccessDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(R.string.yes_it_works), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(stringResource(R.string.test_successful), textAlign = TextAlign.Center) },
            text = { Text(stringResource(R.string.save_remote_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        if (!FavoriteManager.isFavorite(context, deviceId, brandId)) {
                            FavoriteManager.toggleFavorite(context, deviceId, brandId)
                        }
                        showEditRemote = true
                    },
                ) {
                    Text(stringResource(R.string.yes_add_to_main_page))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onMatchSuccess()
                    },
                ) {
                    Text(stringResource(R.string.just_test_controller))
                }
            },
        )
    }
}
