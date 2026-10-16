package com.example.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.BrewHistoryEntity
import com.example.ui.components.BrewDetailDialog
import com.example.ui.components.EditJournalDialog
import com.example.ui.components.RenameBeanDialog
import com.google.firebase.auth.FirebaseUser
import java.util.Locale

data class BeanGroup(
    val beanKey: String,
    val displayBeanName: String,
    val brews: List<BrewHistoryEntity>,
    val latestBrewTimestamp: Long,
    val averageRating: Float
)

@Composable
fun BrewHistoryScreen(
    historyList: List<BrewHistoryEntity>,
    currentUser: FirebaseUser?,
    savedBeanNames: List<String> = emptyList(),
    onGoogleSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteBrew: (BrewHistoryEntity) -> Unit,
    onUpdateJournal: ((BrewHistoryEntity, String, Int, String, Boolean) -> Unit)? = null,
    onRenameBean: ((String, String) -> Unit)? = null,
    onUseRecipe: (BrewHistoryEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedBrewForDetail by remember { mutableStateOf<BrewHistoryEntity?>(null) }
    var editingBrew by remember { mutableStateOf<BrewHistoryEntity?>(null) }
    var renamingBeanKey by remember { mutableStateOf<String?>(null) }
    var expandedBeans by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedBeanKeyForDetail by remember { mutableStateOf<String?>(null) }

    val unnamedLabel = stringResource(R.string.detail_bean_unnamed)
    val beanGroups = remember(historyList, unnamedLabel) {
        historyList.groupBy { it.beanName.trim() }
            .map { (rawName, brews) ->
                val displayName = if (rawName.isBlank()) unnamedLabel else rawName
                val sortedBrews = brews.sortedByDescending { it.timestamp }
                val avgRating = if (brews.isNotEmpty()) brews.map { it.rating }.average().toFloat() else 0f
                BeanGroup(
                    beanKey = rawName,
                    displayBeanName = displayName,
                    brews = sortedBrews,
                    latestBrewTimestamp = sortedBrews.firstOrNull()?.timestamp ?: 0L,
                    averageRating = avgRating
                )
            }
            .sortedByDescending { it.latestBrewTimestamp }
    }

    // If viewing full history page for a specific bean, find active group
    val activeDetailGroup = selectedBeanKeyForDetail?.let { key ->
        beanGroups.find { it.beanKey == key }
    }

    if (activeDetailGroup != null) {
        BeanHistoryDetailScreen(
            group = activeDetailGroup,
            savedBeanNames = savedBeanNames,
            onBack = { selectedBeanKeyForDetail = null },
            onDeleteBrew = onDeleteBrew,
            onUseRecipe = onUseRecipe,
            onUpdateJournal = { brew, newBean, rating, notes, applyToAll ->
                onUpdateJournal?.invoke(brew, newBean, rating, notes, applyToAll)
            },
            onRenameBean = { oldName, newName ->
                onRenameBean?.invoke(oldName, newName)
                selectedBeanKeyForDetail = newName
            },
            modifier = modifier
        )
        return
    }

    // Auto-expand the first bean card so user sees history immediately
    LaunchedEffect(beanGroups) {
        if (expandedBeans.isEmpty() && beanGroups.isNotEmpty()) {
            expandedBeans = setOf(beanGroups.first().beanKey)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Coffee,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.journal_screen_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Dedicated Firebase Cloud Sync Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cloud_sync_card"),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentUser != null) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    }
                ),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.dp,
                    if (currentUser != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (currentUser != null) Icons.Default.CloudDone else Icons.Default.Cloud,
                                contentDescription = null,
                                tint = if (currentUser != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentUser != null) stringResource(R.string.cloud_sync_active_title) else stringResource(R.string.cloud_sync_inactive_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (currentUser != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (currentUser != null) {
                            FilledTonalButton(
                                onClick = onSignOut,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("sign_out_button")
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.btn_sign_out), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    if (currentUser != null) {
                        Text(
                            text = "${currentUser.email ?: currentUser.displayName ?: "Google Account"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.cloud_sync_active_desc),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.cloud_sync_inactive_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Button(
                            onClick = onGoogleSignIn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("google_sign_in_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.btn_sign_in_google), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Empty state placeholder
        if (historyList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp)
                        .testTag("empty_history_view"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = stringResource(R.string.journal_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.journal_empty_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // List of Grouped Bean Cards (Expand / Collapse)
        items(beanGroups, key = { it.beanKey }) { group ->
            val isExpanded = expandedBeans.contains(group.beanKey)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bean_card_${group.beanKey.ifBlank { "unnamed" }.lowercase().replace(" ", "_")}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                    // Bean Card Header (clickable to expand / collapse)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedBeans = if (isExpanded) {
                                    expandedBeans - group.beanKey
                                } else {
                                    expandedBeans + group.beanKey
                                }
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Bean Icon Indicator
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isExpanded) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Coffee,
                                contentDescription = null,
                                tint = if (isExpanded) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Bean Title & Subtitle (Brews count & Latest date)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = group.displayBeanName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val brewCountText = if (group.brews.size == 1) {
                                    stringResource(R.string.bean_card_single_brew)
                                } else {
                                    stringResource(R.string.bean_card_brews_count, group.brews.size)
                                }
                                Text(
                                    text = brewCountText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text(
                                    text = stringResource(R.string.bean_card_latest_brew, group.brews.first().formattedDate),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Rating pill, Rename button, and Expand/Collapse Icon
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (group.averageRating > 0f) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFB300).copy(alpha = 0.15f),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFB300),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = String.format(Locale.getDefault(), "%.1f", group.averageRating),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Quick Rename Bean Button
                            if (onRenameBean != null) {
                                IconButton(
                                    onClick = { renamingBeanKey = group.beanKey },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("rename_bean_${group.beanKey.ifBlank { "unnamed" }.lowercase().replace(" ", "_")}_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.btn_rename_bean),
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    expandedBeans = if (isExpanded) {
                                        expandedBeans - group.beanKey
                                    } else {
                                        expandedBeans + group.beanKey
                                    }
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("expand_toggle_${group.beanKey.ifBlank { "unnamed" }.lowercase().replace(" ", "_")}")
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Expanded Section: List of historical timer brews for this bean (Limited to 3)
                    if (isExpanded) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            thickness = 0.8.dp
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = stringResource(R.string.bean_historical_timers_title),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (group.brews.size > 3) {
                                    Text(
                                        text = "3 / ${group.brews.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Historical timer list items - limited to max 3
                            val displayedBrews = group.brews.take(3)
                            displayedBrews.forEach { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedBrewForDetail = item }
                                        .testTag("history_card_${item.id}"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        width = 0.8.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Recipe name, time & delete button
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.recipeName,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = item.formattedDate,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (onUpdateJournal != null) {
                                                    IconButton(
                                                        onClick = { editingBrew = item },
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .testTag("edit_brew_${item.id}_button")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = stringResource(R.string.btn_edit_journal),
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                    }
                                                }

                                                IconButton(
                                                    onClick = { onDeleteBrew(item) },
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .testTag("delete_brew_${item.id}_button")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = stringResource(R.string.delete_brew_desc),
                                                        tint = MaterialTheme.colorScheme.outline,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Rating Stars
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            for (i in 1..5) {
                                                Icon(
                                                    imageVector = if (i <= item.rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                                                    contentDescription = null,
                                                    tint = if (i <= item.rating) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        // Metrics Pill
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Text(
                                                    text = "${stringResource(R.string.detail_param_dose)}: ${item.doseGrams}g",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "${stringResource(R.string.detail_param_ratio)}: 1:${item.ratio.toInt()}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "${stringResource(R.string.detail_param_water)}: ${item.totalWaterGrams}g",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "${stringResource(R.string.detail_param_time)}: ${item.formattedDuration}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        // Tasting Notes snippet if present
                                        if (item.tastingNotes.isNotBlank()) {
                                            Text(
                                                text = "${stringResource(R.string.label_tasting_notes)} \"${item.tastingNotes}\"",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Card Action Buttons: Open Journal Detail & Quick Use Timer
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                TextButton(
                                                    onClick = { selectedBrewForDetail = item },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.testTag("view_detail_${item.id}_button")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = stringResource(R.string.btn_open_journal),
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }

                                                if (onUpdateJournal != null) {
                                                    TextButton(
                                                        onClick = { editingBrew = item },
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.testTag("edit_card_journal_${item.id}_button")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = stringResource(R.string.btn_edit_journal),
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }
                                            }

                                            FilledTonalButton(
                                                onClick = { onUseRecipe(item) },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                modifier = Modifier
                                                    .height(30.dp)
                                                    .testTag("use_timer_${item.id}_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayCircle,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = stringResource(R.string.btn_use_timer_quick),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // If more than 3 historical brews exist, show button to navigate to dedicated page
                            if (group.brews.size > 3) {
                                OutlinedButton(
                                    onClick = { selectedBeanKeyForDetail = group.beanKey },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp)
                                        .testTag("view_all_brews_${group.beanKey.ifBlank { "unnamed" }.lowercase().replace(" ", "_")}_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.btn_view_all_brews, group.brews.size),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Popup Card Detail: Opened from individual historical timer item
    selectedBrewForDetail?.let { brew ->
        BrewDetailDialog(
            item = brew,
            onDismiss = { selectedBrewForDetail = null },
            onUseTimerAgain = {
                onUseRecipe(it)
                selectedBrewForDetail = null
            },
            onEditJournal = {
                editingBrew = it
                selectedBrewForDetail = null
            }
        )
    }

    // Edit Journal Dialog: Allows editing bean name, rating, notes & propagating changes
    editingBrew?.let { brew ->
        EditJournalDialog(
            item = brew,
            savedBeanNames = savedBeanNames,
            onDismiss = { editingBrew = null },
            onSave = { newBean, rating, notes, applyToAll ->
                onUpdateJournal?.invoke(brew, newBean, rating, notes, applyToAll)
                editingBrew = null
            }
        )
    }

    // Quick Rename Bean Dialog
    renamingBeanKey?.let { beanKey ->
        RenameBeanDialog(
            currentName = beanKey,
            onDismiss = { renamingBeanKey = null },
            onConfirm = { newName ->
                onRenameBean?.invoke(beanKey, newName)
                renamingBeanKey = null
            }
        )
    }
}
