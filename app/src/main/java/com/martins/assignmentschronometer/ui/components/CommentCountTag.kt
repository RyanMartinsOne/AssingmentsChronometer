package com.martins.assignmentschronometer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.text.style.TextOverflow
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

    if (!isCompact) {
        val safeScale = scale.coerceIn(0.90f, 1.20f)

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            shape = RoundedCornerShape((18f * safeScale).dp),
            modifier = modifier.wrapContentWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((8f * safeScale).dp),
                modifier = Modifier.padding(
                    horizontal = (18f * safeScale).dp,
                    vertical = (14f * safeScale).dp
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.raised_hand),
                    contentDescription = null,
                    modifier = Modifier.size((30f * safeScale).dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = stringResource(R.string.comment_count, count),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = (MaterialTheme.typography.titleLarge.fontSize.value * safeScale).sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        return
    }

    val safeScale = scale.coerceIn(1.02f, 1.10f)
    val cornerRadius = (13f * safeScale).dp
    val horizontalPadding = (4f * safeScale).dp
    val verticalPadding = (4f * safeScale).dp
    val iconSize = (15.9f * safeScale).dp
    val compactFontSize = (14.4f * safeScale).sp

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
        shape = RoundedCornerShape(cornerRadius),
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                space = (4f * safeScale).dp,
                alignment = Alignment.CenterHorizontally
            ),
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

            Text(
                text = stringResource(R.string.comment_count_overlay, count),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = compactFontSize
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}