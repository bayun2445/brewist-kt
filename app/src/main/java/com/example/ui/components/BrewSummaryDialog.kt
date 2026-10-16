package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BrewRecipe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrewSummaryDialog(
    recipe: BrewRecipe,
    actualDurationSeconds: Int,
    onDismiss: () -> Unit,
    onSaveHistory: (beanName: String, rating: Int, notes: String) -> Unit,
    savedBeanNames: List<String> = emptyList()
) {
    var beanName by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf(5) }
    var tastingNotes by remember { mutableStateOf("") }

    val filteredBeanSuggestions = remember(beanName, savedBeanNames) {
        if (beanName.isBlank()) {
            savedBeanNames
        } else {
            savedBeanNames.filter { it.contains(beanName, ignoreCase = true) }
        }
    }

    val formattedActualTime = remember(actualDurationSeconds) {
        val mins = actualDurationSeconds / 60
        val secs = actualDurationSeconds % 60
        String.format("%02d:%02d", mins, secs)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = stringResource(R.string.dialog_summary_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.dialog_summary_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Summary metric card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.summary_coffee_dose), style = MaterialTheme.typography.labelSmall)
                            Text("${recipe.doseGrams}g", fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.summary_ratio), style = MaterialTheme.typography.labelSmall)
                            Text("1:${recipe.ratio.toInt()}", fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.summary_total_water), style = MaterialTheme.typography.labelSmall)
                            Text("${recipe.totalWaterGrams}g", fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.summary_actual_time), style = MaterialTheme.typography.labelSmall)
                            Text(formattedActualTime, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Origin / Bean Name with autocomplete dropdown
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded && filteredBeanSuggestions.isNotEmpty(),
                    onExpandedChange = { dropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = beanName,
                        onValueChange = {
                            beanName = it
                            dropdownExpanded = true
                        },
                        label = { Text(stringResource(R.string.field_bean_name)) },
                        placeholder = { Text(stringResource(R.string.field_bean_placeholder)) },
                        trailingIcon = {
                            if (savedBeanNames.isNotEmpty()) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("bean_name_input")
                    )

                    if (filteredBeanSuggestions.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.testTag("bean_name_dropdown_menu")
                        ) {
                            filteredBeanSuggestions.forEach { suggestion ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Coffee,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = suggestion,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    },
                                    onClick = {
                                        beanName = suggestion
                                        dropdownExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                    modifier = Modifier.testTag("bean_suggestion_${suggestion.lowercase().replace(" ", "_")}")
                                )
                            }
                        }
                    }
                }

                // Star Rating
                Column {
                    Text(
                        text = stringResource(R.string.field_rating),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = "$i Stars",
                                tint = if (i <= rating) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { rating = i }
                                    .padding(2.dp)
                                    .testTag("star_rating_$i")
                            )
                        }
                    }
                }

                // Tasting Notes / Flavors
                OutlinedTextField(
                    value = tastingNotes,
                    onValueChange = { tastingNotes = it },
                    label = { Text(stringResource(R.string.field_tasting_notes)) },
                    placeholder = { Text(stringResource(R.string.field_notes_placeholder)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("tasting_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveHistory(beanName.trim(), rating, tastingNotes.trim())
                    onDismiss()
                },
                modifier = Modifier.testTag("save_journal_button")
            ) {
                Text(stringResource(R.string.btn_save_journal))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("skip_save_button")
            ) {
                Text(stringResource(R.string.btn_skip_without_saving))
            }
        }
    )
}
