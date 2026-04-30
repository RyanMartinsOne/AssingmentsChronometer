package com.martins.assignmentschronometer.ui.screens.record

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.martins.assignmentschronometer.R
import com.martins.assignmentschronometer.ui.components.MenuOption
import com.martins.assignmentschronometer.ui.components.WeeklyPartCard
import com.martins.assignmentschronometer.viewmodel.SharedViewModel
import com.martins.assignmentschronometer.viewmodel.WeeklyPartsViewModel
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecordScreen(
    viewModel: WeeklyPartsViewModel,
    sharedViewModel: SharedViewModel,
    onNavigateToChronometer: () -> Unit
) {
    val context = LocalContext.current
    var isMenuExpanded by remember { mutableStateOf(false) }


    val imageFile = remember { File(context.cacheDir, "camera_capture.jpg") }
    val imageUri: Uri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    }


    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) viewModel.processImageOcr(InputImage.fromFilePath(context, imageUri))
    }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val mimeType = context.contentResolver.getType(it)
            if (mimeType == "application/pdf") {
                viewModel.processPdfUri(it)   //
            } else {
                viewModel.processImageOcr(InputImage.fromFilePath(context, it))
            }
        }
    }

    val cornerPercent by animateIntAsState(
        targetValue = if (isMenuExpanded) 50 else 28,
        label = "shapeAnimation"
    )
    val rotation by animateFloatAsState(
        targetValue = if (isMenuExpanded) 45f else 0f,
        label = "rotationAnimation"
    )

    val revealProgress by animateFloatAsState(
        targetValue = if (isMenuExpanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = 800f
        ),
        label = "reveal"
    )

    val shareText = viewModel.shareText

    LaunchedEffect(shareText) {
        shareText?.let { text ->
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, text)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(
                android.content.Intent.createChooser(intent, "Compartilhar com...")
            )
            viewModel.onShareHandled()
        }
    }

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                if (revealProgress > 0f) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        MenuOption(
                            "Fotografar programa",
                            R.drawable.camera,
                            progress = revealProgress)
                        {
                            cameraLauncher.launch(imageUri)
                            isMenuExpanded = false
                        }
                        MenuOption(
                            "Importar PDF ou imagem",
                            R.drawable.pdf,
                            progress = revealProgress)
                        {
                            fileLauncher.launch("*/*")
                            isMenuExpanded = false
                        }
                    }
                }

                LargeFloatingActionButton(
                    onClick = { isMenuExpanded = !isMenuExpanded },
                      shape = RoundedCornerShape(cornerPercent),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.add),
                        contentDescription = "Menu",
                        modifier = Modifier
                            .size(36.dp)
                            .rotate(rotation)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)

                .then(
                    if (isMenuExpanded) {
                        Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { isMenuExpanded = false }
                    } else Modifier
                )
        ) {

            if (isMenuExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { isMenuExpanded = false }
                )
            }

            if (viewModel.weeklyParts.isEmpty()) {
                Text(
                    text = "Importe o programa da semana usando o botão +",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp)
                )
            } else {

                val groupedParts = viewModel.groupedWeeklyParts

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    groupedParts.forEach { (date, partsForWeek) ->
                        stickyHeader {
                            Surface(
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = date,
                                    style = MaterialTheme.typography.titleLarge
                                        .copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    )
                                )
                            }
                        }

                        items(partsForWeek) { part ->
                            WeeklyPartCard(
                                part = part,
                                onClick = {
                                    sharedViewModel.selectPartForTiming(part)
                                    onNavigateToChronometer()
                                },
                                onShareClick = {
                                    viewModel.requestShare(part)
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}