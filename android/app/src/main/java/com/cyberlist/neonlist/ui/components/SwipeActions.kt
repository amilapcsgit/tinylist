package com.cyberlist.neonlist.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs

enum class MultiAxisSwipeAction { None, SlideDown, SlideUp, SlideLeft, SlideRight }
enum class MultiAxisSwipeDirection { None, Horizontal, Vertical }

data class MultiAxisSwipeState(
  val modifier: Modifier,
  val direction: MultiAxisSwipeDirection,
  val progress: Float,
  val isNegative: Boolean,
  val offsetX: Float,
  val offsetY: Float,
  val isLongPressed: Boolean
)

@Composable
fun rememberMultiAxisSwipeActions(
  onSlideDown: () -> Unit,
  onSlideUp: () -> Unit,
  onSlideLeft: () -> Unit,
  onSlideRight: () -> Unit
): MultiAxisSwipeState {
  var rawOffsetX by remember { mutableStateOf(0f) }
  var rawOffsetY by remember { mutableStateOf(0f) }
  var directionLocked by remember { mutableStateOf<MultiAxisSwipeDirection?>(null) }
  var action by remember { mutableStateOf(MultiAxisSwipeAction.None) }
  var isLongPressed by remember { mutableStateOf(false) }

  val horizontalThresholdPx = with(LocalDensity.current) { 200.dp.toPx() }
  val verticalThresholdPx = with(LocalDensity.current) { 75.dp.toPx() }

  val updatedOnSlideDown by rememberUpdatedState(onSlideDown)
  val updatedOnSlideUp by rememberUpdatedState(onSlideUp)
  val updatedOnSlideLeft by rememberUpdatedState(onSlideLeft)
  val updatedOnSlideRight by rememberUpdatedState(onSlideRight)

  val animatedOffsetX by animateFloatAsState(
    targetValue = if (action == MultiAxisSwipeAction.None) rawOffsetX else 0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    label = "multiAxisOffsetX"
  )
  val animatedOffsetY by animateFloatAsState(
    targetValue = if (action == MultiAxisSwipeAction.None) rawOffsetY else 0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    label = "multiAxisOffsetY"
  )
  val scale by animateFloatAsState(
    targetValue = if (action == MultiAxisSwipeAction.None) 1f else 0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    label = "multiAxisScale"
  )

  LaunchedEffect(action) {
    if (action != MultiAxisSwipeAction.None) {
      val currentAction = action
      delay(120)
      when (currentAction) {
        MultiAxisSwipeAction.SlideDown -> updatedOnSlideDown()
        MultiAxisSwipeAction.SlideUp -> updatedOnSlideUp()
        MultiAxisSwipeAction.SlideLeft -> updatedOnSlideLeft()
        MultiAxisSwipeAction.SlideRight -> updatedOnSlideRight()
        else -> Unit
      }
      rawOffsetX = 0f
      rawOffsetY = 0f
      directionLocked = null
      isLongPressed = false
      action = MultiAxisSwipeAction.None
    }
  }

  val direction = directionLocked ?: MultiAxisSwipeDirection.None
  val progress = when (direction) {
    MultiAxisSwipeDirection.Horizontal -> (abs(rawOffsetX) / horizontalThresholdPx).coerceIn(0f, 1f)
    MultiAxisSwipeDirection.Vertical -> (abs(rawOffsetY) / verticalThresholdPx).coerceIn(0f, 1f)
    MultiAxisSwipeDirection.None -> 0f
  }
  val isNegative = when (direction) {
    MultiAxisSwipeDirection.Horizontal -> rawOffsetX < 0f
    MultiAxisSwipeDirection.Vertical -> rawOffsetY < 0f
    MultiAxisSwipeDirection.None -> false
  }

  val modifier = Modifier
    .pointerInput(Unit) {
      detectHorizontalDragGestures(
        onDragStart = { directionLocked = MultiAxisSwipeDirection.Horizontal },
        onDragEnd = {
          if (directionLocked == MultiAxisSwipeDirection.Horizontal) {
            action = when {
              rawOffsetX > horizontalThresholdPx -> MultiAxisSwipeAction.SlideRight
              rawOffsetX < -horizontalThresholdPx -> MultiAxisSwipeAction.SlideLeft
              else -> MultiAxisSwipeAction.None
            }
            if (action == MultiAxisSwipeAction.None) {
              rawOffsetX = 0f
              directionLocked = null
            }
          }
        },
        onDragCancel = {
          rawOffsetX = 0f
          directionLocked = null
        },
        onHorizontalDrag = { change, dragAmount ->
          if (directionLocked == MultiAxisSwipeDirection.Horizontal) {
            rawOffsetX += dragAmount
            change.consume()
          }
        }
      )
    }
    .pointerInput(Unit) {
      detectDragGesturesAfterLongPress(
        onDragStart = {
          directionLocked = MultiAxisSwipeDirection.Vertical
          isLongPressed = true
        },
        onDragEnd = {
          if (directionLocked == MultiAxisSwipeDirection.Vertical) {
            action = when {
              rawOffsetY > verticalThresholdPx -> MultiAxisSwipeAction.SlideDown
              rawOffsetY < -verticalThresholdPx -> MultiAxisSwipeAction.SlideUp
              else -> MultiAxisSwipeAction.None
            }
            if (action == MultiAxisSwipeAction.None) {
              rawOffsetY = 0f
              directionLocked = null
              isLongPressed = false
            }
          }
        },
        onDragCancel = {
          rawOffsetY = 0f
          directionLocked = null
          isLongPressed = false
        },
        onDrag = { change, dragAmount ->
          if (directionLocked == MultiAxisSwipeDirection.Vertical) {
            rawOffsetY += dragAmount.y * 0.5f
            change.consume()
          }
        }
      )
    }
    .graphicsLayer(
      translationX = animatedOffsetX,
      translationY = animatedOffsetY,
      scaleX = scale,
      scaleY = scale
    )

  return MultiAxisSwipeState(
    modifier = modifier,
    direction = direction,
    progress = progress,
    isNegative = isNegative,
    offsetX = animatedOffsetX,
    offsetY = animatedOffsetY,
    isLongPressed = isLongPressed
  )
}
