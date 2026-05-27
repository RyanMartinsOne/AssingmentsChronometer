package com.martins.assignmentschronometer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.martins.assignmentschronometer.R
import com.martins.assignmentschronometer.data.model.WeeklyPart

@Composable
fun WeeklyPartCard(
    part: WeeklyPart,
    onClick: () -> Unit,
    onShareClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember(part.uid) { mutableStateOf(false) }

    val isCompleted = part.realizedTimeOnSeconds != null
    val realizedTimeText = remember(part.realizedTimeOnSeconds) {
        part.realizedTimeOnSeconds?.let { totalSec ->
            val min = totalSec / 60
            val sec = totalSec % 60
            "%02d:%02d".format(min, sec)
        }
    }

    val roomLabel = remember(part.room) {
        when (part.room) {
            "Principal" -> R.string.dialog_part_main_room
            "Sala B" -> R.string.dialog_part_room_b
            else -> null
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = part.id,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = part.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = stringResource(R.string.record_part_assigned, part.assignees),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (part.room == "Sala B") {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer
                        },
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = roomLabel?.let { stringResource(it) } ?: part.room,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stringResource(
                            R.string.record_part_planned,
                            part.durationInMinutes
                        ),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                if (isCompleted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(
                                R.string.record_part_realized,
                                realizedTimeText ?: "--:--"
                            ),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (part.isDelayed) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )

                        part.delayText?.let { text ->
                            Text(
                                text = " ($text)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.more_vert),
                            contentDescription = stringResource(R.string.record_action_options),
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        if (isCompleted) {
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.record_action_share))
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.share),
                                        contentDescription = stringResource(R.string.record_action_share),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onShareClick()
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.record_action_edit))
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.edit),
                                    contentDescription = stringResource(R.string.record_action_edit),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onEditClick()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.record_action_delete),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.delete),
                                    contentDescription = stringResource(R.string.record_action_delete),
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDeleteClick()
                            }
                        )
                    }
                }

                Button(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                ) {
                    Text(
                        text = if (isCompleted) {
                            stringResource(R.string.record_action_redo)
                        } else {
                            stringResource(R.string.record_action_start)
                        }
                    )
                }
            }
        }
    }
}