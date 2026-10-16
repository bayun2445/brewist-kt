package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrewStage

@Composable
fun StageEditDialog(
    stage: BrewStage,
    stageIndex: Int,
    canDelete: Boolean,
    onDismiss: () -> Unit,
    onSave: (BrewStage) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(stage.name) }
    var waterGrams by remember { mutableStateOf(stage.waterGrams) }
    var durationSeconds by remember { mutableStateOf(stage.durationSeconds) }
    var instruction by remember { mutableStateOf(stage.instruction) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.R.string.dialog_edit_stage_title, stageIndex + 1),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (canDelete) {
                    FilledTonalIconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_stage_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = androidx.compose.ui.res.stringResource(com.example.R.string.delete_stage_desc),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nama Tahap
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(androidx.compose.ui.res.stringResource(com.example.R.string.field_stage_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("stage_name_input")
                )

                // Volume Air (g / ml)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.R.string.field_water_volume),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = { waterGrams = (waterGrams - 5).coerceAtLeast(0) },
                            modifier = Modifier.testTag("water_minus_button")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "-5g")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = "${waterGrams}g",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        FilledTonalIconButton(
                            onClick = { waterGrams += 5 },
                            modifier = Modifier.testTag("water_plus_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "+5g")
                        }
                    }
                }

                // Durasi Waktu (detik)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.R.string.field_stage_duration),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = { durationSeconds = (durationSeconds - 5).coerceAtLeast(10) },
                            modifier = Modifier.testTag("duration_minus_button")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "-5s")
                        }

                        val secUnit = androidx.compose.ui.res.stringResource(com.example.R.string.unit_seconds_short)
                        Text(
                            text = "${durationSeconds}$secUnit (${String.format("%02d:%02d", durationSeconds / 60, durationSeconds % 60)})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        FilledTonalIconButton(
                            onClick = { durationSeconds += 5 },
                            modifier = Modifier.testTag("duration_plus_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "+5s")
                        }
                    }
                }

                // Petunjuk Seduh
                OutlinedTextField(
                    value = instruction,
                    onValueChange = { instruction = it },
                    label = { Text(androidx.compose.ui.res.stringResource(com.example.R.string.field_barista_instruction)) },
                    placeholder = { Text(androidx.compose.ui.res.stringResource(com.example.R.string.instruction_placeholder)) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("stage_instruction_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        stage.copy(
                            name = name.ifBlank { "Stage #${stageIndex + 1}" },
                            waterGrams = waterGrams,
                            durationSeconds = durationSeconds,
                            instruction = instruction
                        )
                    )
                },
                modifier = Modifier.testTag("save_stage_button")
            ) {
                Text(androidx.compose.ui.res.stringResource(com.example.R.string.btn_save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_stage_button")
            ) {
                Text(androidx.compose.ui.res.stringResource(com.example.R.string.btn_cancel))
            }
        }
    )
}
