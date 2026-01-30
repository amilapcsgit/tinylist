@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package com.cyberlist.neonlist.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.cyberlist.neonlist.AppViewModel
import com.cyberlist.neonlist.SortMode
import com.cyberlist.neonlist.data.ListEntity
import com.cyberlist.neonlist.ui.NeonBackground
import com.cyberlist.neonlist.ui.NeonCard
import com.cyberlist.neonlist.ui.NeonBorder
import com.cyberlist.neonlist.ui.NeonColorMap
import com.cyberlist.neonlist.ui.NeonCard
import com.cyberlist.neonlist.ui.NeonPrimary
import com.cyberlist.neonlist.ui.NeonSecondary
import com.cyberlist.neonlist.ui.NeonMutedForeground
import com.cyberlist.neonlist.ui.components.ColorGrid
import com.cyberlist.neonlist.ui.components.NeonIconButton
import com.cyberlist.neonlist.ui.components.NeonScaffold
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun HomeScreen(
  viewModel: AppViewModel,
  onOpenList: (String) -> Unit,
  onOpenSearch: () -> Unit,
  onOpenSettings: () -> Unit,
  sharedTransitionScope: SharedTransitionScope,
  animatedVisibilityScope: AnimatedContentScope
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
  val lazyListState = rememberLazyListState()
  val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
    manualLists.add(to.index, manualLists.removeAt(from.index))
  }

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
      DropdownMenu(
        expanded = sortMenuOpen,
        onDismissRequest = { sortMenuOpen = false },
        modifier = Modifier
          .background(NeonCard)
          .clip(RoundedCornerShape(16.dp))
      ) {
        DropdownMenuItem(
          text = { Text("Sort A-Z", color = Color.White) },
          onClick = {
            sortMenuOpen = false
            viewModel.setSortMode(SortMode.AZ)
          }
        )
        DropdownMenuItem(
          text = { Text("Sort by Completion", color = Color.White) },
          onClick = {
            sortMenuOpen = false
            viewModel.setSortMode(SortMode.COMPLETION)
          }
        )
        DropdownMenuItem(
          text = { Text("Manual Order", color = Color.White) },
          onClick = {
            sortMenuOpen = false
            viewModel.setSortMode(SortMode.MANUAL)
          }
        )
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
      listState = lazyListState,
      state = reorderState,
      onOpenList = onOpenList,
      onDelete = { list ->
        val listItems = items.filter { it.listId == list.id }
        viewModel.deleteList(list, listItems)
      },
      onEdit = { editTarget = it },
      sharedTransitionScope = sharedTransitionScope,
      animatedVisibilityScope = animatedVisibilityScope
    )
        } else {
          LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(lists, key = { _, list -> list.id }) { index, list ->
              val listItems = items.filter { it.listId == list.id }
              ListCard(
                list = list,
                itemCount = listItems.size,
                completedCount = listItems.count { it.isDone },
                onOpen = { onOpenList(list.id) },
                onDelete = { viewModel.deleteList(list, listItems) },
                onEdit = { editTarget = list },
                entranceDelayMs = index * 50,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope
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

  if (isCreating) {
    AddListDialog(
      title = newTitle,
      color = newColor,
      onTitleChange = { newTitle = it },
      onColorChange = { newColor = it },
      onDismiss = { isCreating = false },
      onSave = {
        if (newTitle.trim().isNotEmpty()) {
          viewModel.addList(newTitle.trim(), newColor)
          newTitle = ""
          isCreating = false
        }
      }
    )
  }
}

@Composable
private fun ReorderableLists(
  lists: List<ListEntity>,
  items: List<com.cyberlist.neonlist.data.ItemEntity>,
  listState: androidx.compose.foundation.lazy.LazyListState,
  state: ReorderableLazyListState,
  onOpenList: (String) -> Unit,
  onDelete: (ListEntity) -> Unit,
  onEdit: (ListEntity) -> Unit,
  sharedTransitionScope: SharedTransitionScope,
  animatedVisibilityScope: AnimatedContentScope
) {
  LazyColumn(
    state = listState,
    modifier = Modifier.fillMaxWidth()
  ) {
    itemsIndexed(lists, key = { _, list -> list.id }) { index, list ->
      ReorderableItem(state = state, key = list.id) { _ ->
        val listItems = items.filter { it.listId == list.id }
        Box(modifier = Modifier.longPressDraggableHandle()) {
          ListCard(
            list = list,
            itemCount = listItems.size,
            completedCount = listItems.count { it.isDone },
            onOpen = { onOpenList(list.id) },
            onDelete = { onDelete(list) },
            onEdit = { onEdit(list) },
            dragHandle = true,
            entranceDelayMs = index * 50,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope
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
  dragHandle: Boolean = false,
  entranceDelayMs: Int = 0,
  sharedTransitionScope: SharedTransitionScope,
  animatedVisibilityScope: AnimatedContentScope
) {
  val color = NeonColorMap[list.color] ?: NeonPrimary
  val density = LocalDensity.current
  val delayMillis = entranceDelayMs.coerceAtLeast(0)
  val offsetSpec = tween<IntOffset>(durationMillis = 160, easing = FastOutSlowInEasing)
  val sizeSpec = tween<IntSize>(durationMillis = 160, easing = FastOutSlowInEasing)
  val enter = slideInVertically(
    animationSpec = tween(
      durationMillis = 160,
      delayMillis = delayMillis,
      easing = FastOutSlowInEasing
    )
  ) { with(density) { -40.dp.roundToPx() } } +
    expandVertically(animationSpec = sizeSpec, expandFrom = Alignment.Top) +
    fadeIn(
      animationSpec = tween(
        durationMillis = 120,
        delayMillis = delayMillis,
        easing = FastOutSlowInEasing
      )
    )
  val exit = slideOutVertically(animationSpec = offsetSpec) + shrinkVertically(animationSpec = sizeSpec)
  val visibleState: MutableTransitionState<Boolean> =
    remember { MutableTransitionState(false).apply { targetState = true } }
  val interactionSource = remember { MutableInteractionSource() }
  val pressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (pressed) 0.98f else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "listCardPressScale"
  )
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

  AnimatedVisibility(
    visibleState = visibleState,
    enter = enter,
    exit = exit,
    modifier = Modifier.clipToBounds()
  ) {
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
        val sharedState = sharedTransitionScope.rememberSharedContentState(key = "list-${list.id}")
        val sharedModifier = with(sharedTransitionScope) {
          Modifier.sharedElement(
            sharedContentState = sharedState,
            animatedVisibilityScope = animatedVisibilityScope
          )
        }
        Row(
          modifier = Modifier
            .then(sharedModifier)
            .fillMaxWidth()
            .height(76.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(NeonCard)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, NeonBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp)
            .shadow(10.dp, RoundedCornerShape(20.dp))
            .clickable(
              interactionSource = interactionSource,
              indication = null
            ) { onOpen() },
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
              style = MaterialTheme.typography.titleLarge,
              color = Color.White
            )
          }
          Box(
            modifier = Modifier
              .background(color.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text(
              "${completedCount}/${itemCount}",
              color = color,
              style = MaterialTheme.typography.titleMedium
            )
          }
        }
      }
    )
  }

  Spacer(modifier = Modifier.height(10.dp))
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
    containerColor = NeonCard,
    titleContentColor = NeonPrimary,
    textContentColor = Color.White,
    title = { Text("Edit List", style = MaterialTheme.typography.titleLarge) },
    text = {
      Column {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Title") },
          modifier = Modifier.fillMaxWidth(),
          textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonPrimary,
            unfocusedBorderColor = NeonMutedForeground,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = NeonPrimary,
            unfocusedLabelColor = NeonMutedForeground,
            focusedPlaceholderColor = NeonMutedForeground,
            unfocusedPlaceholderColor = NeonMutedForeground,
            cursorColor = NeonPrimary
          )
        )
        Spacer(modifier = Modifier.height(12.dp))
        ColorGrid(selected = color, onSelect = { color = it })
      }
    },
    confirmButton = {
      androidx.compose.material3.Button(
        onClick = {
          if (title.trim().isNotEmpty()) {
            onSave(list.copy(title = title.trim(), color = color))
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
      TextButton(onClick = onDismiss) {
        Text("CANCEL", color = NeonMutedForeground, style = MaterialTheme.typography.titleMedium)
      }
    }
  )
}

@Composable
private fun AddListDialog(
  title: String,
  color: String,
  onTitleChange: (String) -> Unit,
  onColorChange: (String) -> Unit,
  onDismiss: () -> Unit,
  onSave: () -> Unit
) {
  androidx.compose.material3.AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = NeonCard,
    titleContentColor = NeonPrimary,
    textContentColor = Color.White,
    title = { Text("New List", style = MaterialTheme.typography.titleLarge) },
    text = {
      Column {
        ColorGrid(selected = color, onSelect = onColorChange)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = title,
          onValueChange = onTitleChange,
          placeholder = { Text("LIST TITLE") },
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
        onClick = onSave,
        colors = ButtonDefaults.buttonColors(
          containerColor = NeonPrimary,
          contentColor = Color.Black
        )
      ) {
        Text("CREATE", style = MaterialTheme.typography.titleMedium)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("CANCEL", color = NeonMutedForeground, style = MaterialTheme.typography.titleMedium)
      }
    }
  )
}
