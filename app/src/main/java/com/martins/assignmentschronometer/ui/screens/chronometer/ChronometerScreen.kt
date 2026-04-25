package com.martins.assignmentschronometer.ui.screens.chronometer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.martins.assignmentschronometer.R
import com.martins.assignmentschronometer.viewmodel.SharedViewModel

@Composable
fun ChronometerScreen(
    sharedViewModel: SharedViewModel
) {

    val googleSans = FontFamily(
        Font(R.font.googlesans_regular)
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = sharedViewModel.formattedTime,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 72.sp
            )
        )

        Spacer(modifier = Modifier.height(80.dp))

        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = if (sharedViewModel.isRunning) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            ),

            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth(0.7f),

            onClick = {
            if (sharedViewModel.isRunning) sharedViewModel.pause()
            else sharedViewModel.start()
        }) {
            Text(
                text = when {
                sharedViewModel.isRunning -> stringResource(R.string.pause)
                sharedViewModel.isPaused -> stringResource(R.string.resume)
                else -> stringResource(R.string.start)
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
            onClick = {
            sharedViewModel.reset()
        }) {
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