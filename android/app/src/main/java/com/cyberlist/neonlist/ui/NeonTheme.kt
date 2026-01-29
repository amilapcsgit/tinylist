package com.cyberlist.neonlist.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
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

@Composable
fun NeonTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = neonColorScheme(),
    typography = NeonTypography,
    content = content
  )
}
