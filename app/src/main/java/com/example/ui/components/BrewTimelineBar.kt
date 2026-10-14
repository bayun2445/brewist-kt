package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrewStage
import com.example.ui.theme.StageBloomColor
import com.example.ui.theme.StageDrawdownColor
import com.example.ui.theme.StageFinishedColor
import com.example.ui.theme.StagePourColor

@Composable
fun BrewTimelineBar(
    stages: List<BrewStage>,
    currentStageIndex: Int,
    currentStageElapsedSeconds: Int,
    totalElapsedSeconds: Int,
    onStageClick: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (stages.isEmpty()) return

    val totalDurationSeconds = stages.sumOf { it.durationSeconds }.coerceAtLeast(1)
    val overallProgress = (totalElapsedSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
    val animatedOverallProgress by animateFloatAsState(
        targetValue = overallProgress,
        animationSpec = tween(durationMillis = 350),
        label = "overallTimelineProgress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("brew_horizontal_timeline")
            .padding(vertical = 8.dp)
    ) {
        // Top header label: Timeline Overview
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Garis Waktu Penyeduhan (Kiri ke Kanan)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            val currentStage = stages.getOrNull(currentStageIndex)
            if (currentStage != null) {
                Text(
                    text = "${currentStageIndex + 1}/${stages.size}: ${currentStage.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Main Horizontal Segments Container
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            val totalWidth = maxWidth

            // Proportional segments laid out side by side
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                stages.forEachIndexed { index, stage ->
                    val weight = (stage.durationSeconds.toFloat() / totalDurationSeconds.toFloat())
                        .coerceAtLeast(0.05f)

                    val isCompleted = index < currentStageIndex
                    val isCurrent = index == currentStageIndex
                    val isPending = index > currentStageIndex

                    val stageProgress = if (isCompleted) {
                        1f
                    } else if (isCurrent) {
                        (currentStageElapsedSeconds.toFloat() / stage.durationSeconds.toFloat()).coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    val baseColor = when {
                        stage.isBloom -> StageBloomColor
                        stage.isDrawdown -> StageDrawdownColor
                        else -> StagePourColor
                    }

                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .fillMaxHeight()
                            .clickable(enabled = onStageClick != null) {
                                onStageClick?.invoke(index)
                            }
                            .border(
                                width = if (isCurrent) 1.5.dp else 0.5.dp,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                    ) {
                        // Background fill for progress within this segment
                        if (isCompleted) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(baseColor.copy(alpha = 0.85f))
                            )
                        } else if (isCurrent) {
                            // Partial fill
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(stageProgress)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                baseColor.copy(alpha = 0.7f),
                                                baseColor.copy(alpha = 0.95f)
                                            )
                                        )
                                    )
                            )
                        }

                        // Content inside the segment
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 2.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Icon or Stage Number
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selesai",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Text(
                                    text = if (stage.isBloom) "Bloom" else "#${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Water Target
                            Text(
                                text = "${stage.accumulatedWaterGrams}g",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted || isCurrent) Color.White else MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )

                            // Time chip
                            Text(
                                text = "${stage.durationSeconds}d",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = if (isCompleted || isCurrent) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Global moving needle / indicator
            val needleOffset = totalWidth * animatedOverallProgress - 2.dp
            Box(
                modifier = Modifier
                    .offset(x = needleOffset.coerceAtLeast(0.dp))
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.error)
                    .shadow(elevation = 3.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Time milestones beneath the bar (Left to Right timestamp timeline)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "00:00 (Mulai)",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Intermediate key time
            val midTimeSec = totalDurationSeconds / 2
            Text(
                text = String.format("%02d:%02d", midTimeSec / 60, midTimeSec % 60),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val totalMins = totalDurationSeconds / 60
            val totalSecs = totalDurationSeconds % 60
            Text(
                text = String.format("%02d:%02d (Selesai)", totalMins, totalSecs),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
