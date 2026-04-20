package com.martins.assignmentschronometer.ui.screens.chronometer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.martins.assignmentschronometer.R
import java.util.Locale

@Composable
@Preview
fun ChronometerScreen(
    viewModel: ChronometerViewModel = viewModel()
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = viewModel.formattedTime,
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            if (viewModel.isRunning) viewModel.pause()
            else viewModel.start()
        }) {
            Text(text = if (viewModel.isRunning)
                stringResource(R.string.pause)
            else
                stringResource(R.string.start)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            viewModel.reset()
        }) {
            Text(text = stringResource(R.string.reset))
        }
    }

}