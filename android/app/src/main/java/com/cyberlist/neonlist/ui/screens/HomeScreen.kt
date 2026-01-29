package com.cyberlist.neonlist.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberlist.neonlist.AppViewModel
import com.cyberlist.neonlist.SortMode
import com.cyberlist.neonlist.data.ListEntity
import com.cyberlist.neonlist.ui.NeonBackground
import com.cyberlist.neonlist.ui.NeonBorder
import com.cyberlist.neonlist.ui.NeonColorMap
import com.cyberlist.neonlist.ui.NeonPrimary
import com.cyberlist.neonlist.ui.NeonSecondary
import com.cyberlist.neonlist.ui.NeonMutedForeground
import com.cyberlist.neonlist.ui.components.ColorGrid
import com.cyberlist.neonlist.ui.components.NeonIconButton
import com.cyberlist.neonlist.ui.components.NeonPrimaryButton
import com.cyberlist.neonlist.ui.components.NeonScaffold
import com.cyberlist.neonlist.ui.components.NeonTextButton
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun HomeScreen(
  viewModel: AppViewModel,
  onOpenList: (String) -> Unit,
  onOpenSearch: () -> Unit,
  onOpenSettings: () -> Unit
) {
  val lists by viewModel.sortedLists.collectAsState()
  val items by viewModel.items.collectAsState()
  val sortMode by viewModel.currentSortMode.collectAsState()
  val history by viewModel.historyState.collectAsState()

  var isCreating by remember { mutableStateOf(false) }
  var newTitle by remember { mutableStateOf("") }
  var newColor by remember { mutableStateOf("green") }
  var sortMenuOpen by remember { mutableStateOf(false) }
  var editTarget by remember { mutableStateOf<ListEntity?>(null) }

  val manualLists = remember(lists) { mutableStateListOf<ListEntity>().apply { addAll(lists) } }
  val reorderState = rememberReorderableLazyListState(onMove = { from, to ->
    manualLists.add(to.index, manualLists.removeAt(from.index))
  })

  LaunchedEffect(lists, sortMode) {
    if (sortMode == SortMode.MANUAL) {
      manualLists.clear()
      manualLists.addAll(lists)
    }
  }
  LaunchedEffect(sortMode) {
    snapshotFlow { manualLists.toList() }.collect { ordered ->
      if (sortMode == SortMode.MANUAL) {
        val updated = ordered.mapIndexed { index, list -> list.copy(order = index) }
        viewModel.reorderLists(updated)
      }
    }
  }

  NeonScaffold(
    title = "Neon Lists",
    showBack = false,
    onBack = {},
    onSearch = onOpenSearch,
    onSettings = onOpenSettings,
    actions = {
      if (history.isNotEmpty()) {
        NeonIconButton(onClick = { viewModel.undo() }, label = "Undo") {
          Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = NeonPrimary)
        }
      }
      Spacer(modifier = Modifier.width(4.dp))
      NeonIconButton(onClick = { sortMenuOpen = true }, label = "Sort") {
        Icon(Icons.Filled.MoreVert, contentDescription = "Sort", tint = NeonMutedForeground)
      }
      DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
        DropdownMenuItem(text = { Text("Sort A-Z") }, onClick = {
          sortMenuOpen = false
          viewModel.setSortMode(SortMode.AZ)
        })
        DropdownMenuItem(text = { Text("Sort by Completion") }, onClick = {
          sortMenuOpen = false
          viewModel.setSortMode(SortMode.COMPLETION)
        })
        DropdownMenuItem(text = { Text("Manual Order") }, onClick = {
          sortMenuOpen = false
          viewModel.setSortMode(SortMode.MANUAL)
        })
      }
    }
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
          detectTapGestures(onLongPress = { isCreating = true })
        }
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(bottom = 96.dp)
      ) {
        if (sortMode == SortMode.MANUAL) {
          ReorderableLists(
            lists = manualLists,
            items = items,
            state = reorderState,
            onOpenList = onOpenList,
            onDelete = { list ->
              val listItems = items.filter { it.listId == list.id }
              viewModel.deleteList(list, listItems)
            },
            onEdit = { editTarget = it }
          )
        } else {
          Column(modifier = Modifier.fillMaxWidth()) {
            lists.forEach { list ->
              val listItems = items.filter { it.listId == list.id }
              ListCard(
                list = list,
                itemCount = listItems.size,
                completedCount = listItems.count { it.isDone },
                onOpen = { onOpenList(list.id) },
                onDelete = { viewModel.deleteList(list, listItems) },
                onEdit = { editTarget = list }
              )
            }
          }
        }

        if (lists.isEmpty() && !isCreating) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(top = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text("NO DATA", style = MaterialTheme.typography.titleMedium, color = NeonMutedForeground)
            Text("Tap + to initialize new list", style = MaterialTheme.typography.bodySmall, color = NeonMutedForeground)
          }
        }

        AnimatedVisibility(
          visible = isCreating,
          enter = fadeIn() + expandVertically(),
          exit = fadeOut() + shrinkVertically()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(NeonBackground.copy(alpha = 0.9f))
          ) {
            val previewColor = NeonColorMap[newColor] ?: NeonPrimary
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(previewColor)
                .padding(horizontal = 16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("New List", color = Color.White, style = MaterialTheme.typography.labelSmall)
              Text("0/0", color = Color.White, style = MaterialTheme.typography.bodySmall)
            }

            ColorGrid(selected = newColor, onSelect = { newColor = it })

            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
              OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                placeholder = { Text("LIST TITLE", color = NeonMutedForeground) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
              )

              Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
              ) {
                NeonTextButton(text = "CANCEL", onClick = { isCreating = false })
                Spacer(modifier = Modifier.width(12.dp))
                NeonPrimaryButton(text = "CREATE") {
                  if (newTitle.trim().isNotEmpty()) {
                    viewModel.addList(newTitle.trim(), newColor)
                    newTitle = ""
                    isCreating = false
                  }
                }
              }
            }
          }
        }
      }

      FloatingActionButton(
        onClick = { isCreating = true },
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(24.dp),
        containerColor = NeonPrimary,
        contentColor = Color.Black
      ) {
        Icon(Icons.Filled.Add, contentDescription = "Add")
      }
    }
  }

  if (editTarget != null) {
    EditListDialog(
      list = editTarget!!,
      onDismiss = { editTarget = null },
      onSave = { updated ->
        viewModel.updateList(updated)
        editTarget = null
      }
    )
  }
}

@Composable
private fun ReorderableLists(
  lists: List<ListEntity>,
  items: List<com.cyberlist.neonlist.data.ItemEntity>,
  state: ReorderableLazyListState,
  onOpenList: (String) -> Unit,
  onDelete: (ListEntity) -> Unit,
  onEdit: (ListEntity) -> Unit
) {
  LazyColumn(
    state = state.listState,
    modifier = Modifier
      .fillMaxWidth()
      .reorderable(state)
  ) {
    items(lists, key = { it.id }) { list ->
      ReorderableItem(state, key = list.id) { _ ->
        val listItems = items.filter { it.listId == list.id }
        Box(modifier = Modifier.detectReorderAfterLongPress(state)) {
          ListCard(
            list = list,
            itemCount = listItems.size,
            completedCount = listItems.count { it.isDone },
            onOpen = { onOpenList(list.id) },
            onDelete = { onDelete(list) },
            onEdit = { onEdit(list) },
            dragHandle = true
          )
        }
      }
    }
  }
}

@Composable
private fun ListCard(
  list: ListEntity,
  itemCount: Int,
  completedCount: Int,
  onOpen: () -> Unit,
  onDelete: () -> Unit,
  onEdit: () -> Unit,
  dragHandle: Boolean = false
) {
  val color = NeonColorMap[list.color] ?: NeonPrimary
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
      val bg = if (isDelete) Color(0x330B0B) else Color(0x1A2345)
      val label = if (isDelete) "Delete" else "Edit"
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
          .background(NeonSecondary.copy(alpha = 0.9f))
          .padding(horizontal = 16.dp)
          .shadow(6.dp)
          .clickable { onOpen() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .width(6.dp)
              .height(36.dp)
              .background(color)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            list.title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
          )
        }
        Text(
          "${completedCount}/${itemCount}",
          color = color,
          style = MaterialTheme.typography.titleMedium
        )
      }
    }
  )

  Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun EditListDialog(
  list: ListEntity,
  onDismiss: () -> Unit,
  onSave: (ListEntity) -> Unit
) {
  var title by remember { mutableStateOf(list.title) }
  var color by remember { mutableStateOf(list.color) }

  androidx.compose.material3.AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit List", color = NeonPrimary) },
    text = {
      Column {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Title") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        ColorGrid(selected = color, onSelect = { color = it })
      }
    },
    confirmButton = {
      NeonPrimaryButton(text = "SAVE") {
        if (title.trim().isNotEmpty()) {
          onSave(list.copy(title = title.trim(), color = color))
        }
      }
    },
    dismissButton = {
      NeonTextButton(text = "CANCEL", onClick = onDismiss)
    }
  )
}
