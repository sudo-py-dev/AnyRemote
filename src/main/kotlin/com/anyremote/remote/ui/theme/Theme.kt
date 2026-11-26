package com.anyremote.remote.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/*
 * Material 3 tonal palette built from a Google-blue seed (#0B57D0). Used as is when dynamic
 * color is off or unavailable (before Android 12); otherwise the wallpaper palette wins.
 * Screens read roles from MaterialTheme.colorScheme, never these values directly.
 */

private val LightColors: ColorScheme =
    lightColorScheme(
        primary = Color(0xFF0B57D0),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD3E3FD),
        onPrimaryContainer = Color(0xFF041E49),
        inversePrimary = Color(0xFFA8C7FA),
        secondary = Color(0xFF00639B),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFC2E7FF),
        onSecondaryContainer = Color(0xFF001D35),
        tertiary = Color(0xFF146C2E),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFC4EED0),
        onTertiaryContainer = Color(0xFF072711),
        error = Color(0xFFB3261E),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFF9DEDC),
        onErrorContainer = Color(0xFF410E0B),
        background = Color(0xFFF8FAFD),
        onBackground = Color(0xFF1F1F1F),
        surface = Color(0xFFF8FAFD),
        onSurface = Color(0xFF1F1F1F),
        surfaceVariant = Color(0xFFE1E3E1),
        onSurfaceVariant = Color(0xFF444746),
        surfaceTint = Color(0xFF0B57D0),
        inverseSurface = Color(0xFF303030),
        inverseOnSurface = Color(0xFFF2F2F2),
        outline = Color(0xFF747775),
        outlineVariant = Color(0xFFC4C7C5),
        surfaceBright = Color(0xFFF8FAFD),
        surfaceDim = Color(0xFFD3DBE5),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF3F6FB),
        surfaceContainer = Color(0xFFEDF1F8),
        surfaceContainerHigh = Color(0xFFE7EBF3),
        surfaceContainerHighest = Color(0xFFDFE4EC),
    )

private val DarkColors: ColorScheme =
    darkColorScheme(
        primary = Color(0xFFA8C7FA),
        onPrimary = Color(0xFF062E6F),
        primaryContainer = Color(0xFF0842A0),
        onPrimaryContainer = Color(0xFFD3E3FD),
        inversePrimary = Color(0xFF0B57D0),
        secondary = Color(0xFF7FCFFF),
        onSecondary = Color(0xFF003355),
        secondaryContainer = Color(0xFF004A77),
        onSecondaryContainer = Color(0xFFC2E7FF),
        tertiary = Color(0xFF6DD58C),
        onTertiary = Color(0xFF0A3818),
        tertiaryContainer = Color(0xFF0F5223),
        onTertiaryContainer = Color(0xFFC4EED0),
        error = Color(0xFFF2B8B5),
        onError = Color(0xFF601410),
        errorContainer = Color(0xFF8C1D18),
        onErrorContainer = Color(0xFFF9DEDC),
        background = Color(0xFF131314),
        onBackground = Color(0xFFE3E3E3),
        surface = Color(0xFF131314),
        onSurface = Color(0xFFE3E3E3),
        surfaceVariant = Color(0xFF444746),
        onSurfaceVariant = Color(0xFFC4C7C5),
        surfaceTint = Color(0xFFA8C7FA),
        inverseSurface = Color(0xFFE3E3E3),
        inverseOnSurface = Color(0xFF303030),
        outline = Color(0xFF8E918F),
        outlineVariant = Color(0xFF444746),
        surfaceBright = Color(0xFF37393B),
        surfaceDim = Color(0xFF131314),
        surfaceContainerLowest = Color(0xFF0E0E0F),
        surfaceContainerLow = Color(0xFF1B1C1D),
        surfaceContainer = Color(0xFF1F2022),
        surfaceContainerHigh = Color(0xFF292A2C),
        surfaceContainerHighest = Color(0xFF343537),
    )

private val AppShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(32.dp),
    )

@Composable
fun AnyRemoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColors
            else -> LightColors
        }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        shapes = AppShapes,
        content = content,
    )
}
