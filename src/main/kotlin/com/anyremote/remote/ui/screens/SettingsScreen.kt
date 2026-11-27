package com.anyremote.remote.ui.screens

import android.app.StatusBarManager
import android.content.ComponentName
import android.graphics.drawable.Icon as AndroidIcon
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anyremote.remote.R
import com.anyremote.remote.quicksettings.RemoteTileService
import com.anyremote.remote.service.FavoriteManager
import com.anyremote.remote.service.LocaleManager
import com.anyremote.remote.ui.helpers.rememberVibratingHapticFeedback
import com.anyremote.remote.update.UpdateState
import kotlinx.coroutines.launch

private val languages =
    listOf(
        "en" to "English",
        "iw" to "עברית",
        "ar" to "العربية",
        "es" to "Español",
        "fr" to "Français",
        "de" to "Deutsch",
        "it" to "Italiano",
        "pt" to "Português",
        "ru" to "Русский",
        "zh" to "中文",
        "ja" to "日本語",
    )

private val themeModes =
    listOf(
        "system" to R.string.theme_system,
        "light" to R.string.theme_light,
        "dark" to R.string.theme_dark,
    )

private fun themeIcon(mode: String): ImageVector =
    when (mode) {
        "light" -> Icons.Outlined.LightMode
        "dark" -> Icons.Outlined.DarkMode
        else -> Icons.Outlined.BrightnessAuto
    }

// Country flag emoji for each language. Android draws them as color flags.
private val languageFlags =
    mapOf(
        "en" to "🇺🇸",
        "iw" to "🇮🇱",
        "ar" to "🇸🇦",
        "es" to "🇪🇸",
        "fr" to "🇫🇷",
        "de" to "🇩🇪",
        "it" to "🇮🇹",
        "pt" to "🇧🇷",
        "ru" to "🇷🇺",
        "zh" to "🇨🇳",
        "ja" to "🇯🇵",
    )

/** The language's flag, or the globe icon for the system language. */
@Composable
private fun LanguageIcon(code: String) {
    val flag = languageFlags[code]
    if (flag == null) {
        Icon(Icons.Outlined.Language, contentDescription = null)
    } else {
        Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            Text(flag, fontSize = 20.sp, lineHeight = 24.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val haptic = rememberVibratingHapticFeedback()
    val appName = stringResource(R.string.app_name)

    val themeMode by FavoriteManager.currentThemeState
    val dynamicColor by FavoriteManager.currentDynamicColorState
    val vibration by FavoriteManager.currentVibrationState
    val langCode by LocaleManager.currentLanguage

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showQuickSettingsInstructions by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val installedVersion = UpdateState.installedVersion
    val update = UpdateState.release
    val updateSummary =
        when (UpdateState.status) {
            UpdateState.Status.Checking -> stringResource(R.string.update_checking)
            UpdateState.Status.Available ->
                stringResource(R.string.update_available, update?.version.orEmpty())
            UpdateState.Status.UpToDate -> stringResource(R.string.update_latest, installedVersion)
            UpdateState.Status.Failed -> stringResource(R.string.update_check_failed)
            UpdateState.Status.Idle -> stringResource(R.string.update_version, installedVersion)
        }

    fun checkForUpdates() {
        // Builds that can't install (debug) check again on every tap instead.
        if (
            UpdateState.canInstall &&
                UpdateState.status == UpdateState.Status.Available &&
                update != null
        ) {
            UpdateState.showPrompt()
        } else {
            scope.launch { UpdateState.check() }
        }
    }

    fun addQuickSettingsTile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val statusBar = context.getSystemService(StatusBarManager::class.java)
            if (statusBar != null) {
                statusBar.requestAddTileService(
                    ComponentName(context, RemoteTileService::class.java),
                    appName,
                    AndroidIcon.createWithResource(context, R.drawable.ic_quick_settings_remote),
                    context.mainExecutor,
                ) { result ->
                    when (result) {
                        StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED,
                        StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED -> Unit
                        StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED ->
                            Toast.makeText(
                                    context,
                                    R.string.quick_settings_added,
                                    Toast.LENGTH_SHORT,
                                )
                                .show()
                        else -> showQuickSettingsInstructions = true
                    }
                }
                return
            }
        }
        showQuickSettingsInstructions = true
    }

    val systemLabel = stringResource(R.string.theme_system)
    val themeLabel =
        stringResource(
            themeModes.firstOrNull { it.first == themeMode }?.second ?: R.string.theme_system
        )
    val languageLabel = languages.firstOrNull { it.first == langCode }?.second ?: systemLabel

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SettingsGroup(stringResource(R.string.appearance)) {
                SettingsItem(
                    icon = themeIcon(themeMode),
                    title = stringResource(R.string.dark_light_mode),
                    summary = themeLabel,
                    onClick = { showThemeDialog = true },
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SettingsSwitchItem(
                        icon = Icons.Outlined.Palette,
                        title = stringResource(R.string.dynamic_color),
                        summary = stringResource(R.string.dynamic_color_subtitle),
                        checked = dynamicColor,
                        onCheckedChange = { FavoriteManager.setDynamicColor(context, it) },
                    )
                }
            }

            SettingsGroup(stringResource(R.string.general)) {
                SettingsItem(
                    icon = Icons.Outlined.DashboardCustomize,
                    title = stringResource(R.string.quick_settings_add),
                    summary = stringResource(R.string.quick_settings_summary),
                    onClick = ::addQuickSettingsTile,
                )
                SettingsItem(
                    leading = { LanguageIcon(langCode) },
                    title = stringResource(R.string.language_selection),
                    summary = languageLabel,
                    onClick = { showLanguageDialog = true },
                )
                SettingsSwitchItem(
                    icon = Icons.Outlined.Vibration,
                    title = stringResource(R.string.vibration_toggle),
                    summary = stringResource(R.string.vibration_subtitle),
                    checked = vibration,
                    onCheckedChange = { enabled ->
                        FavoriteManager.setVibrationEnabled(context, enabled)
                        if (enabled) haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                    },
                )
                if (UpdateState.enabled) {
                    SettingsItem(
                        icon = Icons.Outlined.SystemUpdate,
                        title = stringResource(R.string.update_check),
                        summary = updateSummary,
                        onClick = ::checkForUpdates,
                    )
                } else {
                    SettingsItem(
                        icon = Icons.Outlined.Info,
                        title = stringResource(R.string.update_version, installedVersion),
                        summary = stringResource(R.string.update_fdroid),
                        onClick = {},
                    )
                }
            }
        }
    }

    if (showQuickSettingsInstructions) {
        AlertDialog(
            onDismissRequest = { showQuickSettingsInstructions = false },
            title = { Text(stringResource(R.string.quick_settings_add)) },
            text = { Text(stringResource(R.string.quick_settings_manual, appName)) },
            confirmButton = {
                TextButton(onClick = { showQuickSettingsInstructions = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }

    if (showThemeDialog) {
        ChoiceDialog(
            title = stringResource(R.string.select_theme),
            options = themeModes.map { it.first to stringResource(it.second) },
            selected = themeMode,
            optionIcon = { Icon(themeIcon(it), contentDescription = null) },
            onSelect = {
                FavoriteManager.setThemeMode(context, it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false },
        )
    }

    if (showLanguageDialog) {
        ChoiceDialog(
            title = stringResource(R.string.select_language),
            options = listOf("system" to systemLabel) + languages,
            selected = langCode,
            optionIcon = { LanguageIcon(it) },
            onSelect = {
                showLanguageDialog = false
                if (it != langCode) LocaleManager.setLanguage(context, it)
            },
            onDismiss = { showLanguageDialog = false },
        )
    }
}

/** Android 16-style settings group: a caption over a rounded tonal container. */
@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
    )
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    summary: String,
    onClick: () -> Unit,
) = SettingsItem({ Icon(icon, contentDescription = null) }, title, summary, onClick)

@Composable
private fun SettingsItem(
    leading: @Composable () -> Unit,
    title: String,
    summary: String,
    onClick: () -> Unit,
) {
    ListItem(
        onClick = onClick,
        supportingContent = { Text(summary) },
        leadingContent = leading,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    ) {
        Text(title)
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    // The whole row toggles; the switch itself is just the indicator.
    ListItem(
        checked = checked,
        onCheckedChange = onCheckedChange,
        supportingContent = { Text(summary) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    ) {
        Text(title)
    }
}

@Composable
private fun ChoiceDialog(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    optionIcon: @Composable (String) -> Unit,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(modifier = Modifier.selectableGroup().heightIn(max = 420.dp)) {
                items(options) { (value, label) ->
                    Row(
                        modifier =
                            Modifier.fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .selectable(
                                    selected = value == selected,
                                    onClick = { onSelect(value) },
                                    role = Role.RadioButton,
                                )
                                .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = value == selected, onClick = null)
                        Spacer(Modifier.width(16.dp))
                        optionIcon(value)
                        Spacer(Modifier.width(12.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
