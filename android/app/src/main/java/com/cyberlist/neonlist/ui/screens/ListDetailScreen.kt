@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package com.cyberlist.neonlist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DragHandle
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import com.cyberlist.neonlist.AppViewModel
import com.cyberlist.neonlist.data.ItemEntity
import com.cyberlist.neonlist.ui.NeonBackground
import com.cyberlist.neonlist.ui.NeonColorMap
import com.cyberlist.neonlist.ui.NeonCard
import com.cyberlist.neonlist.ui.NeonBorder
import com.cyberlist.neonlist.ui.NeonMutedForeground
import com.cyberlist.neonlist.ui.NeonPrimary
import com.cyberlist.neonlist.ui.NeonSecondary
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
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs

private val numericRegex = Regex("(-?(?:\\d+[.,])?\\d+)(?=\\D*$)")
private enum class ItemSortMode { CREATED, AZ, MANUAL }

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ListDetailScreen(
  viewModel: AppViewModel,
  listId: String,
  onBack: () -> Unit,
  sharedTransitionScope: SharedTransitionScope,
  animatedVisibilityScope: AnimatedContentScope
) {
  val lists by viewModel.lists.collectAsState()
  val items by viewModel.items.collectAsState()
  val list = lists.find { it.id == listId } ?: return
  val listItemsRaw = items.filter { it.listId == listId }
  val listColor = NeonColorMap[list.color] ?: NeonPrimary

  var selectedIds by remember { mutableStateOf(setOf<String>()) }
  var menuOpen by remember { mutableStateOf(false) }
  var isAdding by remember { mutableStateOf(false) }
  var newItemText by remember { mutableStateOf("") }
  var newItemColor by remember { mutableStateOf("green") }
  var deleteTarget by remember { mutableStateOf<ItemEntity?>(null) }
  var editTarget by remember { mutableStateOf<ItemEntity?>(null) }
  var editText by remember { mutableStateOf("") }
  var editColor by remember { mutableStateOf("green") }
  var itemSortMode by remember { mutableStateOf(ItemSortMode.CREATED) }

  val selectionMode = selectedIds.isNotEmpty()
  val sumData = computeSum(listItemsRaw, selectedIds)
  val history by viewModel.historyState.collectAsState()
  val strings = LocalStrings.current
  val manualItems = remember { mutableStateListOf<ItemEntity>() }
  val manualListState = rememberLazyListState()
  val reorderState: ReorderableLazyListState =
    rememberReorderableLazyListState(manualListState) { from, to ->
      manualItems.add(to.index, manualItems.removeAt(from.index))
    }

  LaunchedEffect(listItemsRaw, itemSortMode) {
    if (itemSortMode == ItemSortMode.MANUAL) {
      manualItems.clear()
      manualItems.addAll(listItemsRaw.sortedBy { it.order })
    }
  }

  LaunchedEffect(itemSortMode) {
    if (itemSortMode != ItemSortMode.MANUAL) return@LaunchedEffect
    snapshotFlow { manualItems.map { it.id } }.collect {
      val updated = manualItems.mapIndexed { index, item -> item.copy(order = index.toLong()) }
      viewModel.reorderItems(updated)
    }
  }

  val listItems = when (itemSortMode) {
    ItemSortMode.AZ -> listItemsRaw.sortedBy { it.text.lowercase() }
    ItemSortMode.MANUAL -> manualItems
    ItemSortMode.CREATED -> listItemsRaw.sortedBy { it.createdAt }
  }

  val headerSharedState = sharedTransitionScope.rememberSharedContentState(key = "list-$listId")
  val headerContainerModifier = with(sharedTransitionScope) {
    Modifier.sharedElement(
      sharedContentState = headerSharedState,
      animatedVisibilityScope = animatedVisibilityScope
    )
  }

  NeonScaffold(
    title = list.title,
    showBack = true,
    onBack = onBack,
    headerModifier = headerContainerModifier,
    actions = {
      if (history.isNotEmpty()) {
        NeonIconButton(onClick = { viewModel.undo() }, label = strings.undo) {
          Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = strings.undo, tint = NeonPrimary)
        }
      }
      NeonIconButton(onClick = { menuOpen = true }, label = "Menu") {
        Icon(Icons.Filled.MoreVert, contentDescription = "Menu", tint = NeonMutedForeground)
      }
      DropdownMenu(
        expanded = menuOpen,
        onDismissRequest = { menuOpen = false },
        modifier = Modifier
          .background(NeonCard)
          .clip(RoundedCornerShape(16.dp))
      ) {
        DropdownMenuItem(text = { Text(strings.sortAZ, color = MaterialTheme.colorScheme.onSurface) }, onClick = {
          menuOpen = false
          itemSortMode = ItemSortMode.AZ
        })
        DropdownMenuItem(text = { Text(strings.manualOrder, color = MaterialTheme.colorScheme.onSurface) }, onClick = {
          menuOpen = false
          itemSortMode = ItemSortMode.MANUAL
        })
        DropdownMenuItem(text = { Text(strings.sortDefault, color = MaterialTheme.colorScheme.onSurface) }, onClick = {
          menuOpen = false
          itemSortMode = ItemSortMode.CREATED
        })
        DropdownMenuItem(text = { Text(strings.clearSelection, color = MaterialTheme.colorScheme.onSurface) }, onClick = {
          menuOpen = false
          selectedIds = emptySet()
        })
        DropdownMenuItem(text = { Text(strings.clearCompleted, color = MaterialTheme.colorScheme.onSurface) }, onClick = {
          menuOpen = false
          viewModel.clearCompleted(list.id)
        })
        DropdownMenuItem(text = { Text(strings.duplicateList, color = MaterialTheme.colorScheme.onSurface) }, onClick = {
          menuOpen = false
          viewModel.duplicateList(list.id)
        })
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .consumeWindowInsets(innerPadding)
    ) {
      Column(modifier = Modifier.fillMaxSize().padding(bottom = 120.dp)) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(listColor)
        )

        AnimatedContent(
          targetState = listItems.isEmpty(),
          label = "listEmptyTransition"
        ) { isEmpty ->
      if (isEmpty) {
        Box(
          modifier = Modifier.fillMaxWidth().padding(top = 120.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(strings.emptyList, color = NeonMutedForeground, style = MaterialTheme.typography.titleMedium)
            }
          } else {
            if (itemSortMode == ItemSortMode.MANUAL) {
              LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                state = manualListState
              ) {
                itemsIndexed(
                  items = manualItems,
                  key = { _, item -> item.id }
                ) { index, item ->
                  ReorderableItem(state = reorderState, key = item.id) { _ ->
                    TaskRow(
                      modifier = Modifier.animateItem(
                        fadeInSpec = spring(),
                        fadeOutSpec = spring()
                      ),
                      item = item,
                      color = listColor,
                      isSelected = selectedIds.contains(item.id),
                      entranceDelayMs = index * 50,
                      enableSwipe = false,
                      showDragHandle = true,
                      dragHandleModifier = Modifier.longPressDraggableHandle(),
                      onSlideDown = {},
                      onSlideUp = {},
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
                        editColor = item.color
                      },
                      onDelete = { deleteTarget = item }
                    )
                  }
                }
              }
            } else {
              LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(
                  items = listItems,
                  key = { _, item -> item.id }
                ) { index, item ->
                  TaskRow(
                    modifier = Modifier.animateItem(
                      fadeInSpec = spring(),
                      fadeOutSpec = spring()
                    ),
                    item = item,
                    color = listColor,
                    isSelected = selectedIds.contains(item.id),
                    entranceDelayMs = index * 50,
                    enableSwipe = true,
                    showDragHandle = false,
                    onSlideDown = {
                      newItemText = ""
                      newItemColor = list.color
                      isAdding = true
                    },
                    onSlideUp = { viewModel.addItem(list.id, item.text, item.color) },
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
                      editColor = item.color
                    },
                    onDelete = { deleteTarget = item }
                  )
                }
              }
            }
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
      Icon(Icons.Filled.Add, contentDescription = strings.addItem)
    }
  }

  if (isAdding) {
    val dialogTextColor = MaterialTheme.colorScheme.onSurface
    val dialogPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
    AlertDialog(
      onDismissRequest = { isAdding = false },
      containerColor = NeonCard,
      titleContentColor = NeonPrimary,
      textContentColor = dialogTextColor,
      title = { Text(strings.newItem, style = MaterialTheme.typography.titleLarge) },
      text = {
        Column {
          ColorGrid(selected = newItemColor, onSelect = { newItemColor = it })
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = newItemText,
            onValueChange = { newItemText = it },
            placeholder = { Text(strings.whatNeedsToBeDone) },
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
          Text(strings.addItem, style = MaterialTheme.typography.titleMedium)
        }
      },
      dismissButton = {
        TextButton(onClick = { isAdding = false }) {
          Text(strings.cancel, color = NeonMutedForeground, style = MaterialTheme.typography.titleMedium)
        }
      }
    )
  }

  if (deleteTarget != null) {
    val dialogTextColor = MaterialTheme.colorScheme.onSurface
    AlertDialog(
      onDismissRequest = { deleteTarget = null },
      containerColor = NeonCard,
      titleContentColor = NeonPrimary,
      textContentColor = dialogTextColor,
      title = { Text(strings.deleteItemQuestion, style = MaterialTheme.typography.titleLarge) },
      text = { Text("\"${deleteTarget?.text}\" will be permanently removed.", style = MaterialTheme.typography.bodyLarge) },
      confirmButton = {
        androidx.compose.material3.Button(
          onClick = {
            deleteTarget?.let { viewModel.deleteItem(it) }
            deleteTarget = null
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE94B3C),
            contentColor = MaterialTheme.colorScheme.onPrimary
          )
        ) {
          Text(strings.delete, style = MaterialTheme.typography.titleMedium)
        }
      },
      dismissButton = {
        TextButton(onClick = { deleteTarget = null }) {
          Text(strings.cancel, color = NeonMutedForeground, style = MaterialTheme.typography.titleMedium)
        }
      }
    )
  }

  if (editTarget != null) {
    val dialogTextColor = MaterialTheme.colorScheme.onSurface
    val dialogPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
    AlertDialog(
      onDismissRequest = { editTarget = null },
      containerColor = NeonCard,
      titleContentColor = NeonPrimary,
      textContentColor = dialogTextColor,
      title = { Text(strings.editItem, style = MaterialTheme.typography.titleLarge) },
      text = {
        Column {
          ColorGrid(selected = editColor, onSelect = { editColor = it })
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = editText,
            onValueChange = { editText = it },
            placeholder = { Text(strings.updateItemText) },
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
          onClick = {
            val trimmed = editText.trim()
            if (trimmed.isNotEmpty()) {
              editTarget?.let { viewModel.updateItem(it.copy(text = trimmed, color = editColor)) }
              editTarget = null
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
        TextButton(onClick = { editTarget = null }) {
          Text(strings.cancel, color = NeonMutedForeground, style = MaterialTheme.typography.titleMedium)
        }
      }
    )
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskRow(
  modifier: Modifier = Modifier,
  item: ItemEntity,
  color: Color,
  isSelected: Boolean,
  entranceDelayMs: Int = 0,
  enableSwipe: Boolean = true,
  showDragHandle: Boolean = false,
  dragHandleModifier: Modifier = Modifier,
  onSlideDown: () -> Unit,
  onSlideUp: () -> Unit,
  onToggleSelection: () -> Unit,
  onToggleDone: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  val swipeState = if (enableSwipe) {
    rememberMultiAxisSwipeActions(
      onSlideDown = onSlideDown,
      onSlideUp = onSlideUp,
      onSlideLeft = onDelete,
      onSlideRight = onEdit
    )
  } else {
    null
  }
  val rowModifier = if (enableSwipe && swipeState != null) modifier.then(swipeState.modifier) else modifier

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
  val isLongPressed = swipeState?.isLongPressed == true
  val isDarkTheme = LocalNeonIsDark.current
  val scale by animateFloatAsState(
    targetValue = if (pressed || isLongPressed) 0.96f else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "taskRowPressScale"
  )

  val topGap by animateDpAsState(
    targetValue = if (swipeState != null && swipeState.direction == MultiAxisSwipeDirection.Vertical && swipeState.offsetY < 0)
      with(density) { (abs(swipeState.offsetY) * 0.25f).toDp() }.coerceAtMost(32.dp)
    else 0.dp,
    label = "topGap"
  )
  val bottomGap by animateDpAsState(
    targetValue = if (swipeState != null && swipeState.direction == MultiAxisSwipeDirection.Vertical && swipeState.offsetY > 0)
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
    val itemColor = NeonColorMap[item.color] ?: color
    val bg = NeonCard
    val baseTextColor = MaterialTheme.colorScheme.onSurface
    val textColor by animateColorAsState(
      targetValue = if (isSelected && item.isDone) baseTextColor else if (item.isDone) NeonMutedForeground else baseTextColor,
      label = "itemText"
    )
    val highlightAlpha by animateFloatAsState(
      targetValue = if (isSelected) 0.85f else 0f,
      label = "textHighlight"
    )

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
        val swipeDirection = swipeState?.direction ?: MultiAxisSwipeDirection.None
        val swipeProgress = swipeState?.progress ?: 0f

        val strings = LocalStrings.current
        if (swipeState != null && swipeDirection == MultiAxisSwipeDirection.Horizontal) {
          val isDelete = swipeState.isNegative
          val hintLabel = if (isDelete) strings.delete else strings.editItem
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

        if (swipeState != null && swipeDirection == MultiAxisSwipeDirection.Vertical) {
          val isDuplicate = swipeState.isNegative
          val hintText = if (isDuplicate) strings.duplicate else strings.addItem
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

        Box(
          modifier = rowModifier
            .fillMaxWidth()
            .height(76.dp)
            .clipToBounds()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(bg)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, NeonBorder, RoundedCornerShape(20.dp))
            .then(
              if (isDarkTheme) Modifier.shadow(10.dp, RoundedCornerShape(20.dp)) else Modifier
            )
            .combinedClickable(
              interactionSource = interactionSource,
              indication = null,
              onClick = { onToggleSelection() },
              onDoubleClick = { onToggleDone() }
            )
        ) {
        Row(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .width(6.dp)
                .height(36.dp)
                .background(itemColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
              modifier = Modifier
                .background(
                  itemColor.copy(alpha = highlightAlpha),
                  RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                item.text,
                color = textColor,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedVisibility(
              visible = item.isDone,
              enter = scaleIn(),
              exit = scaleOut()
            ) {
              Icon(Icons.Filled.Check, contentDescription = "Done", tint = Color(0xFF69F0AE))
            }
            if (showDragHandle) {
              Spacer(modifier = Modifier.width(8.dp))
              Icon(
                Icons.Filled.DragHandle,
                contentDescription = "Drag",
                tint = NeonMutedForeground,
                modifier = dragHandleModifier
              )
            }
          }
        }
        }
      }
      Spacer(modifier = Modifier.height(bottomGap))
    }
  }

  Spacer(modifier = Modifier.height(10.dp))
}


@Composable
private fun BottomSumBar(
  sum: Double,
  count: Int,
  selectionMode: Boolean,
  onClearSelection: () -> Unit,
  modifier: Modifier = Modifier
) {
  val strings = LocalStrings.current
  val isDarkTheme = LocalNeonIsDark.current
  val barBackground = if (isDarkTheme) {
    NeonBackground.copy(alpha = 0.95f)
  } else {
    Color(0xFF0B0B12)
  }
  Box(
    modifier = Modifier
      .then(modifier)
      .fillMaxWidth()
      .height(72.dp)
      .background(barBackground)
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
          if (selectionMode) strings.selectedSum else strings.totalSum,
          color = NeonMutedForeground,
          style = MaterialTheme.typography.labelSmall
        )
        Row(verticalAlignment = Alignment.Bottom) {
          OdometerSumText(
            sum = sum,
            color = NeonPrimary,
            style = MaterialTheme.typography.displayMedium
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("(${strings.itemsCount(count)})", color = NeonMutedForeground, fontSize = 12.sp)
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

@Composable
private fun OdometerSumText(
  sum: Double,
  color: Color,
  style: TextStyle
) {
  var previousSum by remember { mutableStateOf(sum) }
  val directionUp = sum >= previousSum
  LaunchedEffect(sum) {
    previousSum = sum
  }

  val sumText = if (sum % 1 == 0.0) {
    sum.toInt().toString()
  } else {
    String.format(Locale.getDefault(), "%.2f", sum)
  }
  val digitStyle = style.copy(fontFamily = FontFamily.Monospace)
  val transitionSpec = if (directionUp) {
    (slideInVertically(animationSpec = tween(120)) { it } + fadeIn(animationSpec = tween(120))) togetherWith
      (slideOutVertically(animationSpec = tween(120)) { -it } + fadeOut(animationSpec = tween(120)))
  } else {
    (slideInVertically(animationSpec = tween(120)) { -it } + fadeIn(animationSpec = tween(120))) togetherWith
      (slideOutVertically(animationSpec = tween(120)) { it } + fadeOut(animationSpec = tween(120)))
  }

  Row(
    verticalAlignment = Alignment.Bottom,
    modifier = Modifier.clipToBounds()
  ) {
    sumText.forEachIndexed { index, ch ->
      if (ch.isDigit()) {
        AnimatedContent(
          targetState = ch,
          transitionSpec = { transitionSpec.using(SizeTransform(clip = true)) },
          modifier = Modifier.clipToBounds(),
          label = "sumDigit$index"
        ) { digit ->
          Text(
            digit.toString(),
            color = color,
            style = digitStyle
          )
        }
      } else {
        Text(
          ch.toString(),
          color = color,
          style = digitStyle
        )
      }
    }
  }
}

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
