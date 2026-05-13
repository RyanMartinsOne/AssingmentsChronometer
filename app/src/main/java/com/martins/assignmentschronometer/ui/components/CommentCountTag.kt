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
    isCompact: Boolean = false
) {
    if (count > 0) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            shape = RoundedCornerShape(if (isCompact) 12.dp else 18.dp),
            modifier = modifier
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(
                    horizontal = if (isCompact) 8.dp else 18.dp,
                    vertical = if (isCompact) 4.dp else 14.dp
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.raised_hand),
                    contentDescription = null,
                    modifier = Modifier.size(if (isCompact) 16.dp else 30.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.width(if (isCompact) 4.dp else 8.dp))

                Text(
                    text = if (isCompact)
                        stringResource(R.string.comment_count_overlay, count)
                    else
                        stringResource(R.string.comment_count, count),
                    style = if (isCompact)
                        MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
                    else
                        MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}