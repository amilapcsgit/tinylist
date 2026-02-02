package com.cyberlist.neonlist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberlist.neonlist.ui.NeonColorMap
import com.cyberlist.neonlist.ui.NeonPrimary
import com.cyberlist.neonlist.ui.NeonMutedForeground
import java.util.Locale

private val listColors = listOf(
  "red", "orange", "yellow", "lime", "green", "teal", "cyan", "blue", "purple", "pink"
)

@Composable
fun ColorGrid(selected: String, onSelect: (String) -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    for (row in 0 until 2) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        for (col in 0 until 5) {
          val colorName = listColors[row * 5 + col]
          val color = NeonColorMap[colorName] ?: NeonPrimary
          val isSelected = selected == colorName

          Box(
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(color)
              .selectable(
                selected = isSelected,
                onClick = { onSelect(colorName) },
                role = Role.RadioButton
              )
              .semantics {
                contentDescription = colorName.replaceFirstChar { it.titlecase(Locale.getDefault()) }
              }
              .then(
                if (isSelected) Modifier.border(2.dp, Color.White, RoundedCornerShape(8.dp)) else Modifier
              ),
            contentAlignment = Alignment.Center
          ) {
            if (isSelected) {
              Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = androidx.compose.ui.Modifier.padding(4.dp)
              )
            }
          }
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
