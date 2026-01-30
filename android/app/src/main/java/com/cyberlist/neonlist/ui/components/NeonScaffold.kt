package com.cyberlist.neonlist.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberlist.neonlist.ui.NeonBackground
import com.cyberlist.neonlist.ui.NeonBorder
import com.cyberlist.neonlist.ui.NeonPrimary
import com.cyberlist.neonlist.ui.NeonSecondary
import com.cyberlist.neonlist.ui.NeonMutedForeground
import com.cyberlist.neonlist.ui.DisplayFont

@Composable
fun NeonScaffold(
  title: String,
  showBack: Boolean,
  onBack: () -> Unit,
  onSearch: (() -> Unit)? = null,
  onSettings: (() -> Unit)? = null,
  titleModifier: Modifier = Modifier,
  headerModifier: Modifier = Modifier,
  actions: @Composable RowScope.() -> Unit = {},
  content: @Composable BoxScope.(PaddingValues) -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Brush.verticalGradient(listOf(NeonBackground, NeonSecondary)))
  ) {
    ScanlineOverlay()

    Scaffold(
      containerColor = Color.Transparent,
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      topBar = {
        HeaderBar(
          title = title,
          showBack = showBack,
          onBack = onBack,
          onSearch = onSearch,
          onSettings = onSettings,
          titleModifier = titleModifier,
          headerModifier = headerModifier,
          actions = actions
        )
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
      ) {
        content(innerPadding)
      }
    }
  }
}

@Composable
private fun HeaderBar(
  title: String,
  showBack: Boolean,
  onBack: () -> Unit,
  onSearch: (() -> Unit)?,
  onSettings: (() -> Unit)?,
  titleModifier: Modifier,
  headerModifier: Modifier,
  actions: @Composable RowScope.() -> Unit
) {
  Row(
    modifier = Modifier
      .then(headerModifier)
      .fillMaxWidth()
      .height(64.dp)
      .clipToBounds()
      .background(NeonBackground.copy(alpha = 0.8f))
      .padding(horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (showBack) {
        NeonIconButton(onClick = onBack, label = "Back") {
          Icon(NeonIcons.Back, contentDescription = "Back", tint = NeonPrimary)
        }
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        modifier = titleModifier,
        text = title.uppercase(),
        color = NeonPrimary,
        style = TextStyle(
          fontFamily = DisplayFont,
          fontSize = 20.sp,
          shadow = Shadow(color = NeonPrimary, blurRadius = 16f)
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
      actions()

      if (onSearch != null) {
        Spacer(modifier = Modifier.width(8.dp))
        NeonIconButton(onClick = onSearch, label = "Search") {
          Icon(NeonIcons.Search, contentDescription = "Search", tint = NeonMutedForeground)
        }
      }
      if (onSettings != null) {
        Spacer(modifier = Modifier.width(8.dp))
        NeonIconButton(onClick = onSettings, label = "Settings") {
          Icon(NeonIcons.Settings, contentDescription = "Settings", tint = NeonMutedForeground)
        }
      }
    }
  }
}

@Composable
private fun ScanlineOverlay() {
  Canvas(modifier = Modifier.fillMaxSize()) {
    val lineHeight = 2f
    var y = 0f
    while (y < size.height) {
      drawRect(
        color = Color.Black.copy(alpha = 0.12f),
        topLeft = androidx.compose.ui.geometry.Offset(0f, y),
        size = androidx.compose.ui.geometry.Size(size.width, lineHeight)
      )
      y += lineHeight * 2
    }
  }
}

object NeonIcons {
  val Back = Icons.AutoMirrored.Filled.ArrowBack
  val Search = Icons.Filled.Search
  val Settings = Icons.Filled.Settings
}

@Composable
fun NeonIconButton(onClick: () -> Unit, label: String, content: @Composable () -> Unit) {
  androidx.compose.material3.IconButton(onClick = onClick) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(Color.White.copy(alpha = 0.04f)),
      contentAlignment = Alignment.Center
    ) {
      content()
    }
  }
}
