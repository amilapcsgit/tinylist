@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package com.cyberlist.neonlist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.input.pointer.consumePositionChange
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
import com.cyberlist.neonlist.ui.components.ColorGrid
import com.cyberlist.neonlist.ui.components.NeonIconButton
import com.cyberlist.neonlist.ui.components.NeonScaffold
import kotlinx.coroutines.delay
import java.util.Locale

private val numericRegex = Regex("(-?(?:\\d+[.,])?\\d+)(?=\\D*$)")

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
  var editColor by remember { mutableStateOf("green") }

  val selectionMode = selectedIds.isNotEmpty()
  val sumData = computeSum(listItems, selectedIds)

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
        DropdownMenuItem(text = { Text("Clear Selection", color = Color.White) }, onClick = {
          menuOpen = false
          selectedIds = emptySet()
        })
        DropdownMenuItem(text = { Text("Clear Completed", color = Color.White) }, onClick = {
          menuOpen = false
          viewModel.clearCompleted(list.id)
        })
        DropdownMenuItem(text = { Text("Duplicate List", color = Color.White) }, onClick = {
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
              Text("EMPTY LIST", color = NeonMutedForeground, style = MaterialTheme.typography.titleMedium)
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
                  )
                    .verticalSwipeActions(
                      onSlideDown = { isAdding = true },
                      onSlideUp = { viewModel.addItem(list.id, item.text, item.color) }
                    ),
                  item = item,
                  color = listColor,
                  isSelected = selectedIds.contains(item.id),
                  entranceDelayMs = index * 50,
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
        Column {
          ColorGrid(selected = editColor, onSelect = { editColor = it })
          Spacer(modifier = Modifier.height(12.dp))
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskRow(
  modifier: Modifier = Modifier,
  item: ItemEntity,
  color: Color,
  isSelected: Boolean,
  entranceDelayMs: Int = 0,
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
    label = "taskRowPressScale"
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
        val progress = dismissState.progress
        val label = if (isDelete) "Delete" else "Edit"
        val baseBg = if (isDelete) Color(0x330B0B) else Color(0x1A2345)
        val bg = baseBg.copy(alpha = baseBg.alpha * progress)
        Row(
          modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 20.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = if (isDelete) Arrangement.End else Arrangement.Start
        ) {
          if (progress > 0f) {
            Text(
              label.uppercase(),
              color = if (isDelete) Color(0xFFFF6B6B) else Color(0xFF7AB5FF),
              modifier = Modifier
            )
          }
        }
      },
      content = {
        val itemColor = NeonColorMap[item.color] ?: color
        val bg = NeonCard
        val textColor by animateColorAsState(
          targetValue = if (isSelected && item.isDone) Color.White else if (item.isDone) NeonMutedForeground else Color.White,
          label = "itemText"
        )
        val highlightAlpha by animateFloatAsState(
          targetValue = if (isSelected) 0.85f else 0f,
          label = "textHighlight"
        )

        Box(
          modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .clipToBounds()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(bg)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, NeonBorder, RoundedCornerShape(20.dp))
            .shadow(10.dp, RoundedCornerShape(20.dp))
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
            AnimatedVisibility(
              visible = item.isDone,
              enter = scaleIn(),
              exit = scaleOut()
            ) {
              Icon(Icons.Filled.Check, contentDescription = "Done", tint = Color(0xFF69F0AE))
            }
          }
        }
      }
    )
  }

  Spacer(modifier = Modifier.height(10.dp))
}

private enum class VerticalSwipeAction { None, SlideDown, SlideUp }

private fun Modifier.verticalSwipeActions(
  onSlideDown: () -> Unit,
  onSlideUp: () -> Unit
): Modifier = composed {
  var rawOffsetY by remember { mutableStateOf(0f) }
  var action by remember { mutableStateOf(VerticalSwipeAction.None) }
  val updatedOnSlideDown by rememberUpdatedState(onSlideDown)
  val updatedOnSlideUp by rememberUpdatedState(onSlideUp)
  val animatedOffsetY by animateFloatAsState(
    targetValue = if (action == VerticalSwipeAction.None) rawOffsetY else 0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    label = "verticalSwipeOffset"
  )
  val scale by animateFloatAsState(
    targetValue = if (action == VerticalSwipeAction.None) 1f else 0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    label = "verticalSwipeScale"
  )

  LaunchedEffect(action) {
    when (action) {
      VerticalSwipeAction.SlideDown -> {
        delay(120)
        updatedOnSlideDown()
        rawOffsetY = 0f
        action = VerticalSwipeAction.None
      }
      VerticalSwipeAction.SlideUp -> {
        delay(120)
        updatedOnSlideUp()
        rawOffsetY = 0f
        action = VerticalSwipeAction.None
      }
      VerticalSwipeAction.None -> Unit
    }
  }

  this
    .pointerInput(Unit) {
      detectDragGestures(
        onDragEnd = {
          action = when {
            rawOffsetY > 150f -> VerticalSwipeAction.SlideDown
            rawOffsetY < -150f -> VerticalSwipeAction.SlideUp
            else -> VerticalSwipeAction.None
          }
          if (action == VerticalSwipeAction.None) {
            rawOffsetY = 0f
          }
        },
        onDragCancel = { rawOffsetY = 0f },
        onDrag = { change, dragAmount ->
          if (action != VerticalSwipeAction.None) return@detectDragGestures
          change.consumePositionChange()
          rawOffsetY += dragAmount.y * 0.5f
        }
      )
    }
    .graphicsLayer(
      translationY = animatedOffsetY,
      scaleX = scale,
      scaleY = scale
    )
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
          OdometerSumText(
            sum = sum,
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
