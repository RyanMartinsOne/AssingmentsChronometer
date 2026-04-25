package com.martins.assignmentschronometer.ui.screens.assignments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.martins.assignmentschronometer.R
import com.martins.assignmentschronometer.data.model.Assignment
import com.martins.assignmentschronometer.ui.components.AssignmentCard

private val defaultAssignment = listOf(
    Assignment (
        titleRes = R.string.assignment_public_talk,
        durationOnSeconds = 30 * 60,
        iconRes = R.drawable.public_talk
    ),
    Assignment (
        titleRes = R.string.assignment_watchtower,
        durationOnSeconds = 60 * 60,
        iconRes = R.drawable.watchtower
    ),
    Assignment (
        titleRes = R.string.assignment_treasures,
        durationOnSeconds = 10 * 60,
        iconRes = R.drawable.treasures
    ),
    Assignment (
        titleRes = R.string.assignment_spiritual_gems,
        durationOnSeconds = 10 * 60,
        iconRes = R.drawable.spiritual_gems,
        showCommentCount = true
    )
)

@Composable
fun AssignmentsScreen(
    assignments: List<Assignment> = defaultAssignment,
    onAssignmentClick: (Assignment) -> Unit
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(assignments, key = { it.titleRes }) { assignment ->
            AssignmentCard(
                assignment = assignment,
                onClick = { onAssignmentClick(assignment) }
            )
        }
    }
}