package com.cyberlist.neonlist.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.cyberlist.neonlist.AppViewModel
import com.cyberlist.neonlist.BuildConfig
import com.cyberlist.neonlist.R
import com.cyberlist.neonlist.ui.NeonMutedForeground
import com.cyberlist.neonlist.ui.NeonPrimary
import com.cyberlist.neonlist.ui.DisplayFont
import com.cyberlist.neonlist.ui.LocalStrings
import com.cyberlist.neonlist.ui.components.NeonScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
  val themeMode by viewModel.themeMode.collectAsState()
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val strings = LocalStrings.current
  val uriHandler = LocalUriHandler.current
  var readmeText by remember { mutableStateOf<String?>(null) }
  var importFeedback by remember { mutableStateOf<String?>(null) }
  val isDarkTheme = themeMode != "light"

  LaunchedEffect(Unit) {
    readmeText = runCatching {
      context.assets.open("README.md").bufferedReader().use { it.readText() }
    }.getOrNull()
  }

  val exportLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json")
  ) { uri: Uri? ->
    if (uri == null) return@rememberLauncherForActivityResult
    scope.launch {
      val json = viewModel.exportJson()
      writeToUri(context, uri, json)
    }
  }

  val importLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri: Uri? ->
    if (uri == null) return@rememberLauncherForActivityResult
    scope.launch {
      runCatching {
        val json = readFromUri(context, uri)
        viewModel.importJson(json)
      }.onSuccess { summary ->
        importFeedback = strings.importSummary(
          summary.listsCreated,
          summary.listsMerged,
          summary.itemsImported
        )
      }.onFailure {
        importFeedback = strings.importFailed
      }
    }
  }

  if (importFeedback != null) {
    AlertDialog(
      onDismissRequest = { importFeedback = null },
      confirmButton = {
        androidx.compose.material3.TextButton(onClick = { importFeedback = null }) {
          Text("OK")
        }
      },
      text = { Text(importFeedback.orEmpty()) }
    )
  }

  NeonScaffold(
    title = strings.settings,
    showBack = true,
    onBack = onBack
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .consumeWindowInsets(innerPadding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 16.dp)
        .padding(bottom = 32.dp)
    ) {
      SectionCard(title = BuildConfig.APP_DISPLAY_NAME.uppercase()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Image(
            painter = painterResource(id = R.drawable.neonlist_logo),
            contentDescription = BuildConfig.APP_DISPLAY_NAME,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .height(56.dp)
              .width(56.dp)
              .clip(CircleShape)
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              BuildConfig.APP_DISPLAY_NAME,
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              "Made by L.J. Amila Prasad Perera",
              color = NeonPrimary,
              style = MaterialTheme.typography.labelSmall.copy(fontFamily = DisplayFont)
            )
            Text(
              "github.com/amilapcsgit",
              color = NeonMutedForeground,
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.clickable { uriHandler.openUri("https://github.com/amilapcsgit") }
            )
            Text(
              "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) // ${strings.androidBuild}",
              color = NeonMutedForeground,
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      SectionCard(title = strings.appearance) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            strings.theme,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge
          )
          Row {
            Icon(Icons.Filled.NightsStay, contentDescription = null, tint = NeonPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Filled.WbSunny, contentDescription = null, tint = NeonMutedForeground)
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            if (isDarkTheme) strings.themeDark else strings.themeLight,
            color = NeonMutedForeground,
            style = MaterialTheme.typography.bodySmall
          )
          androidx.compose.material3.Switch(
            checked = !isDarkTheme,
            onCheckedChange = { viewModel.setThemeMode(if (it) "light" else "dark") }
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
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
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                labelColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      SectionCard(title = strings.data) {
        DataActionRow(
          title = strings.exportBackup,
          note = strings.exportBackupNote,
          icon = { Icon(Icons.Filled.Upload, contentDescription = null, tint = NeonPrimary) },
          onClick = {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            exportLauncher.launch("neonlist-backup-$date.json")
          }
        )

        Spacer(modifier = Modifier.height(8.dp))

        DataActionRow(
          title = strings.importJson,
          note = strings.importJsonNote,
          icon = { Icon(Icons.Filled.Download, contentDescription = null, tint = NeonPrimary) },
          onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          StatBox(label = strings.lists, value = lists.size.toString())
          StatBox(label = strings.items, value = items.size.toString())
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      SectionCard(title = strings.creditsLicense) {
        val acknowledgments = readmeText
          ?.let { extractSection(it, "## 🙏 Acknowledgments", "## 📞 Contact") }
          ?.takeIf { it.isNotBlank() }
          ?: strings.creditsFallback
        val licenseText = readmeText
          ?.let { extractSection(it, "## 📄 License", "## 🙏 Acknowledgments") }
          ?.takeIf { it.isNotBlank() }
          ?: strings.creditsFallback

        Text(strings.acknowledgments, color = NeonPrimary, style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          acknowledgments,
          color = MaterialTheme.colorScheme.onSurface,
          style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(strings.license, color = NeonPrimary, style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          licenseText,
          color = MaterialTheme.colorScheme.onSurface,
          style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          strings.openGithub,
          color = NeonPrimary,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.clickable { uriHandler.openUri("https://github.com/amilapcsgit") }
        )
      }
    }
  }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .padding(12.dp)
  ) {
    Text(title, color = NeonPrimary, style = MaterialTheme.typography.labelSmall)
    Spacer(modifier = Modifier.height(8.dp))
    content()
  }
}

@Composable
private fun DataActionRow(
  title: String,
  note: String,
  icon: @Composable () -> Unit,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(MaterialTheme.colorScheme.surface)
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        title,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyLarge
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        note,
        color = NeonMutedForeground,
        style = MaterialTheme.typography.bodySmall
      )
    }
    Row(
      modifier = Modifier
        .size(44.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colorScheme.primaryContainer),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      icon()
    }
  }
}

@Composable
private fun StatBox(label: String, value: String) {
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .padding(12.dp)
  ) {
    Text(label, color = NeonMutedForeground, style = MaterialTheme.typography.labelSmall)
    Text(value, color = NeonPrimary, style = MaterialTheme.typography.titleLarge)
  }
}

private suspend fun writeToUri(context: Context, uri: Uri, content: String) {
  withContext(Dispatchers.IO) {
    context.contentResolver.openOutputStream(uri)?.use { stream ->
      stream.write(content.toByteArray())
    }
  }
}

private suspend fun readFromUri(context: Context, uri: Uri): String {
  return withContext(Dispatchers.IO) {
    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
      ?: throw IllegalArgumentException("Unable to read selected file.")
  }
}

private fun extractSection(text: String, start: String, end: String): String {
  val startIndex = text.indexOf(start)
  if (startIndex == -1) return ""
  val from = startIndex + start.length
  val endIndex = text.indexOf(end, from).takeIf { it != -1 } ?: text.length
  return text.substring(from, endIndex)
    .lines()
    .map { it.trim() }
    .filter { it.isNotBlank() && !it.startsWith("---") }
    .map { line ->
      line.replace(Regex("^[-*]\\s+"), "• ")
        .replace("```", "")
        .replace("**", "")
    }
    .joinToString("\n")
    .trim()
}
