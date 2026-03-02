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
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import kotlin.math.abs
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
import com.cyberlist.neonlist.ui.LocalStrings
import com.cyberlist.neonlist.ui.LocalNeonIsDark
import com.cyberlist.neonlist.ui.components.ColorGrid
import com.cyberlist.neonlist.ui.components.NeonIconButton
import com.cyberlist.neonlist.ui.components.NeonScaffold
import com.cyberlist.neonlist.ui.components.MultiAxisSwipeDirection
import com.cyberlist.neonlist.ui.components.rememberMultiAxisSwipeActions
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

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
  val strings = LocalStrings.current

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
    snapshotFlow { manualLists.map { it.id } }
      .distinctUntilChanged()
      .debounce(250)
      .collect {
      if (sortMode == SortMode.MANUAL) {
        val needsPersist = manualLists.withIndex().any { (index, list) -> list.order != index }
        if (!needsPersist) return@collect
        val updated = manualLists.mapIndexed { index, list -> list.copy(order = index) }
        viewModel.reorderLists(updated)
      }
    }
  }

  NeonScaffold(
    title = strings.homeTitle,
    showBack = false,
    onBack = {},
    onSearch = onOpenSearch,
    onSettings = onOpenSettings,
    actions = {
      if (history.isNotEmpty()) {
        NeonIconButton(onClick = { viewModel.undo() }, label = strings.undo) {
          Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = strings.undo, tint = NeonPrimary)
        }
      }
      Spacer(modifier = Modifier.width(4.dp))
      NeonIconButton(onClick = { sortMenuOpen = true }, label = strings.sort) {
        Icon(Icons.Filled.MoreVert, contentDescription = strings.sort, tint = NeonMutedForeground)
      }
      DropdownMenu(
        expanded = sortMenuOpen,
        onDismissRequest = { sortMenuOpen = false },
        modifier = Modifier
          .background(NeonCard)
          .clip(RoundedCornerShape(16.dp))
      ) {
      DropdownMenuItem(
          text = { Text(strings.sortAZ, color = MaterialTheme.colorScheme.onSurface) },
          onClick = {
            sortMenuOpen = false
            viewModel.setSortMode(SortMode.AZ)
          }
        )
        DropdownMenuItem(
          text = { Text(strings.sortByCompletion, color = MaterialTheme.colorScheme.onSurface) },
          onClick = {
            sortMenuOpen = false
            viewModel.setSortMode(SortMode.COMPLETION)
          }
        )
        DropdownMenuItem(
          text = { Text(strings.manualOrder, color = MaterialTheme.colorScheme.onSurface) },
          onClick = {
            sortMenuOpen = false
            viewModel.setSortMode(SortMode.MANUAL)
          }
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .consumeWindowInsets(innerPadding)
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
      onDuplicate = { viewModel.duplicateList(it.id) },
      onAddNew = { isCreating = true },
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
                onDuplicate = { viewModel.duplicateList(list.id) },
                onAddNew = { isCreating = true },
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
            Text(strings.noData, style = MaterialTheme.typography.titleMedium, color = NeonMutedForeground)
            Text(strings.tapPlusToInitialize, style = MaterialTheme.typography.bodySmall, color = NeonMutedForeground)
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
        Icon(Icons.Filled.Add, contentDescription = strings.addList)
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
  onDuplicate: (ListEntity) -> Unit,
  onAddNew: () -> Unit,
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
            onDuplicate = { onDuplicate(list) },
            onAddNew = onAddNew,
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
  onDuplicate: () -> Unit,
  onAddNew: () -> Unit,
  dragHandle: Boolean = false,
  entranceDelayMs: Int = 0,
  sharedTransitionScope: SharedTransitionScope,
  animatedVisibilityScope: AnimatedContentScope
) {
  val color = NeonColorMap[list.color] ?: NeonPrimary
  val primaryTextColor = MaterialTheme.colorScheme.onSurface
  val isDarkTheme = LocalNeonIsDark.current
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

  val swipeState = rememberMultiAxisSwipeActions(
    onSlideDown = onAddNew,
    onSlideUp = onDuplicate,
    onSlideLeft = onDelete,
    onSlideRight = onEdit
  )
  val rowModifier = swipeState.modifier
  val isLongPressed = swipeState.isLongPressed

  val scale by animateFloatAsState(
    targetValue = if (pressed || isLongPressed) 0.96f else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "listCardPressScale"
  )

  val topGap by animateDpAsState(
    targetValue = if (swipeState.direction == MultiAxisSwipeDirection.Vertical && swipeState.offsetY < 0)
      with(density) { (abs(swipeState.offsetY) * 0.25f).toDp() }.coerceAtMost(32.dp)
    else 0.dp,
    label = "topGap"
  )
  val bottomGap by animateDpAsState(
    targetValue = if (swipeState.direction == MultiAxisSwipeDirection.Vertical && swipeState.offsetY > 0)
      with(density) { (abs(swipeState.offsetY) * 0.25f).toDp() }.coerceAtMost(32.dp)
    else 0.dp,
    label = "bottomGap"
  )

  AnimatedVisibility(
    visibleState = visibleState,
    enter = enter,
    exit = exit,
    modifier = Modifier.clipToBounds()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clipToBounds()
    ) {
      Spacer(modifier = Modifier.height(topGap))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(76.dp)
          .clipToBounds()
      ) {
        val swipeDirection = swipeState.direction
        val swipeProgress = swipeState.progress

        val strings = LocalStrings.current
        if (swipeDirection == MultiAxisSwipeDirection.Horizontal) {
          val isDelete = swipeState.isNegative
          val hintLabel = if (isDelete) strings.delete else strings.editList // Using editList as generic edit
          val baseBg = if (isDelete) Color(0x330B0B) else Color(0x1A2345)
          val bgColor = baseBg.copy(alpha = baseBg.alpha * swipeProgress)
          Row(
            modifier = Modifier
              .fillMaxSize()
              .background(bgColor)
              .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isDelete) Arrangement.End else Arrangement.Start
          ) {
            if (swipeProgress > 0f) {
              Text(
                hintLabel.uppercase(),
                color = if (isDelete) Color(0xFFFF6B6B) else Color(0xFF7AB5FF)
              )
            }
          }
        }

        if (swipeDirection == MultiAxisSwipeDirection.Vertical) {
          val isDuplicate = swipeState.isNegative
          val hintText = if (isDuplicate) strings.duplicate else strings.addList
          val hintColor = if (isDuplicate) NeonMutedForeground else NeonPrimary
          val hintIcon = if (isDuplicate) Icons.Filled.ContentCopy else Icons.Filled.Add

          androidx.compose.animation.AnimatedVisibility(
            visible = (isDuplicate && swipeState.offsetY < -10f) || (!isDuplicate && swipeState.offsetY > 10f),
            enter = fadeIn() + expandVertically(expandFrom = if (isDuplicate) Alignment.Bottom else Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = if (isDuplicate) Alignment.Bottom else Alignment.Top),
            modifier = Modifier.align(if (isDuplicate) Alignment.BottomCenter else Alignment.TopCenter)
          ) {
            Row(
              modifier = Modifier
                .padding(vertical = 8.dp)
                .background(NeonBackground.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(hintIcon, contentDescription = hintText, tint = hintColor)
              Spacer(modifier = Modifier.width(6.dp))
              Text(hintText, color = hintColor, style = MaterialTheme.typography.labelMedium)
            }
          }
        }

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
            .then(rowModifier)
            .fillMaxWidth()
            .height(76.dp)
            .clipToBounds()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(NeonCard)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, NeonBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp)
            .then(
              if (isDarkTheme) Modifier.shadow(10.dp, RoundedCornerShape(20.dp)) else Modifier
            )
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
              color = primaryTextColor
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
      Spacer(modifier = Modifier.height(bottomGap))
    }
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

  val strings = LocalStrings.current
  val dialogTextColor = MaterialTheme.colorScheme.onSurface
  val dialogPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
  androidx.compose.material3.AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = NeonCard,
    titleContentColor = NeonPrimary,
    textContentColor = dialogTextColor,
    title = { Text(strings.editList, style = MaterialTheme.typography.titleLarge) },
    text = {
      Column {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text(strings.title) },
          modifier = Modifier.fillMaxWidth(),
          textStyle = MaterialTheme.typography.bodyLarge.copy(color = dialogTextColor),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonPrimary,
            unfocusedBorderColor = NeonMutedForeground,
            focusedTextColor = dialogTextColor,
            unfocusedTextColor = dialogTextColor,
            focusedLabelColor = NeonPrimary,
            unfocusedLabelColor = NeonMutedForeground,
            focusedPlaceholderColor = dialogPlaceholderColor,
            unfocusedPlaceholderColor = dialogPlaceholderColor,
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
        Text(strings.save, style = MaterialTheme.typography.titleMedium)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancel, color = NeonMutedForeground, style = MaterialTheme.typography.titleMedium)
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
  val strings = LocalStrings.current
  val dialogTextColor = MaterialTheme.colorScheme.onSurface
  val dialogPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
  androidx.compose.material3.AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = NeonCard,
    titleContentColor = NeonPrimary,
    textContentColor = dialogTextColor,
    title = { Text(strings.newList, style = MaterialTheme.typography.titleLarge) },
    text = {
      Column {
        ColorGrid(selected = color, onSelect = onColorChange)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = title,
          onValueChange = onTitleChange,
          placeholder = { Text(strings.title.uppercase()) },
          modifier = Modifier.fillMaxWidth(),
          textStyle = MaterialTheme.typography.bodyLarge.copy(color = dialogTextColor),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonPrimary,
            unfocusedBorderColor = NeonMutedForeground,
            focusedTextColor = dialogTextColor,
            unfocusedTextColor = dialogTextColor,
            focusedPlaceholderColor = dialogPlaceholderColor,
            unfocusedPlaceholderColor = dialogPlaceholderColor,
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
        Text(strings.create, style = MaterialTheme.typography.titleMedium)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancel, color = NeonMutedForeground, style = MaterialTheme.typography.titleMedium)
      }
    }
  )
}
