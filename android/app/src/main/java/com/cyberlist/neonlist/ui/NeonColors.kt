package com.cyberlist.neonlist.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class NeonPalette(
  val background: Color,
  val foreground: Color,
  val card: Color,
  val secondary: Color,
  val muted: Color,
  val mutedForeground: Color,
  val border: Color
)

internal val DarkPalette = NeonPalette(
  background = Color(0xFF12121C),
  foreground = Color(0xFFE6F3F2),
  card = Color(0xFF1B1B29),
  secondary = Color(0xFF2A2A3A),
  muted = Color(0xFF38384A),
  mutedForeground = Color(0xFF8A8AA6),
  border = Color(0xFF2C2C3D)
)

internal val LightPalette = NeonPalette(
  background = Color(0xFFF5F7FB),
  foreground = Color(0xFF0B0F1A),
  card = Color(0xFFFFFFFF),
  secondary = Color(0xFFE9EEF7),
  muted = Color(0xFFD7DEEA),
  mutedForeground = Color(0xFF5B6475),
  border = Color(0xFFCCD4E3)
)

val LocalNeonPalette = staticCompositionLocalOf { DarkPalette }

val NeonBackground: Color
  @Composable get() = LocalNeonPalette.current.background

val NeonForeground: Color
  @Composable get() = LocalNeonPalette.current.foreground

val NeonCard: Color
  @Composable get() = LocalNeonPalette.current.card

val NeonSecondary: Color
  @Composable get() = LocalNeonPalette.current.secondary

val NeonMuted: Color
  @Composable get() = LocalNeonPalette.current.muted

val NeonMutedForeground: Color
  @Composable get() = LocalNeonPalette.current.mutedForeground

val NeonBorder: Color
  @Composable get() = LocalNeonPalette.current.border

val NeonPrimary = Color(0xFF00F5FF)

val NeonRed = Color(0xFFE94B3C)
val NeonOrange = Color(0xFFF38B2A)
val NeonYellow = Color(0xFFF9D248)
val NeonLime = Color(0xFF98E650)
val NeonGreen = Color(0xFF39D98A)
val NeonTeal = Color(0xFF26D0CE)
val NeonCyan = Color(0xFF3DD9F5)
val NeonBlue = Color(0xFF4D7CFE)
val NeonPurple = Color(0xFFB26CFF)
val NeonPink = Color(0xFFFF6EC7)

val NeonColorMap: Map<String, Color> = mapOf(
  "red" to NeonRed,
  "orange" to NeonOrange,
  "yellow" to NeonYellow,
  "lime" to NeonLime,
  "green" to NeonGreen,
  "teal" to NeonTeal,
  "cyan" to NeonCyan,
  "blue" to NeonBlue,
  "purple" to NeonPurple,
  "pink" to NeonPink
)

fun neonColorScheme(): ColorScheme {
  return neonDarkColorScheme()
}

fun neonDarkColorScheme(): ColorScheme {
  return darkColorScheme(
    primary = NeonPrimary,
    onPrimary = Color(0xFF0B0B12),
    primaryContainer = NeonPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = DarkPalette.foreground,
    secondary = DarkPalette.secondary,
    onSecondary = DarkPalette.foreground,
    secondaryContainer = DarkPalette.secondary,
    onSecondaryContainer = DarkPalette.foreground,
    tertiary = NeonPurple,
    onTertiary = DarkPalette.foreground,
    tertiaryContainer = NeonPurple.copy(alpha = 0.2f),
    onTertiaryContainer = DarkPalette.foreground,
    background = DarkPalette.background,
    onBackground = DarkPalette.foreground,
    surface = DarkPalette.card,
    onSurface = DarkPalette.foreground,
    surfaceVariant = DarkPalette.secondary,
    onSurfaceVariant = DarkPalette.mutedForeground,
    error = Color(0xFFFF4D4F),
    onError = Color.White,
    errorContainer = Color(0xFF7A1E1E),
    onErrorContainer = Color.White,
    outline = DarkPalette.border,
    outlineVariant = DarkPalette.border,
    inverseSurface = DarkPalette.foreground,
    inverseOnSurface = DarkPalette.background,
    inversePrimary = NeonPrimary,
    surfaceTint = NeonPrimary,
    scrim = Color(0x99000000)
  )
}

fun neonLightColorScheme(): ColorScheme {
  return lightColorScheme(
    primary = NeonPrimary,
    onPrimary = Color(0xFF001316),
    primaryContainer = NeonPrimary.copy(alpha = 0.12f),
    onPrimaryContainer = LightPalette.foreground,
    secondary = LightPalette.secondary,
    onSecondary = LightPalette.foreground,
    secondaryContainer = LightPalette.secondary,
    onSecondaryContainer = LightPalette.foreground,
    tertiary = NeonPurple,
    onTertiary = Color.White,
    tertiaryContainer = NeonPurple.copy(alpha = 0.2f),
    onTertiaryContainer = LightPalette.foreground,
    background = LightPalette.background,
    onBackground = LightPalette.foreground,
    surface = LightPalette.card,
    onSurface = LightPalette.foreground,
    surfaceVariant = LightPalette.secondary,
    onSurfaceVariant = LightPalette.mutedForeground,
    error = Color(0xFFFF4D4F),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD7),
    onErrorContainer = Color(0xFF410002),
    outline = LightPalette.border,
    outlineVariant = LightPalette.border,
    inverseSurface = LightPalette.foreground,
    inverseOnSurface = LightPalette.background,
    inversePrimary = NeonPrimary,
    surfaceTint = NeonPrimary,
    scrim = Color(0x33000000)
  )
}
