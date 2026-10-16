package com.example.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrewRecipe
import com.example.ui.components.BrewSummaryDialog
import com.example.ui.components.BrewTimelineBar
import com.example.ui.theme.StageBloomColor
import com.example.ui.theme.StageDrawdownColor
import com.example.ui.theme.StagePourColor

@Composable
fun BrewTimerScreen(
    recipe: BrewRecipe,
    timerState: TimerUiState,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onJumpToStage: (Int) -> Unit,
    onToggleVibration: () -> Unit,
    onToggleSound: () -> Unit,
    onTestVibration: () -> Unit,
    onSaveHistory: (String, Int, String) -> Unit,
    onDismissSummary: () -> Unit,
    onBackToSetup: () -> Unit,
    savedBeanNames: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Keep screen ON while on timer screen so barista's phone does not sleep mid-pour!
    DisposableEffect(timerState.isBrewing) {
        val window = (context as? Activity)?.window
        if (timerState.isBrewing) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val currentStage = recipe.stages.getOrNull(timerState.currentStageIndex)
    val nextStage = recipe.stages.getOrNull(timerState.currentStageIndex + 1)

    val stageCountdownSeconds = remember(currentStage, timerState.elapsedStageSeconds) {
        if (currentStage != null) {
            (currentStage.durationSeconds - timerState.elapsedStageSeconds).coerceAtLeast(0)
        } else 0
    }

    val formattedStageCountdown = remember(stageCountdownSeconds) {
        val m = stageCountdownSeconds / 60
        val s = stageCountdownSeconds % 60
        String.format("%02d:%02d", m, s)
    }

    val formattedTotalElapsed = remember(timerState.totalElapsedSeconds) {
        val m = timerState.totalElapsedSeconds / 60
        val s = timerState.totalElapsedSeconds % 60
        String.format("%02d:%02d", m, s)
    }

    val stageThemeColor = when {
        currentStage?.isBloom == true -> StageBloomColor
        currentStage?.isDrawdown == true -> StageDrawdownColor
        else -> StagePourColor
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("brew_timer_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Info & Controls Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        com.example.R.string.timer_recipe_summary_format,
                        recipe.doseGrams.toString(),
                        recipe.ratio.toInt(),
                        recipe.totalWaterGrams
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Notification Toggles & Test Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Vibration toggle
                IconButton(
                    onClick = onToggleVibration,
                    modifier = Modifier.testTag("toggle_vibration_button")
                ) {
                    Icon(
                        imageVector = if (timerState.vibrationEnabled) Icons.Default.Vibration else Icons.Default.NotificationsOff,
                        contentDescription = androidx.compose.ui.res.stringResource(com.example.R.string.vibration_notification_desc),
                        tint = if (timerState.vibrationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }

                // Sound toggle
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier.testTag("toggle_sound_button")
                ) {
                    Icon(
                        imageVector = if (timerState.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = androidx.compose.ui.res.stringResource(com.example.R.string.bell_sound_notification_desc),
                        tint = if (timerState.soundEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                    )
                }

                // Quick test haptic button
                FilledTonalButton(
                    onClick = onTestVibration,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("test_vibration_button")
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.example.R.string.btn_test_vibration), style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Horizontal Timeline Bar (Visually displaying stages from left to right!)
        BrewTimelineBar(
            stages = recipe.stages,
            currentStageIndex = timerState.currentStageIndex,
            currentStageElapsedSeconds = timerState.elapsedStageSeconds,
            totalElapsedSeconds = timerState.totalElapsedSeconds,
            onStageClick = { idx -> onJumpToStage(idx) }
        )

        // Main Digital Timer Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, stageThemeColor.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Stage countdown label
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.R.string.remaining_stage_time_label),
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.5.sp,
                    color = stageThemeColor,
                    fontWeight = FontWeight.Bold
                )

                // Large Stage Countdown
                Text(
                    text = formattedStageCountdown,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 62.sp
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("stage_countdown_display")
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Total elapsed clock pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(20.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.example.R.string.total_elapsed_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formattedTotalElapsed,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = " / ${recipe.formattedTotalTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Active Stage Barista Instructions Card
        if (currentStage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Stage title chip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = stageThemeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(
                                    com.example.R.string.stage_x_of_y,
                                    timerState.currentStageIndex + 1,
                                    recipe.stages.size
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = stageThemeColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = if (timerState.isBrewing && !timerState.isPaused) {
                                androidx.compose.ui.res.stringResource(com.example.R.string.status_in_progress)
                            } else if (timerState.isPaused) {
                                androidx.compose.ui.res.stringResource(com.example.R.string.status_paused)
                            } else {
                                androidx.compose.ui.res.stringResource(com.example.R.string.status_ready)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (timerState.isBrewing && !timerState.isPaused) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = currentStage.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )

                    // Target Water on Scale (Very prominent for barista)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.example.R.string.pour_water_heading),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "+${currentStage.waterGrams}g",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(28.dp)
                                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.example.R.string.target_scale_heading),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.example.R.string.grams_unit, currentStage.accumulatedWaterGrams),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Barista instruction text
                    if (currentStage.instruction.isNotBlank()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = stageThemeColor,
                                modifier = Modifier.size(18.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentStage.instruction,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Next Stage Preview
        if (nextStage != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.R.string.next_stage_label, nextStage.name),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.R.string.next_target_label, nextStage.accumulatedWaterGrams),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Main Timer Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Stage Button
            FilledTonalIconButton(
                onClick = {
                    if (timerState.currentStageIndex > 0) {
                        onJumpToStage(timerState.currentStageIndex - 1)
                    }
                },
                enabled = timerState.currentStageIndex > 0,
                modifier = Modifier.size(52.dp).testTag("prev_stage_button")
            ) {
                Icon(
                    Icons.Default.FastRewind,
                    contentDescription = androidx.compose.ui.res.stringResource(com.example.R.string.prev_stage_desc)
                )
            }

            // Central Play / Pause / Resume Button
            Button(
                onClick = {
                    when {
                        !timerState.isBrewing -> onStartTimer()
                        timerState.isPaused -> onResumeTimer()
                        else -> onPauseTimer()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
                    .testTag("main_timer_toggle_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (timerState.isBrewing && !timerState.isPaused) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = when {
                        !timerState.isBrewing -> Icons.Default.PlayArrow
                        timerState.isPaused -> Icons.Default.PlayArrow
                        else -> Icons.Default.Pause
                    },
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        !timerState.isBrewing -> androidx.compose.ui.res.stringResource(com.example.R.string.btn_start_brew)
                        timerState.isPaused -> androidx.compose.ui.res.stringResource(com.example.R.string.btn_resume_brew)
                        else -> androidx.compose.ui.res.stringResource(com.example.R.string.btn_pause_brew)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Next Stage Button
            FilledTonalIconButton(
                onClick = {
                    if (timerState.currentStageIndex < recipe.stages.size - 1) {
                        onJumpToStage(timerState.currentStageIndex + 1)
                    }
                },
                enabled = timerState.currentStageIndex < recipe.stages.size - 1,
                modifier = Modifier.size(52.dp).testTag("next_stage_button")
            ) {
                Icon(
                    Icons.Default.FastForward,
                    contentDescription = androidx.compose.ui.res.stringResource(com.example.R.string.next_stage_desc)
                )
            }
        }

        // Secondary Actions (Reset & Back to Setup)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onResetTimer,
                modifier = Modifier.weight(1f).testTag("reset_timer_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(androidx.compose.ui.res.stringResource(com.example.R.string.btn_reset_timer))
            }

            OutlinedButton(
                onClick = onBackToSetup,
                modifier = Modifier.weight(1f).testTag("back_to_setup_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(androidx.compose.ui.res.stringResource(com.example.R.string.btn_change_recipe))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Finished Brew Summary Dialog
    if (timerState.showSummaryDialog) {
        BrewSummaryDialog(
            recipe = recipe,
            actualDurationSeconds = timerState.totalElapsedSeconds,
            onDismiss = onDismissSummary,
            onSaveHistory = onSaveHistory,
            savedBeanNames = savedBeanNames
        )
    }
}
