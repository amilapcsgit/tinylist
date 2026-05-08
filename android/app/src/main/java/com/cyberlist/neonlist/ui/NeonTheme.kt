package com.cyberlist.neonlist.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DisplayFont = FontFamily.SansSerif
val UiFont = FontFamily.SansSerif

private val NeonTypography = Typography(
  displayLarge = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.W700, fontSize = 32.sp),
  displayMedium = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.W700, fontSize = 24.sp),
  titleLarge = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.W700, fontSize = 20.sp),
  titleMedium = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.W600, fontSize = 16.sp),
  bodyLarge = TextStyle(fontFamily = UiFont, fontWeight = FontWeight.W500, fontSize = 16.sp),
  bodyMedium = TextStyle(fontFamily = UiFont, fontWeight = FontWeight.W400, fontSize = 14.sp),
  labelSmall = TextStyle(fontFamily = UiFont, fontWeight = FontWeight.W500, fontSize = 11.sp, letterSpacing = 1.sp),
)

val LocalNeonIsDark = staticCompositionLocalOf { true }

@Composable
fun NeonTheme(
  isDark: Boolean,
  content: @Composable () -> Unit
) {
  val palette = if (isDark) DarkPalette else LightPalette
  val colorScheme = if (isDark) neonDarkColorScheme() else neonLightColorScheme()
  CompositionLocalProvider(
    LocalNeonPalette provides palette,
    LocalNeonIsDark provides isDark
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = NeonTypography,
      content = content
    )
  }
}
