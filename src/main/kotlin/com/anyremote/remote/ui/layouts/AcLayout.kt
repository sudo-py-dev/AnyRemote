package com.anyremote.remote.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anyremote.remote.R
import com.anyremote.remote.data.RemoteDatabase
import com.anyremote.remote.ir.AcState
import com.anyremote.remote.ui.helpers.rememberVibratingHapticFeedback
import com.anyremote.remote.ui.theme.CrimsonRed
import com.anyremote.remote.ui.theme.ElectricBlue

/** On/off AC features. [key] matches the option names in remote.pb's acOptions. */
private enum class AcToggle(
    val key: String,
    val icon: ImageVector,
    val label: Int,
    val labelOn: Int?,
    val isOn: (AcState) -> Boolean,
    val toggle: (AcState) -> AcState,
) {
    TURBO("turbo", Icons.Default.FlashOn, R.string.ac_label_turbo, R.string.ac_label_turbo_on,
        { it.turbo }, { it.copy(turbo = !it.turbo) }),
    LIGHT("light", Icons.Default.Lightbulb, R.string.ac_label_light, R.string.ac_label_light_on,
        { it.light }, { it.copy(light = !it.light) }),
    AUX_HEAT("aux_heat", Icons.Default.Whatshot, R.string.ac_label_aux_heat, null,
        { it.auxHeat }, { it.copy(auxHeat = !it.auxHeat) }),
    HEALTH("health", Icons.Default.Favorite, R.string.ac_label_health, null,
        { it.health }, { it.copy(health = !it.health) }),
    VENTILATION("ventilation", Icons.Default.Loop, R.string.ac_label_ventilation, null,
        { it.ventilation }, { it.copy(ventilation = !it.ventilation) }),
    ECO("eco", Icons.Default.Eco, R.string.ac_label_eco, null,
        { it.eco }, { it.copy(eco = !it.eco) }),
    SWING_H("swing_h", Icons.Default.SwapHoriz, R.string.ac_label_swing_h, null,
        { it.swingH }, { it.copy(swingH = !it.swingH) }),
    MUTE("mute", Icons.AutoMirrored.Filled.VolumeOff, R.string.ac_label_mute, null,
        { it.mute }, { it.copy(mute = !it.mute) }),
    TEMP_DISPLAY("temp_display", Icons.Default.DeviceThermostat, R.string.ac_label_temp_display, null,
        { it.tempDisplay }, { it.copy(tempDisplay = !it.tempDisplay) }),
}

private val modeCycle = listOf("MC", "MH", "MD", "MV", "MA")
private val speedCycle = listOf("SA", "SL", "SM", "SH")

/** Next item after [item], wrapping; unknown values restart at the first item. */
private fun <T> List<T>.after(item: T): T = this[(indexOf(item) + 1) % size]

private fun modeIcon(mode: String): ImageVector =
    when (mode) {
        "MC" -> Icons.Default.AcUnit
        "MH" -> Icons.Default.WbSunny
        "MD" -> Icons.Default.Opacity
        "MV" -> Icons.Default.Air
        else -> Icons.Default.Autorenew
    }

private fun modeLabel(mode: String): Int =
    when (mode) {
        "MC" -> R.string.ac_mode_cool
        "MH" -> R.string.ac_mode_heat
        "MD" -> R.string.ac_mode_dry
        "MV" -> R.string.ac_mode_fan
        else -> R.string.ac_mode_auto
    }

private fun speedLabel(speed: String): Int =
    when (speed) {
        "SA" -> R.string.ac_wind_auto
        "SL" -> R.string.ac_wind_low
        "SM" -> R.string.ac_wind_medium
        else -> R.string.ac_wind_high
    }

@Composable
fun AcLayout(
    state: AcState,
    onChange: (AcState) -> Unit,
) {
    val power = state.power
    val onSurface = MaterialTheme.colorScheme.onSurface
    val modeName = stringResource(modeLabel(state.mode))
    val speedName = stringResource(speedLabel(state.speed))
    val options = AcToggle.entries.filter { RemoteDatabase.hasAcOption(it.key) }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "${state.temp}",
                    color = if (power) onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                )
                Text(
                    text = "°C",
                    color = onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 18.dp, start = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier =
                    Modifier.clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusChip(modeIcon(state.mode), modeName)
                StatusChip(Icons.Default.Air, speedName)
                if (state.swing &&
                    (RemoteDatabase.hasAcOption("wind_model") || RemoteDatabase.hasAcOption("swing"))) {
                    StatusChip(Icons.AutoMirrored.Filled.CompareArrows, stringResource(R.string.swing))
                }
                if (power) {
                    options.filter { it.isOn(state) }.forEach { StatusChip(it.icon, stringResource(it.label)) }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                AcRow {
                    AcCell(
                        icon = Icons.Default.PowerSettingsNew,
                        label = stringResource(R.string.ac_label_power),
                        color = if (power) CrimsonRed else onSurface,
                        iconSize = 28.dp,
                        fontSize = 11,
                    ) {
                        onChange(state.copy(power = !power))
                    }
                    VDivider()
                    AcCell(
                        icon = modeIcon(state.mode),
                        label = "${stringResource(R.string.ac_label_mode)} ($modeName)",
                        enabled = power,
                        iconSize = 28.dp,
                        fontSize = 11,
                    ) {
                        onChange(state.copy(mode = modeCycle.after(state.mode)))
                    }
                }

                HDivider()
                AcRow {
                    AcCell(
                        icon = Icons.Default.Remove,
                        contentDescription = "Temp Down",
                        enabled = power,
                        iconSize = 32.dp,
                    ) {
                        val t = (state.temp - 1).coerceIn(16, 30)
                        if (t != state.temp) onChange(state.copy(temp = t))
                    }
                    VDivider()
                    AcCell(
                        label = stringResource(R.string.ac_label_temperature),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11,
                    )
                    VDivider()
                    AcCell(
                        icon = Icons.Default.Add,
                        contentDescription = "Temp Up",
                        enabled = power,
                        iconSize = 32.dp,
                    ) {
                        val t = (state.temp + 1).coerceIn(16, 30)
                        if (t != state.temp) onChange(state.copy(temp = t))
                    }
                }

                HDivider()
                AcRow {
                    AcCell(
                        icon = Icons.Default.Air,
                        label = "${stringResource(R.string.ac_label_speed)} ($speedName)",
                        enabled = power,
                    ) {
                        onChange(state.copy(speed = speedCycle.after(state.speed)))
                    }
                    VDivider()
                    val fixedVane = !state.swing && state.swingPosition > 0
                    AcCell(
                        icon = Icons.Default.SyncAlt,
                        label =
                            stringResource(R.string.ac_label_direction) +
                                if (fixedVane) " (${state.swingPosition})" else "",
                        enabled = power,
                        color = if (fixedVane && power) ElectricBlue else onSurface,
                    ) {
                        // Step the vane through fixed positions 1..5; this turns auto-sweep off.
                        onChange(state.copy(swing = false, swingPosition = state.swingPosition % 5 + 1))
                    }
                    VDivider()
                    val sweeping = state.swing && power
                    AcCell(
                        icon = Icons.AutoMirrored.Filled.CompareArrows,
                        label =
                            stringResource(
                                if (sweeping) R.string.ac_label_sweep_on else R.string.ac_label_sweep),
                        enabled = power,
                        color = if (sweeping) ElectricBlue else onSurface,
                    ) {
                        onChange(state.copy(swing = !state.swing))
                    }
                }

                options.chunked(2).forEach { chunk ->
                    HDivider()
                    AcRow {
                        chunk.forEachIndexed { index, option ->
                            if (index > 0) VDivider()
                            val active = option.isOn(state) && power
                            AcCell(
                                icon = option.icon,
                                label = stringResource(if (active) option.labelOn ?: option.label else option.label),
                                enabled = power,
                                color =
                                    if (active) ElectricBlue
                                    else onSurface.copy(alpha = if (power) 1f else 0.4f),
                            ) {
                                onChange(option.toggle(state))
                            }
                        }
                        if (chunk.size == 1) {
                            VDivider()
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    icon: ImageVector,
    label: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AcRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun dividerColor() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

@Composable
private fun HDivider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.5.dp).background(dividerColor()))
}

@Composable
private fun VDivider() {
    Box(modifier = Modifier.width(1.5.dp).fillMaxHeight().background(dividerColor()))
}

/** One cell of the AC grid. Without [onClick] it is a plain label. */
@Composable
private fun RowScope.AcCell(
    icon: ImageVector? = null,
    label: String? = null,
    contentDescription: String? = null,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.onSurface,
    iconSize: Dp = 24.dp,
    fontSize: Int = 10,
    onClick: (() -> Unit)? = null,
) {
    val haptic = rememberVibratingHapticFeedback()
    val clickModifier =
        if (onClick == null) Modifier
        else
            Modifier.clickable(enabled = enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
    Box(
        modifier = Modifier.weight(1f).fillMaxHeight().then(clickModifier),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (icon != null) {
                Icon(icon, contentDescription = contentDescription, tint = color, modifier = Modifier.size(iconSize))
            }
            if (icon != null && label != null) Spacer(modifier = Modifier.height(4.dp))
            if (label != null) {
                Text(text = label, color = color, fontSize = fontSize.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
