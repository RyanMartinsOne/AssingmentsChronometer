package com.martins.assignmentschronometer.ui.screens.chronometer

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.martins.assignmentschronometer.R
import com.martins.assignmentschronometer.ui.components.CommentCountTag
import com.martins.assignmentschronometer.viewmodel.SharedViewModel

@Composable
fun ChronometerOverlayRoute(
    sharedViewModel: SharedViewModel,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onClose: () -> Unit
) {
    ChronometerOverlayScreen(
        time = sharedViewModel.formattedTime,
        isOverTime = sharedViewModel.isOverTime,
        commentCount = sharedViewModel.commentCount,
        showCommentCount = sharedViewModel.selectedAssignment?.showCommentCount == true,
        isRunning = sharedViewModel.isRunning,
        onDrag = onDrag,
        onToggleTimer = {
            if (sharedViewModel.isRunning) sharedViewModel.pause() else sharedViewModel.start()
        },
        onReset = { sharedViewModel.resetOnOverlay() },
        onClose = onClose
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
    onToggleTimer: () -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit
) {
    val bgColor = if (isOverTime) Color(0xCCB00020) else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(bgColor)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (showCommentCount) {
                CommentCountTag(
                    count = commentCount,
                    modifier = Modifier.padding(top = 4.dp),
                    isCompact = true
                )
            }

            Text(
                text = time,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 34.sp,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            ) {
                IconButton(onClick = onToggleTimer, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = painterResource(if (isRunning) R.drawable.pause else R.drawable.play),
                        contentDescription = if (isRunning) "Pausar" else "Continuar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onReset, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.restart),
                        contentDescription = "Reiniciar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = "Fechar overlay",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}