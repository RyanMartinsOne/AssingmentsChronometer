package com.martins.assignmentschronometer.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Assignment (
    @param:StringRes val titleRes: Int,
    val durationOnSeconds: Int,
    @param:DrawableRes val iconRes: Int
)