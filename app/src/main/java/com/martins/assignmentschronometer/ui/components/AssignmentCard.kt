package com.martins.assignmentschronometer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.martins.assignmentschronometer.R
import com.martins.assignmentschronometer.data.model.Assignment
import com.martins.assignmentschronometer.navigation.Screen
import com.martins.assignmentschronometer.viewmodel.SharedViewModel

@Composable
fun AssignmentCard(
    assignment: Assignment,
    sharedViewModel: SharedViewModel,
    navController: NavController
) {
    val googleSansBold = FontFamily(Font(R.font.googlesans_bold))

    val minutes = assignment.durationOnSeconds / 60
    val duration = if (minutes >= 60) "${minutes / 60}h" else "${minutes}min"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(assignment.titleRes),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = googleSansBold,
                            fontSize = 24.sp
                        )
                    )
                    Text(
                        text = duration,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = googleSansBold,
                            fontSize = 15.sp
                        )
                    )
                }

                Icon(
                    painter = painterResource(assignment.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    sharedViewModel.selectAssignment(assignment)
                    sharedViewModel.start()
                    navController.navigate(Screen.Home.route)
                }
            ) {
                Text(
                    text = stringResource(R.string.start),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = googleSansBold
                    )
                )
            }
        }
    }
}