package com.cyberlist.neonlist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.border
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberlist.neonlist.ui.NeonColorMap
import com.cyberlist.neonlist.ui.NeonPrimary
import com.cyberlist.neonlist.ui.NeonMutedForeground

private val listColors = listOf(
  "red", "orange", "yellow", "lime", "green", "teal", "cyan", "blue", "purple", "pink"
)

@Composable
fun ColorGrid(selected: String, onSelect: (String) -> Unit) {
  Column {
    for (row in 0 until 2) {
      Row(modifier = Modifier.fillMaxWidth()) {
        for (col in 0 until 5) {
          val colorName = listColors[row * 5 + col]
          val color = NeonColorMap[colorName] ?: NeonPrimary
          androidx.compose.foundation.layout.Box(
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .background(color)
              .then(
                if (selected == colorName) Modifier.border(2.dp, Color.White) else Modifier
              )
              .pointerInput(Unit) { detectTapGestures { onSelect(colorName) } }
          )
        }
      }
    }
  }
}

@Composable
fun NeonPrimaryButton(text: String, onClick: () -> Unit) {
  Button(
    onClick = onClick,
    colors = ButtonDefaults.buttonColors(
      containerColor = NeonPrimary,
      contentColor = Color.Black
    )
  ) {
    Text(text, letterSpacing = 1.sp)
  }
}

@Composable
fun NeonTextButton(text: String, onClick: () -> Unit) {
  TextButton(onClick = onClick) {
    Text(text, color = NeonMutedForeground)
  }
}
