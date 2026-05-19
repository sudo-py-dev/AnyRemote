package com.anyremote.remote.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anyremote.remote.R
import com.anyremote.remote.service.FavoriteManager
import com.anyremote.remote.service.LocaleManager
import com.anyremote.remote.ui.components.*
import com.anyremote.remote.ui.helpers.*
import com.anyremote.remote.ui.layouts.*
import com.anyremote.remote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current

    // Reactive States from FavoriteManager / LocaleManager
    val activeThemeMode by FavoriteManager.currentThemeState
    val isVibrationEnabled by FavoriteManager.currentVibrationState
    val activeLangCode by LocaleManager.currentLanguage

    val languages =
        listOf(
            "system" to "System Default",
            "en" to "English",
            "iw" to "עברית (Hebrew)",
            "es" to "Español (Spanish)",
            "fr" to "Français (French)",
            "de" to "Deutsch (German)",
            "it" to "Italiano (Italian)",
            "ru" to "Русский (Russian)",
            "ar" to "العربية (Arabic)",
            "zh" to "中文 (Chinese)",
            "ja" to "日本語 (Japanese)",
            "pt" to "Português (Portuguese)",
        )

    val themeModes =
        listOf(
            "light" to R.string.theme_light,
            "dark" to R.string.theme_dark,
            "system" to R.string.theme_system,
        )

    var showLanguageDropdown by remember { mutableStateOf(false) }
    var showThemeDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
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
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Dark / Light Mode Setting Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier =
                                Modifier.size(36.dp)
                                    .background(
                                        ElectricBlue.copy(
                                            alpha = if (isAppDarkTheme()) 0.2f else 0.1f),
                                        CircleShape,
                                    ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.dark_light_mode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = stringResource(R.string.theme_subtitle),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Segmented Button or Dropdown selection for Theme
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showThemeDropdown = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = getAppButtonBackground(),
                                ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            val activeThemeLabelKey =
                                themeModes.firstOrNull { it.first == activeThemeMode }?.second
                                    ?: R.string.theme_light
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(activeThemeLabelKey),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showThemeDropdown,
                            onDismissRequest = { showThemeDropdown = false },
                            modifier =
                                Modifier.fillMaxWidth(0.9f)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)),
                        ) {
                            themeModes.forEach { (mode, labelKey) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(labelKey),
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontWeight =
                                                if (activeThemeMode == mode) FontWeight.Bold
                                                else FontWeight.Normal,
                                        )
                                    },
                                    onClick = {
                                        FavoriteManager.setThemeMode(context, mode)
                                        showThemeDropdown = false
                                    },
                                    leadingIcon = {
                                        if (activeThemeMode == mode) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = ElectricBlue)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // 2. Language Selection Setting Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier =
                                Modifier.size(36.dp)
                                    .background(
                                        AccentGreen.copy(
                                            alpha = if (isAppDarkTheme()) 0.2f else 0.1f),
                                        CircleShape,
                                    ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.language_selection),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = stringResource(R.string.language_subtitle),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showLanguageDropdown = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = getAppButtonBackground(),
                                ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            val activeLangLabel =
                                if (activeLangCode == "system") {
                                    stringResource(R.string.theme_system)
                                } else {
                                    languages.firstOrNull { it.first == activeLangCode }?.second
                                        ?: stringResource(R.string.theme_system)
                                }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = activeLangLabel,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showLanguageDropdown,
                            onDismissRequest = { showLanguageDropdown = false },
                            modifier =
                                Modifier.fillMaxWidth(0.9f)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp))
                                    .heightIn(max = 280.dp),
                        ) {
                            languages.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text =
                                                if (code == "system")
                                                    stringResource(R.string.theme_system)
                                                else label,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontWeight =
                                                if (activeLangCode == code) FontWeight.Bold
                                                else FontWeight.Normal,
                                        )
                                    },
                                    onClick = {
                                        LocaleManager.setLanguage(context, code)
                                        showLanguageDropdown = false
                                    },
                                    leadingIcon = {
                                        if (activeLangCode == code) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = AccentGreen)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // 3. Vibration Haptics Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier.size(36.dp)
                                .background(
                                    WarmOrange.copy(alpha = if (isAppDarkTheme()) 0.2f else 0.1f),
                                    CircleShape,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = WarmOrange,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.vibration_toggle),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = stringResource(R.string.vibration_subtitle),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        )
                    }

                    Switch(
                        checked = isVibrationEnabled,
                        onCheckedChange = { enabled ->
                            FavoriteManager.setVibrationEnabled(context, enabled)
                        },
                        colors =
                            SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = WarmOrange,
                                uncheckedThumbColor =
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                uncheckedTrackColor = getAppButtonBackground(),
                            ),
                    )
                }
            }
        }
    }
}
