package com.anyremote.remote.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.anyremote.remote.R
import com.anyremote.remote.data.RemoteDatabase
import com.anyremote.remote.data.RemoteDatabase.Brand
import com.anyremote.remote.data.RemoteDatabase.DeviceType
import com.anyremote.remote.service.FavoriteManager
import com.anyremote.remote.service.ShortcutHelper
import com.anyremote.remote.ui.components.*
import com.anyremote.remote.ui.helpers.*
import kotlinx.coroutines.launch

/** A remote saved on the dashboard. */
data class SavedRemote(val device: DeviceType, val brand: Brand, val name: String)

private fun loadSavedRemotes(context: android.content.Context): List<SavedRemote> =
    FavoriteManager.getFavorites(context)
        .mapNotNull { key ->
            val parts = key.split("_")
            if (parts.size != 2) return@mapNotNull null
            val deviceId = parts[0].toIntOrNull() ?: return@mapNotNull null
            val brandId = parts[1].toIntOrNull() ?: return@mapNotNull null
            val device =
                RemoteDatabase.deviceTypes.firstOrNull { it.id == deviceId }
                    ?: return@mapNotNull null
            val brand = device.brands.firstOrNull { it.id == brandId } ?: return@mapNotNull null
            SavedRemote(
                device,
                brand,
                FavoriteManager.getCustomName(
                    context,
                    device.id,
                    brand.id,
                    defaultRemoteName(context, device, brand),
                ),
            )
        }
        // Favorites are stored as a set; sort so tiles don't shuffle between launches.
        .sortedBy { it.name.lowercase() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onDeviceSelected: (deviceId: Int, brandId: Int) -> Unit,
    onFavoriteSelected: (deviceId: Int, brandId: Int) -> Unit,
    onSettingsClick: () -> Unit,
) {
    val context = LocalContext.current
    RemoteDatabase.init(context)

    var selectedDevice by remember { mutableStateOf<DeviceType?>(null) }
    var addingRemote by remember { mutableStateOf(false) }
    var remotes by remember { mutableStateOf(emptyList<SavedRemote>()) }
    LaunchedEffect(Unit) { remotes = loadSavedRemotes(context) }
    fun reload() {
        remotes = loadSavedRemotes(context)
    }

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

    var optionsFor by remember { mutableStateOf<SavedRemote?>(null) }
    var editing by remember { mutableStateOf<SavedRemote?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val removedMessage = stringResource(R.string.remote_removed)
    val undoLabel = stringResource(R.string.undo)

    BackHandler(enabled = editing == null && (selectedDevice != null || addingRemote)) {
        if (selectedDevice != null) selectedDevice = null else addingRemote = false
    }

    val editingRemote = editing
    if (editingRemote != null) {
        EditRemoteScreen(
            device = editingRemote.device,
            brand = editingRemote.brand,
            onDismiss = { editing = null },
            onSave = { newName ->
                FavoriteManager.setCustomName(
                    context,
                    editingRemote.device.id,
                    editingRemote.brand.id,
                    newName,
                )
                reload()
                editing = null
            },
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        selectedDevice?.localizedName()
                            ?: if (addingRemote) stringResource(R.string.select_device_type)
                            else stringResource(R.string.app_name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    if (selectedDevice != null || addingRemote) {
                        IconButton(
                            onClick = {
                                if (selectedDevice != null) selectedDevice = null
                                else addingRemote = false
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                            )
                        }
                    }
                },
                actions = {
                    if (!addingRemote) {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                Icons.Outlined.Settings,
                                contentDescription = stringResource(R.string.settings),
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !addingRemote && remotes.isNotEmpty(),
                modifier = Modifier.padding(bottom = 8.dp),
                enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 3 },
                exit = fadeOut(tween(100)),
            ) {
                AddRemoteButton(onClick = { addingRemote = true })
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val end = if (LocalLayoutDirection.current == LayoutDirection.Rtl) 0.08f else 0.92f
        val origin =
            if (remotes.isEmpty()) TransformOrigin(0.5f, 0.5f) else TransformOrigin(end, 0.97f)
        AnimatedContent(
            targetState = addingRemote,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            transitionSpec = {
                if (targetState) {
                    (fadeIn(tween(200)) +
                        scaleIn(
                            animationSpec = tween(280, easing = FastOutSlowInEasing),
                            initialScale = 0.76f,
                            transformOrigin = origin,
                        )) togetherWith fadeOut(tween(120))
                } else {
                    fadeIn(tween(200)) togetherWith
                        (fadeOut(tween(180)) +
                            scaleOut(
                                animationSpec = tween(220, easing = FastOutSlowInEasing),
                                targetScale = 0.84f,
                                transformOrigin = origin,
                            ))
                }
            },
            label = "Add remote picker",
        ) { showPicker ->
            if (showPicker) {
                val device = selectedDevice
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (device == null)
                        CategorySelectionGrid(onCategoryClick = { selectedDevice = it })
                    else
                        BrandSelectionPanel(
                            device = device,
                            onBrandClick = { onDeviceSelected(device.id, it.id) },
                        )
                }
            } else {
                RemotesHome(
                    remotes = remotes,
                    showIrNotice = showIrNotice,
                    onDismissIrNotice = {
                        FavoriteManager.setIrNoticeDismissed(context, true)
                        showIrNotice = false
                    },
                    onRemoteClick = { onFavoriteSelected(it.device.id, it.brand.id) },
                    onRemoteOptions = { optionsFor = it },
                    onAddClick = { addingRemote = true },
                )
            }
        }
    }

    val options = optionsFor
    if (options != null) {
        RemoteOptionsSheet(
            remote = options,
            onDismiss = { optionsFor = null },
            onRename = {
                optionsFor = null
                editing = options
            },
            onPin = {
                optionsFor = null
                ShortcutHelper.pinRemoteShortcut(
                    context = context,
                    deviceId = options.device.id,
                    brandId = options.brand.id,
                    deviceName = options.device.getLocalizedName(context),
                    brandName = options.brand.nameEn,
                )
            },
            onRemove = {
                optionsFor = null
                FavoriteManager.toggleFavorite(context, options.device.id, options.brand.id)
                reload()
                scope.launch {
                    val result =
                        snackbarHostState.showSnackbar(
                            removedMessage,
                            actionLabel = undoLabel,
                            duration = SnackbarDuration.Short,
                        )
                    if (
                        result == SnackbarResult.ActionPerformed &&
                            !FavoriteManager.isFavorite(
                                context,
                                options.device.id,
                                options.brand.id,
                            )
                    ) {
                        FavoriteManager.toggleFavorite(context, options.device.id, options.brand.id)
                        reload()
                    }
                }
            },
        )
    }
}

@Composable
private fun AddRemoteButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = { Text(stringResource(R.string.add_remote)) },
        interactionSource = interaction,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.pressScale(interaction, pressedScale = 0.94f),
    )
}

@Composable
internal fun RemotesHome(
    remotes: List<SavedRemote>,
    showIrNotice: Boolean,
    onDismissIrNotice: () -> Unit,
    onRemoteClick: (SavedRemote) -> Unit,
    onRemoteOptions: (SavedRemote) -> Unit,
    onAddClick: () -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (RemoteDatabase.loadFailed) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                NoticeCard(
                    title = null,
                    message = stringResource(R.string.db_load_error),
                    onDismiss = null,
                )
            }
        }
        if (showIrNotice) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                NoticeCard(
                    title = stringResource(R.string.ir_sensor_missing_title),
                    message = stringResource(R.string.ir_sensor_missing_message),
                    onDismiss = onDismissIrNotice,
                )
            }
        }

        if (remotes.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { EmptyRemotes(onAddClick) }
        } else {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    stringResource(R.string.your_remotes),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
                )
            }
            items(remotes, key = { "${it.device.id}_${it.brand.id}" }) { remote ->
                RemoteTile(
                    remote = remote,
                    modifier = Modifier.animateItem(),
                    onClick = { onRemoteClick(remote) },
                    onOptions = { onRemoteOptions(remote) },
                )
            }
        }
    }
}

@Composable
private fun NoticeCard(
    title: String?,
    message: String,
    onDismiss: (() -> Unit)?,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(Icons.Default.WarningAmber, contentDescription = null)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                if (title != null) {
                    Text(title, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                }
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
            if (onDismiss != null) {
                IconButton(onClick = onDismiss, modifier = Modifier.offset(y = (-12).dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.ir_sensor_missing_action),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRemotes(onAddClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 64.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(112.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.SettingsRemote,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.main_page_empty),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.main_page_empty_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAddClick, contentPadding = ButtonDefaults.ButtonWithIconContentPadding) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.add_remote))
        }
    }
}

/** Google Home–style device tile. Long-press or the ⋮ button opens its options. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RemoteTile(
    remote: SavedRemote,
    onClick: () -> Unit,
    onOptions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberVibratingHapticFeedback()
    val interaction = remember { MutableInteractionSource() }
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier =
            modifier
                .fillMaxWidth()
                .pressScale(interaction, pressedScale = 0.97f)
                .clip(MaterialTheme.shapes.large)
                .combinedClickable(
                    interactionSource = interaction,
                    onClick = onClick,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOptions()
                    },
                ),
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 16.dp, end = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color =
                        if (remote.device.id == 3) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor =
                        if (remote.device.id == 3) MaterialTheme.colorScheme.onTertiaryContainer
                        else MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(getDeviceIcon(remote.device.id), contentDescription = null)
                    }
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onOptions) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_options),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                remote.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 12.dp),
            )
            Text(
                remote.device.localizedName(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemoteOptionsSheet(
    remote: SavedRemote,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onPin: () -> Unit,
    onRemove: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState =
            rememberBottomSheetState(
                initialValue = SheetValue.Hidden,
                enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
            ),
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            ListItem(
                supportingContent = { Text(remote.device.localizedName()) },
                leadingContent = {
                    Icon(getDeviceIcon(remote.device.id), contentDescription = null)
                },
                colors =
                    ListItemDefaults.colors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
            ) {
                Text(remote.name, style = MaterialTheme.typography.titleLarge)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SheetAction(Icons.Outlined.Edit, stringResource(R.string.rename_remote), onRename)
            SheetAction(Icons.Outlined.PushPin, stringResource(R.string.pin_to_home), onPin)
            SheetAction(
                Icons.Outlined.Delete,
                stringResource(R.string.remove_remote),
                onRemove,
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun SheetAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    ListItem(
        onClick = onClick,
        leadingContent = { Icon(icon, contentDescription = null, tint = tint) },
        colors =
            ListItemDefaults.colors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent
            ),
    ) {
        Text(label, color = tint)
    }
}
