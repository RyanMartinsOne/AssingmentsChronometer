package com.martins.assignmentschronometer.ui.screens.chronometer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.martins.assignmentschronometer.R
import com.martins.assignmentschronometer.ui.components.CommentCountTag
import com.martins.assignmentschronometer.ui.screens.settings.OverlaySizeRules
import com.martins.assignmentschronometer.viewmodel.SharedViewModel
import com.martins.assignmentschronometer.viewmodel.WeeklyPartsViewModel

private val OverlayBaseWidth = 192.dp
private val OverlayMinWidth = 168.dp

fun overlayWidthForScale(scaleX: Float): Dp {
    val scaled = OverlayBaseWidth * scaleX
    return if (scaled < OverlayMinWidth) OverlayMinWidth else scaled
}

@Composable
fun ChronometerOverlayRoute(
    sharedViewModel: SharedViewModel,
    onDrag: (dx: Float, dy: Float) -> Unit,
    weeklyPartsViewModel: WeeklyPartsViewModel,
    onClose: () -> Unit,
    showCommentCountInOverlay: Boolean,
    simplifiedOverlayEnabled: Boolean,
    overlayOpacity: Float = 1.0f,
    overlayWidth: Dp = OverlayBaseWidth,
    verticalScale: Float = 1f
) {
    ChronometerOverlayScreen(
        time = sharedViewModel.formattedTime,
        isOverTime = sharedViewModel.isOverTime,
        commentCount = sharedViewModel.commentCount,
        showCommentCount =
            (sharedViewModel.selectedAssignment?.showCommentCount == true) &&
                    showCommentCountInOverlay,
        isRunning = sharedViewModel.isRunning,
        onDrag = onDrag,
        isDraggable = true,
        onToggleTimer = {
            if (sharedViewModel.isRunning) {
                sharedViewModel.pause()
            } else {
                sharedViewModel.start()
            }
        },
        onReset = { sharedViewModel.resetOnOverlay() },
        onClose = {
            if (sharedViewModel.activePart != null) {
                sharedViewModel.savePartTimeAndResetForOverlay { updatedPart ->
                    weeklyPartsViewModel.updatePart(updatedPart)
                }
            } else {
                sharedViewModel.reset()
            }
            onClose()
        },
        overlayWidth = overlayWidth,
        overlayOpacity = overlayOpacity,
        verticalScale = verticalScale,
        simplifiedOverlayEnabled = simplifiedOverlayEnabled
    )
}

@Composable
fun ChronometerOverlayScreen(
    time: String,
    isOverTime: Boolean,
    commentCount: Int,
    showCommentCount: Boolean,
    isRunning: Boolean,
    onDrag: (dx: Float, dy: Float) -> Unit,
    isDraggable: Boolean = true,
    onToggleTimer: () -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit,
    overlayWidth: Dp = OverlayBaseWidth,
    overlayOpacity: Float = 1.0f,
    verticalScale: Float = 1f,
    simplifiedOverlayEnabled: Boolean = false
) {
    val bgColor = if (isOverTime) {
        Color(0xCCB00020)
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val safeVerticalScale = verticalScale.coerceIn(0.83f, 1.20f)
    val cornerRadius = (14f * safeVerticalScale.coerceIn(0.92f, 1.05f)).dp
    val containerVerticalPadding = (3.5f * safeVerticalScale).dp
    val commentTopPadding = (2f * safeVerticalScale).dp
    val commentScale = OverlaySizeRules.recommendedCompactCommentScale(
        overlayWidth = overlayWidth,
        verticalScale = safeVerticalScale
    )
    val timeFontSize = (37f * safeVerticalScale.coerceIn(0.92f, 1.16f)).sp
    val timeHorizontalPadding = (6f * safeVerticalScale.coerceIn(0.92f, 1.04f)).dp
    val timeVerticalPadding = (2f * safeVerticalScale).dp
    val iconButtonSize = (32f * safeVerticalScale.coerceIn(0.92f, 1.18f)).dp
    val iconSize = (18f * safeVerticalScale.coerceIn(0.92f, 1.08f)).dp
    val bottomRowPadding = (2f * safeVerticalScale).dp

    Surface(
        modifier = Modifier
            .width(overlayWidth)
            .wrapContentHeight()
            .graphicsLayer { alpha = overlayOpacity }
            .then(
                if (isDraggable) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.x, dragAmount.y)
                        }
                    }
                } else {
                    Modifier
                }
            )
            .then(
                if (simplifiedOverlayEnabled) {
                    Modifier.clickable { onToggleTimer() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(cornerRadius),
        color = bgColor,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = containerVerticalPadding)
        ) {
            if (showCommentCount) {
                CommentCountTag(
                    count = commentCount,
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .padding(top = commentTopPadding),
                    isCompact = true,
                    scale = commentScale
                )
            }

            Text(
                text = time,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = timeFontSize,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                modifier = Modifier.padding(
                    horizontal = timeHorizontalPadding,
                    vertical = timeVerticalPadding
                )
            )

            if (!simplifiedOverlayEnabled) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = bottomRowPadding)
                ) {
                    IconButton(
                        onClick = onToggleTimer,
                        modifier = Modifier.size(iconButtonSize)
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isRunning) R.drawable.pause else R.drawable.play
                            ),
                            contentDescription = if (isRunning) {
                                stringResource(R.string.pause)
                            } else {
                                stringResource(R.string.resume)
                            },
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(iconSize)
                        )
                    }

                    IconButton(
                        onClick = onReset,
                        modifier = Modifier.size(iconButtonSize)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.restart),
                            contentDescription = stringResource(R.string.reset),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(iconSize)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(iconButtonSize)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.delete),
                            contentDescription = stringResource(R.string.overlay_close),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            }
        }
    }
}