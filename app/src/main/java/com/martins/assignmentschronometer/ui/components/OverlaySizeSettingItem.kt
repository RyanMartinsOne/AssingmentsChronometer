package com.martins.assignmentschronometer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.martins.assignmentschronometer.R
import com.martins.assignmentschronometer.ui.screens.settings.OverlayAdjustmentResult
import com.martins.assignmentschronometer.ui.screens.settings.OverlaySizeRules

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OverlaySizeSettingItem(
    icon: ImageVector,
    title: String,
    description: String,
    currentScaleX: Float,
    currentScaleY: Float,
    messageRes: Int?,
    messageArgs: List<Any>,
    onHeightResultChanged: (OverlayAdjustmentResult) -> Unit,
    onScaleXSaved: (Float) -> Unit,
    onScaleYSaved: (Float) -> Unit,
    onClearMessage: () -> Unit
) {
    var localWidthLevel by remember(currentScaleX) {
        mutableIntStateOf(
            OverlaySizeRules.scaleToClosestLevel(
                currentScaleX,
                OverlaySizeRules.widthLevels
            )
        )
    }

    var localHeightLevel by remember(currentScaleY) {
        mutableIntStateOf(
            OverlaySizeRules.scaleToClosestLevel(
                currentScaleY,
                OverlaySizeRules.heightLevels
            )
        )
    }

    val localScaleX = OverlaySizeRules.widthLevels[localWidthLevel]
    val localScaleY = OverlaySizeRules.heightLevels[localHeightLevel]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stringResource(
                        R.string.settings_overlay_size_label,
                        description,
                        localWidthLevel + 1,
                        localHeightLevel + 1
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = stringResource(R.string.settings_overlay_size_width),
            style = MaterialTheme.typography.labelMedium
        )

        Slider(
            value = localWidthLevel.toFloat(),
            onValueChange = {
                val newWidthLevel = it.toInt()
                localWidthLevel = newWidthLevel

                val result = OverlaySizeRules.adjustHeightForNewWidth(
                    currentHeightLevel = localHeightLevel,
                    newWidthLevel = newWidthLevel
                )
                localHeightLevel = result.appliedHeightLevel
                onHeightResultChanged(result)
            },
            valueRange = 0f..OverlaySizeRules.widthLevels.lastIndex.toFloat(),
            steps = OverlaySizeRules.widthLevels.size - 2,
            onValueChangeFinished = {
                onScaleXSaved(OverlaySizeRules.widthLevels[localWidthLevel])
                onScaleYSaved(OverlaySizeRules.heightLevels[localHeightLevel])
            }
        )

        Text(
            text = stringResource(R.string.settings_overlay_size_height),
            style = MaterialTheme.typography.labelMedium
        )

        Slider(
            value = localHeightLevel.toFloat(),
            onValueChange = {
                val requestedLevel = it.toInt()

                val result = OverlaySizeRules.tryApplyHeightLevel(
                    requestedHeightLevel = requestedLevel,
                    currentWidthLevel = localWidthLevel
                )
                localHeightLevel = result.appliedHeightLevel
                onHeightResultChanged(result)
            },
            valueRange = 0f..OverlaySizeRules.heightLevels.lastIndex.toFloat(),
            steps = OverlaySizeRules.heightLevels.size - 2,
            onValueChangeFinished = {
                onScaleYSaved(OverlaySizeRules.heightLevels[localHeightLevel])
            }
        )

        if (messageRes != null) {
            Text(
                text = stringResource(messageRes, *messageArgs.toTypedArray()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = {
                    localWidthLevel = 0
                    localHeightLevel = 2
                    onClearMessage()
                    onScaleXSaved(OverlaySizeRules.widthLevels[localWidthLevel])
                    onScaleYSaved(OverlaySizeRules.heightLevels[localHeightLevel])
                },
                label = { Text(stringResource(R.string.settings_overlay_size_compact)) }
            )

            AssistChip(
                onClick = {
                    localWidthLevel = 0
                    localHeightLevel = 4
                    onClearMessage()
                    onScaleXSaved(OverlaySizeRules.widthLevels[localWidthLevel])
                    onScaleYSaved(OverlaySizeRules.heightLevels[localHeightLevel])
                },
                label = { Text(stringResource(R.string.settings_overlay_size_default)) }
            )

            AssistChip(
                onClick = {
                    localWidthLevel = 4
                    localHeightLevel = 7
                    onClearMessage()
                    onScaleXSaved(OverlaySizeRules.widthLevels[localWidthLevel])
                    onScaleYSaved(OverlaySizeRules.heightLevels[localHeightLevel])
                },
                label = { Text(stringResource(R.string.settings_overlay_size_large)) }
            )
        }

        OverlayPreviewCard(
            overlayScaleX = localScaleX,
            overlayScaleY = localScaleY
        )
    }
}