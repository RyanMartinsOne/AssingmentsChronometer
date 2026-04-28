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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.martins.assignmentschronometer.R
import com.martins.assignmentschronometer.ui.components.CommentCountTag
import com.martins.assignmentschronometer.ui.theme.LocalChronometerColors
import com.martins.assignmentschronometer.viewmodel.SharedViewModel

private val GoogleSans = FontFamily(Font(R.font.googlesans_regular))

@Composable
fun ChronometerScreen(sharedViewModel: SharedViewModel) {
    val chronometerColors = LocalChronometerColors.current
    var showSaveDialog by remember { mutableStateOf(false) }
    val activePart = sharedViewModel.activePart

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

        if (activePart != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = activePart.title,
                    style = MaterialTheme.typography.headlineSmall
                        .copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = activePart.assignees,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (sharedViewModel.selectedAssignment?.showCommentCount == true) {
            CommentCountTag(
                count = sharedViewModel.commentCount,
                modifier = Modifier
                    .align(Alignment.TopCenter)
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
                        sharedViewModel.isRunning  -> chronometerColors.overtimeButton
                        else                       -> MaterialTheme.colorScheme.onPrimary
                    }
                ),
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth(0.7f),
                onClick = {
                    if (sharedViewModel.isRunning) {
                        sharedViewModel.pause()
                        if (activePart != null) showSaveDialog = true
                    } else {
                        sharedViewModel.start()
                    }
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
                        fontFamily = GoogleSans
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
                    if (activePart != null && (sharedViewModel.isRunning || sharedViewModel.isPaused)) {
                        showSaveDialog = true
                    } else {
                        sharedViewModel.reset()
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.reset),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 20.sp,
                        fontFamily = GoogleSans
                    )
                )
            }
        }

        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        activePart?.let { sharedViewModel.finishPartAndSaveTime(it.id) }
                        showSaveDialog = false
                    }) {
                        Text("Salvar e Finalizar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveDialog = false }) {
                        Text("Apenas Pausar")
                    }
                },
                title = { Text("Registrar Tempo") },
                text = {
                    Text(
                        "Deseja salvar o tempo de ${sharedViewModel.formattedTime} " +
                                "para a designação de ${activePart?.assignees}?"
                    )
                }
            )
        }
    }
}