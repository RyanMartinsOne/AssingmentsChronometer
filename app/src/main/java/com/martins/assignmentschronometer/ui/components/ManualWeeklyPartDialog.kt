package com.martins.assignmentschronometer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.martins.assignmentschronometer.data.model.WeeklyPart
import com.martins.assignmentschronometer.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualWeeklyPartDialog(
    onDismiss: () -> Unit,
    onConfirm: (WeeklyPart) -> Unit,
    partToEdit: WeeklyPart? = null
) {
    val isEditing = partToEdit != null

    var partNumber by remember { mutableStateOf(partToEdit?.id ?: "") }
    var title by remember { mutableStateOf(partToEdit?.title ?: "") }
    var assignees by remember { mutableStateOf(partToEdit?.assignees ?: "") }
    var room by remember { mutableStateOf(partToEdit?.room ?: "") }
    var duration by remember { mutableStateOf(partToEdit?.durationInMinutes?.toString() ?: "") }
    var roomExpanded by remember { mutableStateOf(false) }

    val isValid = partNumber.isNotBlank() &&
            title.isNotBlank() &&
            assignees.isNotBlank() &&
            room.isNotBlank() &&
            duration.isNotBlank() &&
            duration.toIntOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Editar designação" else "Adicionar designação",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = partNumber,
                    onValueChange = { if (it.length <= 1 && it.all {
                        c -> c.isDigit() }) partNumber = it },
                    label = { Text("Nº da parte") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = assignees,
                    onValueChange = { assignees = it },
                    label = { Text("Designado(s)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                ExposedDropdownMenuBox(
                    expanded = roomExpanded,
                    onExpandedChange = { roomExpanded = it }
                ) {
                    OutlinedTextField(
                        value = room,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sala") },
                        trailingIcon = { ExposedDropdownMenuDefaults
                            .TrailingIcon(expanded = roomExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType
                                .PrimaryNotEditable,
                                enabled = true)
                    )
                    ExposedDropdownMenu(
                        expanded = roomExpanded,
                        onDismissRequest = { roomExpanded = false }
                    ) {
                        listOf("Principal", "Sala B").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    room = option
                                    roomExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duração (minutos)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        WeeklyPart(
                            id = partNumber.trim(),
                            title = title.trim(),
                            assignees = assignees.trim(),
                            room = room.trim(),
                            durationInMinutes = duration.trim().toInt(),
                            dateText = DateUtils.nextMeetingDate()
                        )
                    )
                },
                enabled = isValid
            ) {
                Text("Salvar")
            }
        }
    )
}