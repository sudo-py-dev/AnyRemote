package com.anyremote.remote.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anyremote.remote.R
import com.anyremote.remote.ir.RemoteKey
import com.anyremote.remote.ui.helpers.rememberVibratingHapticFeedback
import com.anyremote.remote.ui.theme.CrimsonRed
import com.anyremote.remote.ui.theme.HighlightBlue

@Composable
fun keyOutline(): Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

/** Base for every remote button: shared surface, outline, highlight and haptic tap. */
@Composable
fun RemoteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    highlighted: Boolean = false,
    borderColor: Color? = null,
    background: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val haptic = rememberVibratingHapticFeedback()
    Box(
        modifier =
            modifier
                .clip(shape)
                .background(
                    background
                        ?: if (highlighted) HighlightBlue.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surface)
                .border(1.5.dp, borderColor ?: if (highlighted) HighlightBlue else keyOutline(), shape)
                .clickable(enabled = enabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
fun RoundIconKey(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    iconSize: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color? = null,
    enabled: Boolean = true,
) {
    RemoteButton(
        onClick = onClick,
        modifier = modifier.size(size),
        enabled = enabled,
        borderColor = borderColor,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}

/** Big round power key; red outline and icon while [active]. */
@Composable
fun PowerKey(
    onClick: () -> Unit,
    active: Boolean = true,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
) {
    RoundIconKey(
        icon = Icons.Default.PowerSettingsNew,
        contentDescription = stringResource(R.string.cd_power),
        onClick = onClick,
        modifier = modifier,
        size = size,
        iconSize = 28.dp,
        tint = if (active) CrimsonRed else MaterialTheme.colorScheme.onSurface,
        borderColor = if (active) CrimsonRed else null,
    )
}

/** Rounded-rectangle key with a text label. */
@Composable
fun TextKey(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    highlighted: Boolean = false,
    textColor: Color? = null,
    height: Dp = 60.dp,
    fontSize: Int = 14,
) {
    RemoteButton(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled,
        highlighted = highlighted,
    ) {
        Text(
            text,
            color = textColor ?: if (highlighted) HighlightBlue else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
        )
    }
}

/** Status panel at the top of fan/audio remotes. */
@Composable
fun StatusBanner(
    title: String,
    subtitle: String?,
    active: Boolean,
) {
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, keyOutline(), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = if (active) HighlightBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

/** Row of mutually exclusive options (audio source, camera delay). */
@Composable
fun <T> SegmentedSelector(
    options: List<Pair<T, String>>,
    selected: T?,
    onSelect: (T) -> Unit,
    enabled: Boolean = true,
) {
    val haptic = rememberVibratingHapticFeedback()
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, keyOutline(), RoundedCornerShape(12.dp))
                .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        options.forEach { (value, label) ->
            val isActive = value == selected
            Box(
                modifier =
                    Modifier.weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isActive) HighlightBlue.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable(enabled = enabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelect(value)
                        }
                        .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (isActive) HighlightBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

private data class DPadArrow(val key: RemoteKey, val icon: ImageVector, val alignment: Alignment, val description: Int)

/** Circular D-pad with OK in the middle. */
@Composable
fun DPad(
    onKey: (RemoteKey) -> Unit,
    size: Dp = 200.dp,
    centerSize: Dp = 76.dp,
    showDividers: Boolean = true,
) {
    val haptic = rememberVibratingHapticFeedback()
    val outline = keyOutline()
    Box(
        modifier =
            Modifier.size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, outline, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (showDividers) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(outline, Offset(0f, 0f), Offset(this.size.width, this.size.height), 1.5f)
                drawLine(outline, Offset(0f, this.size.height), Offset(this.size.width, 0f), 1.5f)
            }
        }

        RemoteButton(
            onClick = { onKey(RemoteKey.OK) },
            modifier = Modifier.size(centerSize),
            background = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        ) {
            Text(
                stringResource(R.string.ok),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }

        listOf(
                DPadArrow(RemoteKey.UP, Icons.Default.KeyboardArrowUp, Alignment.TopCenter, R.string.cd_up),
                DPadArrow(RemoteKey.DOWN, Icons.Default.KeyboardArrowDown, Alignment.BottomCenter, R.string.cd_down),
                DPadArrow(RemoteKey.LEFT, Icons.AutoMirrored.Filled.KeyboardArrowLeft, Alignment.CenterStart, R.string.cd_left),
                DPadArrow(RemoteKey.RIGHT, Icons.AutoMirrored.Filled.KeyboardArrowRight, Alignment.CenterEnd, R.string.cd_right),
            )
            .forEach { (key, icon, alignment, description) ->
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKey(key)
                    },
                    modifier = Modifier.align(alignment).padding(8.dp).size(54.dp),
                ) {
                    Icon(
                        icon,
                        contentDescription = stringResource(description),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
    }
}

/** Vertical up/down pill such as VOL or CH. */
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
) {
    val haptic = rememberVibratingHapticFeedback()
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, keyOutline(), RoundedCornerShape(24.dp))
                .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onUp()
            }) {
                Icon(upIcon, contentDescription = upDescription, tint = MaterialTheme.colorScheme.onSurface)
            }
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDown()
            }) {
                Icon(downIcon, contentDescription = downDescription, tint = MaterialTheme.colorScheme.onSurface)
            }
    }
}
