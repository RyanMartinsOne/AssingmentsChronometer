package com.martins.assignmentschronometer.ui.screens.settings

import androidx.annotation.StringRes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.martins.assignmentschronometer.R

data class OverlayAdjustmentResult(
    val appliedHeightLevel: Int,
    @param:StringRes val messageRes: Int? = null,
    val messageArgs: List<Any> = emptyList()
)

object OverlaySizeRules {

    val widthLevels: List<Float> = listOf(
        0.86f, 0.89f, 0.92f, 0.95f, 0.98f, 1.01f, 1.04f, 1.07f
    )

    val heightLevels: List<Float> = listOf(
        0.91f, 0.94f, 0.96f, 0.99f, 1.04f, 1.08f, 1.11f, 1.15f
    )

    fun scaleToClosestLevel(scale: Float, levels: List<Float>): Int {
        return levels.indices.minByOrNull { index ->
            kotlin.math.abs(levels[index] - scale)
        } ?: 0
    }

    fun minimumWidthLevelForHeight(heightLevel: Int): Int {
        return when {
            heightLevel >= 7 -> 4
            heightLevel >= 6 -> 3
            heightLevel >= 5 -> 2
            else -> 0
        }
    }

    fun maxAllowedHeightLevelForWidth(widthLevel: Int): Int {
        return when {
            widthLevel >= 4 -> heightLevels.lastIndex
            widthLevel >= 3 -> 6
            widthLevel >= 2 -> 5
            else -> 4
        }
    }

    fun tryApplyHeightLevel(
        requestedHeightLevel: Int,
        currentWidthLevel: Int
    ): OverlayAdjustmentResult {
        val minWidthLevel = minimumWidthLevelForHeight(requestedHeightLevel)

        return if (currentWidthLevel >= minWidthLevel) {
            OverlayAdjustmentResult(appliedHeightLevel = requestedHeightLevel)
        } else {
            val fallbackHeightLevel = maxAllowedHeightLevelForWidth(currentWidthLevel)
            OverlayAdjustmentResult(
                appliedHeightLevel = fallbackHeightLevel,
                messageRes = R.string.settings_overlay_error_height_requires_width,
                messageArgs = listOf(minWidthLevel + 1)
            )
        }
    }

    fun adjustHeightForNewWidth(
        currentHeightLevel: Int,
        newWidthLevel: Int
    ): OverlayAdjustmentResult {
        val maxHeightForWidth = maxAllowedHeightLevelForWidth(newWidthLevel)

        return if (currentHeightLevel <= maxHeightForWidth) {
            OverlayAdjustmentResult(appliedHeightLevel = currentHeightLevel)
        } else {
            val minWidthLevelNeeded = minimumWidthLevelForHeight(currentHeightLevel)
            OverlayAdjustmentResult(
                appliedHeightLevel = maxHeightForWidth,
                messageRes = R.string.settings_overlay_error_height_requires_width,
                messageArgs = listOf(minWidthLevelNeeded + 1)
            )
        }
    }

    fun recommendedCompactCommentScale(
        overlayWidth: Dp,
        verticalScale: Float
    ): Float {
        val widthFactor = when {
            overlayWidth < 176.dp -> 0.90f
            overlayWidth < 188.dp -> 0.94f
            overlayWidth < 200.dp -> 0.98f
            else -> 1.00f
        }

        val verticalFactor = verticalScale.coerceIn(0.94f, 1.04f)
        return (widthFactor * verticalFactor).coerceIn(0.88f, 1.02f)
    }
}