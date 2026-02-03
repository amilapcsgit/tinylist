package com.cyberlist.neonlist.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cyberlist.neonlist.AppViewModel
import com.cyberlist.neonlist.ui.NeonMutedForeground
import com.cyberlist.neonlist.ui.NeonPrimary
import com.cyberlist.neonlist.ui.LocalStrings
import com.cyberlist.neonlist.ui.components.NeonScaffold
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
  viewModel: AppViewModel,
  onBack: () -> Unit
) {
  val lists by viewModel.lists.collectAsState()
  val items by viewModel.items.collectAsState()
  val currentLanguage by viewModel.currentLanguage.collectAsState()
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val strings = LocalStrings.current

  val exportLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json")
  ) { uri: Uri? ->
    if (uri == null) return@rememberLauncherForActivityResult
    scope.launch {
      val json = viewModel.exportJson()
      writeToUri(context, uri, json)
    }
  }

  NeonScaffold(
    title = strings.settings,
    showBack = true,
    onBack = onBack
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(innerPadding)
        .consumeWindowInsets(innerPadding)
        .padding(16.dp)
    ) {
      SectionCard(title = strings.appearance) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(strings.theme, style = MaterialTheme.typography.bodyLarge)
          Row {
            Icon(Icons.Filled.NightsStay, contentDescription = null, tint = NeonPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Filled.WbSunny, contentDescription = null, tint = NeonMutedForeground)
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(strings.themeLockedNote, color = NeonMutedForeground, style = MaterialTheme.typography.bodySmall)
      }

      Spacer(modifier = Modifier.height(16.dp))

      SectionCard(title = strings.language.uppercase()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("en" to "English", "it" to "Italiano", "si" to "සිංහල").forEach { (code, name) ->
            androidx.compose.material3.FilterChip(
              selected = currentLanguage == code,
              onClick = { viewModel.setLanguage(code) },
              label = { Text(name) },
              colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                selectedContainerColor = NeonPrimary,
                selectedLabelColor = Color.Black,
                labelColor = Color.White,
                containerColor = Color.White.copy(alpha = 0.05f)
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      SectionCard(title = strings.data) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
            .background(Color.White.copy(alpha = 0.03f))
            .padding(10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(strings.exportBackup, style = MaterialTheme.typography.bodyLarge)
            Text(strings.exportBackupNote, color = NeonMutedForeground, style = MaterialTheme.typography.bodySmall)
          }
          Icon(Icons.Filled.Download, contentDescription = null, tint = NeonMutedForeground)
        }

        Spacer(modifier = Modifier.height(8.dp))

        androidx.compose.material3.TextButton(onClick = {
          val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
          exportLauncher.launch("neonlist-backup-$date.json")
        }) {
          Text(strings.exportBackup, color = NeonPrimary)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          StatBox(label = strings.lists, value = lists.size.toString())
          StatBox(label = strings.items, value = items.size.toString())
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(strings.neonList, color = Color.White.copy(alpha = 0.1f), style = MaterialTheme.typography.titleLarge)
        Text("v1.0.0 // ${strings.androidBuild}", color = Color.White.copy(alpha = 0.2f), style = MaterialTheme.typography.bodySmall)
      }
    }
  }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(Color.White.copy(alpha = 0.03f))
      .padding(12.dp)
  ) {
    Text(title, color = NeonPrimary, style = MaterialTheme.typography.labelSmall)
    Spacer(modifier = Modifier.height(8.dp))
    content()
  }
}

@Composable
private fun StatBox(label: String, value: String) {
  Column(
    modifier = Modifier
      .background(Color.White.copy(alpha = 0.04f))
      .padding(12.dp)
  ) {
    Text(label, color = NeonMutedForeground, style = MaterialTheme.typography.labelSmall)
    Text(value, color = NeonPrimary, style = MaterialTheme.typography.titleLarge)
  }
}

private suspend fun writeToUri(context: Context, uri: Uri, content: String) {
  context.contentResolver.openOutputStream(uri)?.use { stream ->
    stream.write(content.toByteArray())
  }
}
