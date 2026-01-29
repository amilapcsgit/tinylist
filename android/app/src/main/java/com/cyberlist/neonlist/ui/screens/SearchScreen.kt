package com.cyberlist.neonlist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cyberlist.neonlist.AppViewModel
import com.cyberlist.neonlist.ui.NeonColorMap
import com.cyberlist.neonlist.ui.NeonMutedForeground
import com.cyberlist.neonlist.ui.NeonPrimary
import com.cyberlist.neonlist.ui.components.NeonScaffold

@Composable
fun SearchScreen(
  viewModel: AppViewModel,
  onBack: () -> Unit,
  onOpenList: (String) -> Unit
) {
  val lists by viewModel.lists.collectAsState()
  val items by viewModel.items.collectAsState()

  var query by remember { mutableStateOf("") }

  val q = query.trim().lowercase()
  val matchedLists = if (q.isEmpty()) emptyList() else lists.filter { it.title.lowercase().contains(q) }
  val matchedItems = if (q.isEmpty()) emptyList() else items.filter { it.text.lowercase().contains(q) }

  NeonScaffold(
    title = "Search",
    showBack = true,
    onBack = onBack
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
      OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        placeholder = { Text("Search lists and items...") },
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(20.dp))

      if (matchedLists.isNotEmpty()) {
        Text("MATCHING LISTS", color = NeonPrimary, style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(8.dp))
        matchedLists.forEach { list ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color.White.copy(alpha = 0.04f))
              .padding(14.dp)
              .clickable { onOpenList(list.id) },
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(list.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            val color = NeonColorMap[list.color] ?: NeonPrimary
            androidx.compose.foundation.layout.Box(
              modifier = Modifier
                .size(8.dp)
                .background(color)
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
        }
      }

      if (matchedItems.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text("MATCHING ITEMS", color = NeonPrimary, style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(8.dp))
        matchedItems.forEach { item ->
          val parent = lists.find { it.id == item.listId } ?: return@forEach
          val color = NeonColorMap[parent.color] ?: NeonPrimary
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color.White.copy(alpha = 0.04f))
              .padding(14.dp)
              .clickable { onOpenList(parent.id) }
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              androidx.compose.foundation.layout.Box(
                modifier = Modifier
                  .size(6.dp)
                  .background(color)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(parent.title.uppercase(), color = NeonMutedForeground, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              item.text,
              style = MaterialTheme.typography.bodyLarge,
              color = if (item.isDone) NeonMutedForeground else Color.White,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
        }
      }

      if (q.isNotEmpty() && matchedLists.isEmpty() && matchedItems.isEmpty()) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("NO MATCHES FOUND", color = NeonMutedForeground, style = MaterialTheme.typography.titleMedium)
      }
    }
  }
}
