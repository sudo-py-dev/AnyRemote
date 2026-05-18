package com.anyremote.remote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Reusable Premium Light Theme Design Tokens
val DeepBackground = Color(0xFFF4F6F9)   // Clean light silver-grey background
val CardSurface = Color(0xFFFFFFFF)      // Pure white card surfaces
val ButtonBackground = Color(0xFFEAECEF) // Light gray button background
val ButtonBorder = Color(0xFFD6D9E0)     // Soft gray border color

// Reusable Premium Dark Theme Design Tokens
val DeepBackgroundDark = Color(0xFF0F172A)   // Sleek dark midnight-slate background
val CardSurfaceDark = Color(0xFF1E293B)      // Rich dark card surfaces
val ButtonBackgroundDark = Color(0xFF334155) // Charcoal slate button background
val ButtonBorderDark = Color(0xFF475569)     // Slate border color

// Accent Colors
val ElectricBlue = Color(0xFF007AFF)   // AC Cooling / Active indicators
val WarmOrange = Color(0xFFFFA500)   // AC Heating / Active warning
val CrimsonRed = Color(0xFFFF3B30)   // Power key
val AccentGreen = Color(0xFF30D158)  // Speeds / Signal transmitted
val AccentPurple = Color(0xFF985EFF) // Dehumidify state

private val CustomLightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    background = DeepBackground,
    surface = CardSurface,
    surfaceVariant = Color(0xFFF0F2F5),
    outline = ButtonBorder,
    onPrimary = Color.White,
    onBackground = Color(0xFF302F2F),
    onSurface = Color(0xFF302F2F),
    onSurfaceVariant = Color(0xFF7A7F7C),
    error = CrimsonRed
)

private val CustomDarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    background = DeepBackgroundDark,
    surface = CardSurfaceDark,
    surfaceVariant = ButtonBackgroundDark,
    outline = ButtonBorderDark,
    onPrimary = Color.White,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFFA0AAB2),
    error = CrimsonRed
)

@Composable
fun isAppDarkTheme(): Boolean = MaterialTheme.colorScheme.background == DeepBackgroundDark

@Composable
fun getAppButtonBackground(): Color = if (isAppDarkTheme()) ButtonBackgroundDark else ButtonBackground

@Composable
fun getAppButtonBorder(): Color = if (isAppDarkTheme()) ButtonBorderDark else ButtonBorder

@Composable
fun AnyRemoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CustomDarkColorScheme else CustomLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(
            titleLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            ),
            labelMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp
            )
        ),
        content = content
    )
}
