package com.anyremote.remote.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anyremote.remote.R
import com.anyremote.remote.ir.RemoteKey
import com.anyremote.remote.ui.helpers.rememberVibratingHapticFeedback
import kotlinx.coroutines.delay

/** Sizes shared by every remote layout, so keys line up across device types. */
object RemoteDefaults {
    val KeySize = 56.dp
    val LargeKeySize = 72.dp
    val IconSize = 24.dp
    val ContentMaxWidth = 440.dp

    /** Hold this long before a repeating key starts auto-firing, then fire at this interval. */
    const val REPEAT_DELAY_MS = 400L
    const val REPEAT_INTERVAL_MS = 110L
}

/**
 * Click handling for a remote key: haptic tap, ripple, and optional auto-repeat while held
 * (volume, channel, temperature). A hold that already repeated doesn't fire again on release.
 */
@Composable
private fun Modifier.remoteKeyClick(
    onClick: () -> Unit,
    enabled: Boolean,
    repeatOnHold: Boolean,
    interactionSource: MutableInteractionSource,
    role: Role = Role.Button,
): Modifier {
    val haptic = rememberVibratingHapticFeedback()
    val currentOnClick by rememberUpdatedState(onClick)
    val pressed by interactionSource.collectIsPressedAsState()
    var repeated by remember { mutableStateOf(false) }

    LaunchedEffect(interactionSource, repeatOnHold, enabled) {
        repeated = false
        if (repeatOnHold && enabled) {
            interactionSource.interactions.collect { interaction ->
                if (interaction is PressInteraction.Cancel) repeated = false
            }
        }
    }

    if (repeatOnHold && enabled) {
        LaunchedEffect(pressed) {
            if (pressed) {
                repeated = false
                delay(RemoteDefaults.REPEAT_DELAY_MS)
                while (true) {
                    repeated = true
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    currentOnClick()
                    delay(RemoteDefaults.REPEAT_INTERVAL_MS)
                }
            }
        }
    }

    return clickable(
        interactionSource = interactionSource,
        indication = LocalIndication.current,
        enabled = enabled,
        role = role,
    ) {
        if (repeated) {
            repeated = false
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            currentOnClick()
        }
    }
}

/**
 * Base for every remote key: a borderless tonal surface. The key scales down slightly while pressed.
 *
 * [cornerPercent] is the resting corner radius as a percent of the shorter side (50 = circle).
 */
@Composable
fun RemoteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerPercent: Int = 50,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    repeatOnHold: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, tween(if (pressed) 70 else 140), label = "keyScale")
    val container by
        animateColorAsState(
            if (enabled) containerColor
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            animationSpec = tween(120),
            label = "keyContainer",
        )
    val shape = RoundedCornerShape(cornerPercent)

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                 }
                .clip(shape)
                .background(container)
                .remoteKeyClick(onClick, enabled, repeatOnHold, interactionSource),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides
                if (enabled) contentColor else contentColor.copy(alpha = 0.38f),
        ) {
            content()
        }
    }
}

@Composable
fun IconKey(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = RemoteDefaults.KeySize,
    iconSize: Dp = RemoteDefaults.IconSize,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    repeatOnHold: Boolean = false,
) {
    RemoteButton(
        onClick = onClick,
        modifier = modifier.size(size),
        enabled = enabled,
        containerColor = containerColor,
        contentColor = contentColor,
        repeatOnHold = repeatOnHold,
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(iconSize))
    }
}

/**
 * Power key. [active] means the device is known to be on (stateful remotes like the AC) or,
 * for stateless remotes, simply that power is the key to reach for: it gets the error tone.
 */
@Composable
fun PowerKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = true,
    size: Dp = RemoteDefaults.KeySize,
) {
    val scheme = MaterialTheme.colorScheme
    IconKey(
        icon = Icons.Default.PowerSettingsNew,
        contentDescription = stringResource(R.string.cd_power),
        onClick = onClick,
        modifier = modifier,
        size = size,
        iconSize = if (size >= RemoteDefaults.LargeKeySize) 32.dp else RemoteDefaults.IconSize,
        containerColor = if (active) scheme.errorContainer else scheme.surfaceContainerHighest,
        contentColor = if (active) scheme.onErrorContainer else scheme.onSurfaceVariant,
    )
}

/** Pill key with a text label. [selected] fills it with the primary tone. */
@Composable
fun TextKey(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    height: Dp = RemoteDefaults.KeySize,
    repeatOnHold: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    RemoteButton(
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled,
        containerColor = if (selected) scheme.primary else scheme.surfaceContainerHighest,
        contentColor = if (selected) scheme.onPrimary else scheme.onSurface,
        repeatOnHold = repeatOnHold,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}

/**
 * Round toggle with a caption underneath, like the quick tiles in Google Home. Used for AC
 * modes and features and the fan's oscillate/mode keys.
 */
@Composable
fun OptionKey(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier.width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        RemoteButton(
            onClick = onClick,
            modifier = Modifier.size(RemoteDefaults.KeySize),
            enabled = enabled,
            containerColor = if (selected) scheme.primary else scheme.surfaceContainerHighest,
            contentColor = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
        ) {
            // The caption below carries the name; the icon is decorative.
            Icon(icon, contentDescription = null, modifier = Modifier.size(RemoteDefaults.IconSize))
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color =
                when {
                    !enabled -> scheme.onSurface.copy(alpha = 0.38f)
                    selected -> scheme.primary
                    else -> scheme.onSurfaceVariant
                },
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Large rounded panel showing what the remote last told the device (fan speed, volume…). */
@Composable
fun StatusCard(
    title: String,
    subtitle: String?,
    active: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = if (active) scheme.primaryContainer else scheme.surfaceContainer,
        contentColor = if (active) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Column {
                Text(title, style = MaterialTheme.typography.headlineSmall)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

/** Titled group of controls on a tonal card. */
@Composable
fun RemoteSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}

/** Mutually exclusive options (audio source, camera delay, AC fan speed). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SegmentedSelector(
    options: List<Pair<T, String>>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val haptic = rememberVibratingHapticFeedback()
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = value == selected,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    onSelect(value)
                },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                enabled = enabled,
                label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            )
        }
    }
}

private data class DPadArrow(
    val key: RemoteKey,
    val icon: ImageVector,
    val alignment: Alignment,
    val description: Int,
)

/**
 * Circular D-pad in the style of the Google TV remote: a tonal ring with four arrow zones and
 * a primary OK key in the middle. Arrows repeat while held so long lists scroll.
 */
@Composable
fun DPad(
    onKey: (RemoteKey) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 248.dp,
) {
    val scheme = MaterialTheme.colorScheme
    val zone = size * 0.3f
    Box(
        modifier =
            modifier.size(size).clip(CircleShape).background(scheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        listOf(
                DPadArrow(RemoteKey.UP, Icons.Default.KeyboardArrowUp, Alignment.TopCenter, R.string.cd_up),
                DPadArrow(RemoteKey.DOWN, Icons.Default.KeyboardArrowDown, Alignment.BottomCenter, R.string.cd_down),
                DPadArrow(RemoteKey.LEFT, Icons.Default.KeyboardArrowLeft, AbsoluteAlignment.CenterLeft, R.string.cd_left),
                DPadArrow(RemoteKey.RIGHT, Icons.Default.KeyboardArrowRight, AbsoluteAlignment.CenterRight, R.string.cd_right),
            )
            .forEach { (key, icon, alignment, description) ->
                RemoteButton(
                    onClick = { onKey(key) },
                    modifier = Modifier.align(alignment).padding(6.dp).size(zone),
                    containerColor = Color.Transparent,
                    contentColor = scheme.onSurfaceVariant,
                    repeatOnHold = true,
                ) {
                    Icon(icon, contentDescription = stringResource(description), modifier = Modifier.size(32.dp))
                }
            }

        RemoteButton(
            onClick = { onKey(RemoteKey.OK) },
            modifier = Modifier.size(size * 0.38f),
            containerColor = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
        ) {
            Text(stringResource(R.string.ok), style = MaterialTheme.typography.titleLarge)
        }
    }
}

/** Vertical up/down pill such as volume or channel; both halves repeat while held. */
@Composable
fun RockerKey(
    label: String,
    upIcon: ImageVector,
    downIcon: ImageVector,
    upDescription: String,
    downDescription: String,
    onUp: () -> Unit,
    onDown: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier =
            modifier
                .width(RemoteDefaults.LargeKeySize)
                .height(176.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    if (enabled) scheme.surfaceContainerHighest
                    else scheme.onSurface.copy(alpha = 0.08f)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RockerHalf(upIcon, upDescription, onUp, enabled, Modifier.weight(1f))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.38f),
        )
        RockerHalf(downIcon, downDescription, onDown, enabled, Modifier.weight(1f))
    }
}

@Composable
private fun RockerHalf(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .remoteKeyClick(onClick, enabled, repeatOnHold = true, interactionSource = interactionSource),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.38f),
        )
    }
}
