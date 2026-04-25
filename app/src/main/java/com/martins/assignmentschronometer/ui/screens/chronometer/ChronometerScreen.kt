package com.martins.assignmentschronometer.ui.screens.chronometer

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.martins.assignmentschronometer.R
import com.martins.assignmentschronometer.ui.components.CommentCountTag
import com.martins.assignmentschronometer.ui.theme.LocalChronometerColors
import com.martins.assignmentschronometer.viewmodel.SharedViewModel

@Composable
fun ChronometerScreen(sharedViewModel: SharedViewModel) {

    val googleSans = FontFamily(Font(R.font.googlesans_regular))
    val chronometerColors = LocalChronometerColors.current
    val selectedAssignment = sharedViewModel.selectedAssignment

    val backgroundColor by animateColorAsState(
        targetValue = if (sharedViewModel.isOverTime)
            chronometerColors.overtimeBackground
        else
            MaterialTheme.colorScheme.background,
        label = "backgroundColor"
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {

        if (selectedAssignment?.showCommentCount == true) {
            CommentCountTag(
                count = sharedViewModel.commentCount,
                modifier = Modifier
                    .align(
                        Alignment.TopCenter)
                    .padding(top = 48.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = sharedViewModel.formattedTime,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp)
            )

            Spacer(modifier = Modifier.height(80.dp))

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        sharedViewModel.isOverTime -> chronometerColors.overtimeButton
                        sharedViewModel.isRunning  -> chronometerColors.overtimeBackground
                        else                       -> MaterialTheme.colorScheme.primary
                    },
                    contentColor = when {
                        sharedViewModel.isOverTime -> chronometerColors.overtimeOnButton
                        sharedViewModel.isRunning  -> Color.White
                        else                       -> MaterialTheme.colorScheme.onPrimary
                    }
                ),
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth(0.7f),
                onClick = {
                    if (sharedViewModel.isRunning) sharedViewModel.pause()
                    else sharedViewModel.start()
                }
            ) {
                Text(
                    text = when {
                        sharedViewModel.isRunning -> stringResource(R.string.pause)
                        sharedViewModel.isPaused  -> stringResource(R.string.resume)
                        else                      -> stringResource(R.string.start)
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontFamily = googleSans
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .height(70.dp)
                    .fillMaxWidth(0.7f),
                onClick = { sharedViewModel.reset() }
            ) {
                Text(
                    text = stringResource(R.string.reset),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 20.sp,
                        fontFamily = googleSans
                    )
                )
            }
        }
    }
}