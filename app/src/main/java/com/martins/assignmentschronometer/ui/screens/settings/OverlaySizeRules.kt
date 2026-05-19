package com.martins.assignmentschronometer.ui.screens.settings

data class OverlayAdjustmentResult(
    val appliedHeightLevel: Int,
    val message: String?
)

object OverlaySizeRules {

    val widthLevels: List<Float> = listOf(
        0.86f, 0.89f, 0.92f, 0.95f, 0.98f, 1.01f, 1.04f, 1.07f
    )

    val heightLevels: List<Float> = listOf(
        0.91f, 0.94f, 0.96f, 0.99f, 1.04f, 1.09f, 1.011f, 1.15f
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
            OverlayAdjustmentResult(
                appliedHeightLevel = requestedHeightLevel,
                message = null
            )
        } else {
            val fallbackHeightLevel = maxAllowedHeightLevelForWidth(currentWidthLevel)
            OverlayAdjustmentResult(
                appliedHeightLevel = fallbackHeightLevel,
                message = requiredWidthMessageForHeight(requestedHeightLevel)
            )
        }
    }

    fun adjustHeightForNewWidth(
        currentHeightLevel: Int,
        newWidthLevel: Int
    ): OverlayAdjustmentResult {
        val maxHeightForWidth = maxAllowedHeightLevelForWidth(newWidthLevel)

        return if (currentHeightLevel <= maxHeightForWidth) {
            OverlayAdjustmentResult(
                appliedHeightLevel = currentHeightLevel,
                message = null
            )
        } else {
            OverlayAdjustmentResult(
                appliedHeightLevel = maxHeightForWidth,
                message = requiredWidthMessageForHeight(currentHeightLevel)
            )
        }
    }

    fun requiredWidthMessageForHeight(heightLevel: Int): String? {
        return when {
            heightLevel >= 7 -> "Aumente o width para o nível 5 ou mais para usar esse height."
            heightLevel >= 6 -> "Aumente o width para o nível 4 ou mais para usar esse height."
            heightLevel >= 5 -> "Aumente o width para o nível 3 ou mais para usar esse height."
            else -> null
        }
    }
}