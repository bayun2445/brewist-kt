package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrewMethodType
import com.example.model.BrewRecipe
import com.example.model.BrewStage
import com.example.ui.components.BrewTimelineBar
import com.example.ui.components.StageEditDialog
import com.example.ui.theme.StageBloomColor
import com.example.ui.theme.StageDrawdownColor
import com.example.ui.theme.StagePourColor

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.ui.theme.PaletteCherryMaroon
import com.example.ui.theme.PaletteIvoryWhite
import com.example.ui.theme.PaletteSlateWaterBlue
import com.example.ui.theme.PaletteWarmSand

data class RatioItem(
    val ratio: Float,
    val label: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrewSetupScreen(
    recipe: BrewRecipe,
    onUpdateDose: (Float) -> Unit,
    onUpdateRatio: (Float) -> Unit,
    onSelectMethod: (BrewMethodType) -> Unit,
    onUpdateStage: (Int, BrewStage) -> Unit,
    onAddStage: () -> Unit,
    onDeleteStage: (Int) -> Unit,
    onAutoBalanceWater: () -> Unit,
    onResetToAuto: () -> Unit,
    onStartBrewing: () -> Unit,
    onNavigateToTimer: () -> Unit = onStartBrewing,
    onOpenMethodsGuide: () -> Unit = {},
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var editingStageIndex by remember { mutableStateOf<Int?>(null) }

    val quickDoses = listOf(12f, 15f, 18f, 20f, 24f, 30f)
    val ratioDropdownItems = remember {
        listOf(
            RatioItem(12f, "1 : 12", "Sangat Pekat & Kuat (Intense)"),
            RatioItem(13f, "1 : 13", "Pekat & Tebal (Bold & Rich)"),
            RatioItem(14f, "1 : 14", "Medium-Kuat (Full-bodied)"),
            RatioItem(15f, "1 : 15", "Seimbang / Classic V60 (Rekomendasi)"),
            RatioItem(16f, "1 : 16", "Spesialti Jernih (Clean & Sweet)"),
            RatioItem(16.6f, "1 : 16.6", "Rasio Emas SCA (Golden Ratio)"),
            RatioItem(17f, "1 : 17", "Ringan & Floral (Light & Bright)"),
            RatioItem(18f, "1 : 18", "Ekstra Ringan (Delicate Tea-like)")
        )
    }

    var doseInputText by remember {
        mutableStateOf(if (recipe.doseGrams % 1f == 0f) recipe.doseGrams.toInt().toString() else recipe.doseGrams.toString())
    }

    // Synchronize doseInputText when external recipe changes occur (e.g. quick chips or stepper)
    LaunchedEffect(recipe.doseGrams) {
        val currentParsed = doseInputText.toFloatOrNull()
        if (currentParsed != recipe.doseGrams) {
            doseInputText = if (recipe.doseGrams % 1f == 0f) recipe.doseGrams.toInt().toString() else recipe.doseGrams.toString()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("brew_setup_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App header title with swatch badge & theme toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Coffee,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "V60 Coffee Timer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "Artisan Nordic Palette",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            listOf(PaletteCherryMaroon, PaletteWarmSand, PaletteIvoryWhite, PaletteSlateWaterBlue).forEach { c ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(c)
                                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                                )
                            }
                        }
                    }
                }

                FilledTonalIconButton(
                    onClick = onToggleDarkMode,
                    modifier = Modifier.testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = if (isDarkMode) "Mode Terang" else "Mode Gelap",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Method Selector Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilih Metode Penyeduhan",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FilledTonalIconButton(
                        onClick = onOpenMethodsGuide,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("button_brew_methods_info")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Penjelasan Metode Penyeduhan",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(BrewMethodType.values()) { method ->
                        val selected = recipe.methodType == method
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectMethod(method) },
                            label = {
                                Text(
                                    text = method.displayName,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (selected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("method_chip_${method.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // Dose & Ratio Control Cards
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Dosis Kopi (g) - Free Text Input (Default 15g)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Coffee,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Dosis Kopi (Coffee Grounds)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "${recipe.doseGrams} gram",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Free text input field for dose (default 15 gram)
                        OutlinedTextField(
                            value = doseInputText,
                            onValueChange = { newText ->
                                val sanitized = newText.replace(',', '.')
                                if (sanitized.isEmpty() || sanitized.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    doseInputText = sanitized
                                    val parsed = sanitized.toFloatOrNull()
                                    if (parsed != null && parsed > 0f) {
                                        onUpdateDose(parsed)
                                    }
                                }
                            },
                            label = { Text("Ketik Dosis Kopi Bebas (Free Text)") },
                            placeholder = { Text("15") },
                            suffix = {
                                Text(
                                    "gram",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Coffee,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dose_input_field"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stepper + / - & Quick dose chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalIconButton(
                                onClick = { onUpdateDose((recipe.doseGrams - 1f).coerceAtLeast(1f)) },
                                modifier = Modifier.testTag("dose_minus_button")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Kurang 1g")
                            }

                            // Quick dose chips
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                quickDoses.forEach { d ->
                                    val isSelected = recipe.doseGrams == d
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .clickable { onUpdateDose(d) }
                                            .border(
                                                1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        Text(
                                            text = "${d.toInt()}g",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            FilledTonalIconButton(
                                onClick = { onUpdateDose(recipe.doseGrams + 1f) },
                                modifier = Modifier.testTag("dose_plus_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah 1g")
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Rasio Penyeduhan (Dropdown 1:12 sampai 1:18)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Rasio Penyeduhan (Brew Ratio)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "1 : ${if (recipe.ratio % 1f == 0f) recipe.ratio.toInt() else recipe.ratio}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Ratio Dropdown Selection (1:12 sampai 1:18)
                        var ratioDropdownExpanded by remember { mutableStateOf(false) }
                        val currentRatioFormatted = if (recipe.ratio % 1f == 0f) "1 : ${recipe.ratio.toInt()}" else "1 : ${recipe.ratio}"
                        val currentOption = ratioDropdownItems.find { kotlin.math.abs(it.ratio - recipe.ratio) < 0.05f }
                        val ratioDisplayText = if (currentOption != null) {
                            "${currentOption.label} • ${currentOption.description}"
                        } else {
                            currentRatioFormatted
                        }

                        ExposedDropdownMenuBox(
                            expanded = ratioDropdownExpanded,
                            onExpandedChange = { ratioDropdownExpanded = !ratioDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = ratioDisplayText,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pilihan Rasio Seduh (1:12 sampai 1:18)") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = ratioDropdownExpanded)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.WaterDrop,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                                    .testTag("ratio_dropdown_field"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = ratioDropdownExpanded,
                                onDismissRequest = { ratioDropdownExpanded = false }
                            ) {
                                ratioDropdownItems.forEach { opt ->
                                    val isSelected = kotlin.math.abs(opt.ratio - recipe.ratio) < 0.05f
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = opt.label,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = opt.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        trailingIcon = if (isSelected) {
                                            {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        } else null,
                                        onClick = {
                                            onUpdateRatio(opt.ratio)
                                            ratioDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("ratio_option_${(opt.ratio * 10).toInt()}")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stepper + / - & Quick ratio chips (1:12 - 1:18)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalIconButton(
                                onClick = { onUpdateRatio((recipe.ratio - 1f).coerceAtLeast(10f)) },
                                modifier = Modifier.testTag("ratio_minus_button")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Kurang rasio")
                            }

                            // Quick ratio chips
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val ratioChips = listOf(12f, 13f, 14f, 15f, 16f, 17f, 18f)
                                ratioChips.forEach { r ->
                                    val isSelected = kotlin.math.abs(recipe.ratio - r) < 0.05f
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .clickable { onUpdateRatio(r) }
                                            .border(
                                                1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        Text(
                                            text = "1:${r.toInt()}",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            FilledTonalIconButton(
                                onClick = { onUpdateRatio((recipe.ratio + 1f).coerceAtMost(25f)) },
                                modifier = Modifier.testTag("ratio_plus_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah rasio")
                            }
                        }
                    }
                }
            }
        }

        // Summary Target Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TOTAL AIR",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${recipe.totalWaterGrams} g / ml",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TOTAL WAKTU",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = recipe.formattedTotalTime,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TAHAPAN",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${recipe.stages.size} Tahap",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Tombol langsung di bawah Card Hasil Kalkulasi untuk masuk ke tab timer seduh
        item {
            Button(
                onClick = onNavigateToTimer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_go_to_timer_under_calc"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Masuk ke Timer Seduh (${recipe.totalWaterGrams}g Air)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Horizontal Timeline Visualizer Preview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    BrewTimelineBar(
                        stages = recipe.stages,
                        currentStageIndex = 0,
                        currentStageElapsedSeconds = 0,
                        totalElapsedSeconds = 0,
                        onStageClick = { idx -> editingStageIndex = idx }
                    )
                }
            }
        }

        // Stage list section header with helper actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rincian Tahapan Seduh",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ketuk tahap untuk mengubah durasi & volume air",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledTonalIconButton(
                        onClick = onAutoBalanceWater,
                        modifier = Modifier.testTag("auto_balance_water_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Ratakan Sisa Air",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    FilledTonalIconButton(
                        onClick = onResetToAuto,
                        modifier = Modifier.testTag("reset_to_auto_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Otomatis"
                        )
                    }
                }
            }
        }

        // Each individual Stage Card
        itemsIndexed(recipe.stages) { index, stage ->
            val badgeColor = when {
                stage.isBloom -> StageBloomColor
                stage.isDrawdown -> StageDrawdownColor
                else -> StagePourColor
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { editingStageIndex = index }
                    .testTag("stage_card_$index"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Badge chip
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (stage.isBloom) "B" else "${index + 1}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = stage.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (stage.instruction.isNotBlank()) {
                                Text(
                                    text = stage.instruction,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            Row(
                                modifier = Modifier.padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Tuang: +${stage.waterGrams}g (Target: ${stage.accumulatedWaterGrams}g)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "• Durasi: ${stage.durationSeconds}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { editingStageIndex = index },
                        modifier = Modifier.testTag("edit_stage_${index}_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Tahap",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Add Stage Button
        item {
            OutlinedButton(
                onClick = onAddStage,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_stage_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Tahapan Seduh")
            }
        }

        // Big Primary Button to Start Brewing
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onStartBrewing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_brewing_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buka Timer & Mulai Seduh",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Modal Editor Dialog
    editingStageIndex?.let { idx ->
        val currentStage = recipe.stages.getOrNull(idx)
        if (currentStage != null) {
            StageEditDialog(
                stage = currentStage,
                stageIndex = idx,
                canDelete = recipe.stages.size > 1,
                onDismiss = { editingStageIndex = null },
                onSave = { updated ->
                    onUpdateStage(idx, updated)
                    editingStageIndex = null
                },
                onDelete = {
                    onDeleteStage(idx)
                    editingStageIndex = null
                }
            )
        }
    }
}
