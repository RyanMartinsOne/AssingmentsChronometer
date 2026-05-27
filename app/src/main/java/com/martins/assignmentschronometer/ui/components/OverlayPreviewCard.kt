package com.martins.assignmentschronometer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.martins.assignmentschronometer.ui.screens.chronometer.ChronometerOverlayScreen
import com.martins.assignmentschronometer.ui.screens.chronometer.overlayWidthForScale

@Composable
fun OverlayPreviewCard(
    overlayScaleX: Float,
    overlayScaleY: Float,
    overlayOpacity: Float,
    showCommentCountInOverlay: Boolean,
    simplifiedOverlayEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    var isRunning by remember { mutableStateOf(true) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            ChronometerOverlayScreen(
                time = "00:05:42",
                isOverTime = false,
                commentCount = 13,
                showCommentCount = showCommentCountInOverlay,
                isRunning = isRunning,
                onDrag = { _, _ -> },
                isDraggable = false,
                onToggleTimer = { isRunning = !isRunning },
                onReset = {},
                onClose = {},
                overlayWidth = overlayWidthForScale(overlayScaleX),
                verticalScale = overlayScaleY,
                overlayOpacity = overlayOpacity,
                simplifiedOverlayEnabled = simplifiedOverlayEnabled
            )
        }
    }
}