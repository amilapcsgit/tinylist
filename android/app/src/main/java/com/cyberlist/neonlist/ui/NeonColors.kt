package com.cyberlist.neonlist.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val NeonBackground = Color(0xFF12121C)
val NeonForeground = Color(0xFFE6F3F2)
val NeonCard = Color(0xFF1B1B29)
val NeonPrimary = Color(0xFF00F5FF)
val NeonSecondary = Color(0xFF2A2A3A)
val NeonMuted = Color(0xFF38384A)
val NeonMutedForeground = Color(0xFF8A8AA6)
val NeonBorder = Color(0xFF2C2C3D)

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
  return lightColorScheme(
    primary = NeonPrimary,
    onPrimary = Color(0xFF0B0B12),
    primaryContainer = NeonPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = NeonForeground,
    secondary = NeonSecondary,
    onSecondary = NeonForeground,
    secondaryContainer = NeonSecondary,
    onSecondaryContainer = NeonForeground,
    tertiary = NeonPurple,
    onTertiary = NeonForeground,
    tertiaryContainer = NeonPurple.copy(alpha = 0.2f),
    onTertiaryContainer = NeonForeground,
    background = NeonBackground,
    onBackground = NeonForeground,
    surface = NeonCard,
    onSurface = NeonForeground,
    surfaceVariant = NeonSecondary,
    onSurfaceVariant = NeonMutedForeground,
    error = Color(0xFFFF4D4F),
    onError = Color.White,
    errorContainer = Color(0xFF7A1E1E),
    onErrorContainer = Color.White,
    outline = NeonBorder,
    outlineVariant = NeonBorder,
    inverseSurface = NeonForeground,
    inverseOnSurface = NeonBackground,
    inversePrimary = NeonPrimary,
    surfaceTint = NeonPrimary,
    scrim = Color(0x99000000)
  )
}
