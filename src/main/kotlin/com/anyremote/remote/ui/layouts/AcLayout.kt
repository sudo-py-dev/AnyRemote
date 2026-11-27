package com.anyremote.remote.ui.layouts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.unit.dp
import com.anyremote.remote.R
import com.anyremote.remote.data.RemoteDatabase
import com.anyremote.remote.ir.AcProtocol
import com.anyremote.remote.ir.AcState
import com.anyremote.remote.ui.components.*
import kotlin.math.cos
import kotlin.math.sin

/** On/off AC features. [key] matches the option names in remote.pb's acOptions. */
private enum class AcToggle(
    val key: String,
    val icon: ImageVector,
    val label: Int,
    val isOn: (AcState) -> Boolean,
    val toggle: (AcState) -> AcState,
) {
    TURBO("turbo", Icons.Default.FlashOn, R.string.ac_label_turbo, { it.turbo }, { it.copy(turbo = !it.turbo) }),
    ECO("eco", Icons.Default.Eco, R.string.ac_label_eco, { it.eco }, { it.copy(eco = !it.eco) }),
    LIGHT("light", Icons.Default.Lightbulb, R.string.ac_label_light, { it.light }, { it.copy(light = !it.light) }),
    SWING_H("swing_h", Icons.Default.SwapHoriz, R.string.ac_label_swing_h, { it.swingH }, { it.copy(swingH = !it.swingH) }),
    TEMP_DISPLAY("temp_display", Icons.Default.DeviceThermostat, R.string.ac_label_temp_display,
        { it.tempDisplay }, { it.copy(tempDisplay = !it.tempDisplay) }),
}


private val modes = listOf("MC", "MH", "MD", "MV", "MA")
private val speeds = listOf("SA", "SL", "SM", "SH")

private fun modeIcon(mode: String): ImageVector =
    when (mode) {
        "MC" -> Icons.Default.AcUnit
        "MH" -> Icons.Default.WbSunny
        "MD" -> Icons.Default.WaterDrop
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
    protocol: AcProtocol = AcProtocol.GREE,
) {
    val power = state.power
    val maxTemp = protocol.maxTemp
    val minTemp = protocol.minTemp
    val electra = protocol == AcProtocol.ELECTRA
    val options = if (electra) listOf(AcToggle.TURBO, AcToggle.SWING_H) else AcToggle.entries.filter { RemoteDatabase.hasAcOption(it.key) }
    val hasSweep = electra || RemoteDatabase.hasAcOption("wind_model") || RemoteDatabase.hasAcOption("swing")

    fun setTemp(t: Int) {
        val clamped = t.coerceIn(minTemp, maxTemp)
        if (clamped != state.temp) onChange(state.copy(temp = clamped))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ThermostatDial(state, minTemp, maxTemp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconKey(
                Icons.Default.Remove,
                stringResource(R.string.cd_temp_down),
                { setTemp(state.temp - 1) },
                size = 64.dp,
                enabled = power && state.temp > minTemp,
                repeatOnHold = true,
            )
            PowerKey(
                onClick = { onChange(state.copy(power = !power)) },
                active = power,
                size = 80.dp,
            )
            IconKey(
                Icons.Default.Add,
                stringResource(R.string.cd_temp_up),
                { setTemp(state.temp + 1) },
                size = 64.dp,
                enabled = power && state.temp < maxTemp,
                repeatOnHold = true,
            )
        }

        RemoteSection(title = stringResource(R.string.ac_label_mode)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                modes.forEach { mode ->
                    OptionKey(
                        icon = modeIcon(mode),
                        label = stringResource(modeLabel(mode)),
                        selected = power && state.mode == mode,
                        enabled = power,
                        onClick = { onChange(state.copy(mode = mode)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        RemoteSection(title = stringResource(R.string.ac_label_speed)) {
            SegmentedSelector(
                options = speeds.map { it to stringResource(speedLabel(it)) },
                selected = state.speed.takeIf { power },
                onSelect = { onChange(state.copy(speed = it)) },
                enabled = power,
            )
        }

        if (protocol != AcProtocol.AMCOR) RemoteSection(title = stringResource(R.string.airflow)) {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                maxItemsInEachRow = 4,
            ) {
                if (hasSweep) {
                    OptionKey(
                        icon = Icons.AutoMirrored.Filled.CompareArrows,
                        label = stringResource(R.string.ac_label_sweep),
                        selected = power && state.swing,
                        enabled = power,
                        onClick = { onChange(state.copy(swing = !state.swing)) },
                    )
                }
                val fixedVane = !state.swing && state.swingPosition > 0
                if (!electra) OptionKey(
                    icon = Icons.Default.SyncAlt,
                    label =
                        stringResource(R.string.ac_label_direction) +
                            if (fixedVane) " ${state.swingPosition}" else "",
                    selected = power && fixedVane,
                    enabled = power,
                    // Step the vane through fixed positions 1..5; this turns auto-sweep off.
                    onClick = { onChange(state.copy(swing = false, swingPosition = state.swingPosition % 5 + 1)) },
                )
                options.forEach { option ->
                    OptionKey(
                        icon = option.icon,
                        label = stringResource(option.label),
                        selected = power && option.isOn(state),
                        enabled = power,
                        onClick = { onChange(option.toggle(state)) },
                    )
                }
            }
        }
    }
}

/** Google Home–style dial: a 270° track whose arc fills with the set temperature. */
@Composable
private fun ThermostatDial(state: AcState, minTemp: Int, maxTemp: Int) {
    val scheme = MaterialTheme.colorScheme
    val power = state.power
    val fraction by
        animateFloatAsState(
            (state.temp - minTemp).toFloat() / (maxTemp - minTemp),
            animationSpec = tween(180),
            label = "dialFraction",
        )
    val trackColor = scheme.surfaceContainerHighest
    val arcColor = if (power) scheme.primary else scheme.outlineVariant
    val modeName = stringResource(modeLabel(state.mode))
    val speedName = stringResource(speedLabel(state.speed))
    val offLabel = stringResource(R.string.off_word)
    val spoken = if (power) "${state.temp} °C, $modeName, $speedName" else offLabel

    Box(
        modifier =
            Modifier.widthIn(max = 208.dp).fillMaxWidth().aspectRatio(1f).clearAndSetSemantics {
                contentDescription = spoken
                liveRegion = LiveRegionMode.Polite
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val stroke = 14.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            drawArc(trackColor, 135f, 270f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            val sweep = 270f * fraction
            drawArc(arcColor, 135f, sweep, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            // Thumb at the end of the arc.
            val angle = Math.toRadians((135f + sweep).toDouble())
            val r = arcSize.width / 2
            val center = Offset(size.width / 2, size.height / 2)
            val thumb = Offset(center.x + r * cos(angle).toFloat(), center.y + r * sin(angle).toFloat())
            drawCircle(scheme.surface, radius = stroke * 0.7f, center = thumb)
            drawCircle(arcColor, radius = stroke * 0.45f, center = thumb)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modeIcon(state.mode),
                    contentDescription = null,
                    tint = if (power) scheme.primary else scheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (power) modeName else offLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            Text(
                "${state.temp}°",
                style = MaterialTheme.typography.displayLarge,
                color = if (power) scheme.onSurface else scheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
            Text(
                if (power) speedName else "°C",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}
