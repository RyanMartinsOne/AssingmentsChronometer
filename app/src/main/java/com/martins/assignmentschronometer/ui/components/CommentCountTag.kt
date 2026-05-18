package com.martins.assignmentschronometer.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.martins.assignmentschronometer.R

@Composable
fun CommentCountTag(
    count: Int,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    scale: Float = 1f
) {
    if (count <= 0) return

    val safeScale = scale.coerceIn(0.85f, 1.20f)

    val cornerRadius = if (isCompact) {
        (12f * safeScale).dp
    } else {
        (18f * safeScale).dp
    }

    val horizontalPadding = if (isCompact) {
        (8f * safeScale).dp
    } else {
        (18f * safeScale).dp
    }

    val verticalPadding = if (isCompact) {
        (4f * safeScale).dp
    } else {
        (14f * safeScale).dp
    }

    val iconSize = if (isCompact) {
        (16f * safeScale).dp
    } else {
        (30f * safeScale).dp
    }

    val spacerWidth = if (isCompact) {
        (4f * safeScale).dp
    } else {
        (8f * safeScale).dp
    }

    val compactFontSize = (12f * safeScale).sp

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
        shape = RoundedCornerShape(cornerRadius),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = horizontalPadding,
                vertical = verticalPadding
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.raised_hand),
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.width(spacerWidth))

            Text(
                text = if (isCompact) {
                    stringResource(R.string.comment_count_overlay, count)
                } else {
                    stringResource(R.string.comment_count, count)
                },
                style = if (isCompact) {
                    MaterialTheme.typography.labelMedium.copy(fontSize = compactFontSize)
                } else {
                    MaterialTheme.typography.titleLarge.copy(
                        fontSize = (MaterialTheme.typography.titleLarge.fontSize.value * safeScale).sp
                    )
                },
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1
            )
        }
    }
}