package com.cyberlist.neonlist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberlist.neonlist.AppViewModel
import com.cyberlist.neonlist.data.ItemEntity
import com.cyberlist.neonlist.ui.NeonBackground
import com.cyberlist.neonlist.ui.NeonColorMap
import com.cyberlist.neonlist.ui.NeonCard
import com.cyberlist.neonlist.ui.NeonMutedForeground
import com.cyberlist.neonlist.ui.NeonPrimary
import com.cyberlist.neonlist.ui.NeonSecondary
import com.cyberlist.neonlist.ui.components.ColorGrid
import com.cyberlist.neonlist.ui.components.NeonIconButton
import com.cyberlist.neonlist.ui.components.NeonScaffold

private val numericRegex = Regex("(-?(?:\\d+[.,])?\\d+)(?=\\D*$)")

@Composable
fun ListDetailScreen(
  viewModel: AppViewModel,
  listId: String,
  onBack: () -> Unit
) {
  val lists by viewModel.lists.collectAsState()
  val items by viewModel.items.collectAsState()
  val list = lists.find { it.id == listId } ?: return
  val listItems = items.filter { it.listId == listId }.sortedBy { it.createdAt }
  val listColor = NeonColorMap[list.color] ?: NeonPrimary

  var selectedIds by remember { mutableStateOf(setOf<String>()) }
  var menuOpen by remember { mutableStateOf(false) }
  var isAdding by remember { mutableStateOf(false) }
  var newItemText by remember { mutableStateOf("") }
  var newItemColor by remember { mutableStateOf("green") }
  var deleteTarget by remember { mutableStateOf<ItemEntity?>(null) }
  var editTarget by remember { mutableStateOf<ItemEntity?>(null) }
  var editText by remember { mutableStateOf("") }

  val selectionMode = selectedIds.isNotEmpty()
  val sumData = computeSum(listItems, selectedIds)

  NeonScaffold(
    title = list.title,
    showBack = true,
    onBack = onBack,
    actions = {
      NeonIconButton(onClick = { menuOpen = true }, label = "Menu") {
        Icon(Icons.Filled.MoreVert, contentDescription = "Menu", tint = NeonMutedForeground)
      }
      DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
        DropdownMenuItem(text = { Text("Clear Selection") }, onClick = {
          menuOpen = false
          selectedIds = emptySet()
        })
        DropdownMenuItem(text = { Text("Clear Completed") }, onClick = {
          menuOpen = false
          viewModel.clearCompleted(list.id)
        })
        DropdownMenuItem(text = { Text("Duplicate List") }, onClick = {
          menuOpen = false
          viewModel.duplicateList(list.id)
        })
      }
    }
  ) {
    Column(modifier = Modifier.fillMaxSize().padding(bottom = 120.dp)) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(4.dp)
          .background(listColor)
      )

      if (listItems.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxWidth().padding(top = 120.dp),
          contentAlignment = Alignment.Center
        ) {
          Text("EMPTY LIST", color = NeonMutedForeground, style = MaterialTheme.typography.titleMedium)
        }
      } else {
        Column(modifier = Modifier.fillMaxWidth()) {
          listItems.forEach { item ->
            TaskRow(
              item = item,
              color = listColor,
              isSelected = selectedIds.contains(item.id),
              selectionMode = selectionMode,
              onToggleSelection = {
                selectedIds = if (selectedIds.contains(item.id)) {
                  selectedIds - item.id
                } else {
                  selectedIds + item.id
                }
              },
              onToggleDone = { viewModel.toggleItem(item) },
              onEdit = {
                editTarget = item
                editText = item.text
              },
              onDelete = { deleteTarget = item }
            )
          }
        }
      }
    }

    BottomSumBar(
      sum = sumData.sum,
      count = sumData.count,
      selectionMode = selectionMode,
      onClearSelection = { selectedIds = emptySet() },
      modifier = Modifier.align(Alignment.BottomCenter)
    )

    FloatingActionButton(
      onClick = { isAdding = true },
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(bottom = 96.dp, end = 24.dp),
      containerColor = NeonPrimary,
      contentColor = Color.Black
    ) {
      Icon(Icons.Filled.Add, contentDescription = "Add")
    }
  }

  if (isAdding) {
    AlertDialog(
      onDismissRequest = { isAdding = false },
      containerColor = NeonCard,
      titleContentColor = NeonPrimary,
      textContentColor = Color.White,
      title = { Text("New Item", style = MaterialTheme.typography.titleLarge) },
      text = {
        Column {
          ColorGrid(selected = newItemColor, onSelect = { newItemColor = it })
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = newItemText,
            onValueChange = { newItemText = it },
            placeholder = { Text("What needs to be done?") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonPrimary,
              unfocusedBorderColor = NeonMutedForeground,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              focusedPlaceholderColor = NeonMutedForeground,
              unfocusedPlaceholderColor = NeonMutedForeground,
              cursorColor = NeonPrimary
            )
          )
        }
      },
      confirmButton = {
        androidx.compose.material3.Button(
          onClick = {
            if (newItemText.trim().isNotEmpty()) {
              viewModel.addItem(list.id, newItemText.trim(), newItemColor)
              newItemText = ""
              isAdding = false
            }
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = NeonPrimary,
            contentColor = Color.Black
          )
        ) {
          Text("ADD ITEM", style = MaterialTheme.typography.titleMedium)
        }
      },
      dismissButton = {
        TextButton(onClick = { isAdding = false }) {
          Text("CANCEL", color = NeonMutedForeground, style = MaterialTheme.typography.titleMedium)
        }
      }
    )
  }

  if (deleteTarget != null) {
    AlertDialog(
      onDismissRequest = { deleteTarget = null },
      containerColor = NeonCard,
      titleContentColor = NeonPrimary,
      textContentColor = Color.White,
      title = { Text("Delete Item?", style = MaterialTheme.typography.titleLarge) },
      text = { Text("\"${deleteTarget?.text}\" will be permanently removed.", style = MaterialTheme.typography.bodyLarge) },
      confirmButton = {
        androidx.compose.material3.Button(
          onClick = {
            deleteTarget?.let { viewModel.deleteItem(it) }
            deleteTarget = null
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE94B3C),
            contentColor = Color.White
          )
        ) {
          Text("DELETE", style = MaterialTheme.typography.titleMedium)
        }
      },
      dismissButton = {
        TextButton(onClick = { deleteTarget = null }) {
          Text("CANCEL", color = NeonMutedForeground, style = MaterialTheme.typography.titleMedium)
        }
      }
    )
  }

  if (editTarget != null) {
    AlertDialog(
      onDismissRequest = { editTarget = null },
      containerColor = NeonCard,
      titleContentColor = NeonPrimary,
      textContentColor = Color.White,
      title = { Text("Edit Item", style = MaterialTheme.typography.titleLarge) },
      text = {
        OutlinedTextField(
          value = editText,
          onValueChange = { editText = it },
          placeholder = { Text("Update item text") },
          modifier = Modifier.fillMaxWidth(),
          textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonPrimary,
            unfocusedBorderColor = NeonMutedForeground,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedPlaceholderColor = NeonMutedForeground,
            unfocusedPlaceholderColor = NeonMutedForeground,
            cursorColor = NeonPrimary
          )
        )
      },
      confirmButton = {
        androidx.compose.material3.Button(
          onClick = {
            val trimmed = editText.trim()
            if (trimmed.isNotEmpty()) {
              editTarget?.let { viewModel.updateItem(it.copy(text = trimmed)) }
              editTarget = null
            }
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = NeonPrimary,
            contentColor = Color.Black
          )
        ) {
          Text("SAVE", style = MaterialTheme.typography.titleMedium)
        }
      },
      dismissButton = {
        TextButton(onClick = { editTarget = null }) {
          Text("CANCEL", color = NeonMutedForeground, style = MaterialTheme.typography.titleMedium)
        }
      }
    )
  }
}

@Composable
private fun TaskRow(
  item: ItemEntity,
  color: Color,
  isSelected: Boolean,
  selectionMode: Boolean,
  onToggleSelection: () -> Unit,
  onToggleDone: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  val dismissState = rememberSwipeToDismissBoxState(
    confirmValueChange = { value ->
      when (value) {
        SwipeToDismissBoxValue.StartToEnd -> {
          onEdit()
          false
        }
        SwipeToDismissBoxValue.EndToStart -> {
          onDelete()
          true
        }
        else -> false
      }
    }
  )

  SwipeToDismissBox(
    state = dismissState,
    backgroundContent = {
      val isDelete = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
      val label = if (isDelete) "Delete" else "Edit"
      val bg = if (isDelete) Color(0x330B0B) else Color(0x1A2345)
      Row(
        modifier = Modifier
          .fillMaxSize()
          .background(bg)
          .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isDelete) Arrangement.End else Arrangement.Start
      ) {
        Text(label.uppercase(), color = if (isDelete) Color(0xFFFF6B6B) else Color(0xFF7AB5FF))
      }
    },
    content = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(64.dp)
          .background(if (isSelected) NeonBackground.copy(alpha = 0.6f) else NeonSecondary.copy(alpha = 0.9f))
          .padding(horizontal = 16.dp)
          .pointerInput(Unit) {
            detectTapGestures(
              onTap = { onToggleSelection() },
              onDoubleTap = { onToggleDone() }
            )
          },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .width(6.dp)
              .height(36.dp)
              .background(NeonColorMap[item.color] ?: color)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            item.text,
            color = if (item.isDone) NeonMutedForeground else Color.White,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }
        if (item.isDone) {
          Icon(Icons.Filled.Check, contentDescription = "Done", tint = Color(0xFF69F0AE))
        }
      }
    }
  )

  Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun BottomSumBar(
  sum: Double,
  count: Int,
  selectionMode: Boolean,
  onClearSelection: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = Modifier
      .then(modifier)
      .fillMaxWidth()
      .height(72.dp)
      .background(NeonBackground.copy(alpha = 0.95f))
      .padding(horizontal = 16.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(
          if (selectionMode) "SELECTED SUM" else "TOTAL SUM",
          color = NeonMutedForeground,
          style = MaterialTheme.typography.labelSmall
        )
        Row(verticalAlignment = Alignment.Bottom) {
          Text(
            if (sum % 1 == 0.0) sum.toInt().toString() else String.format("%.2f", sum),
            color = NeonPrimary,
            style = MaterialTheme.typography.displayMedium
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("($count items)", color = NeonMutedForeground, fontSize = 12.sp)
        }
      }
      if (selectionMode) {
        NeonIconButton(onClick = onClearSelection, label = "Clear") {
          Icon(Icons.Filled.Close, contentDescription = "Clear", tint = NeonMutedForeground)
        }
      }
    }
  }
}

private data class SumData(val sum: Double, val count: Int)

private fun computeSum(items: List<ItemEntity>, selected: Set<String>): SumData {
  val target = if (selected.isEmpty()) items else items.filter { selected.contains(it.id) }
  var sum = 0.0
  var count = 0
  target.forEach { item ->
    val match = numericRegex.find(item.text)
    if (match != null) {
      val value = match.value.replace(',', '.').toDoubleOrNull()
      if (value != null) {
        sum += value
        count++
      }
    }
  }
  return SumData(sum, count)
}
