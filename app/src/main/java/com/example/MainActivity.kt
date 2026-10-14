package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.BrewHistoryScreen
import com.example.ui.BrewSetupScreen
import com.example.ui.BrewTimerScreen
import com.example.ui.BrewViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: BrewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: BrewViewModel) {
    val recipe by viewModel.recipe.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val brewHistory by viewModel.brewHistory.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    // If timer begins brewing, automatically switch to Timer screen
    // or barista can switch back and forth
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(modifier = Modifier.testTag("main_navigation_bar")) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "Atur Resep") },
                    label = { Text("Atur Resep") },
                    modifier = Modifier.testTag("nav_item_setup")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        if (timerState.isBrewing && !timerState.isPaused) {
                            BadgedBox(
                                badge = { Badge() }
                            ) {
                                Icon(Icons.Default.PlayCircle, contentDescription = "Timer Seduh")
                            }
                        } else {
                            Icon(Icons.Default.PlayCircle, contentDescription = "Timer Seduh")
                        }
                    },
                    label = { Text("Timer Seduh") },
                    modifier = Modifier.testTag("nav_item_timer")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Jurnal") },
                    label = { Text("Jurnal") },
                    modifier = Modifier.testTag("nav_item_history")
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tabContentTransition",
            modifier = Modifier.padding(innerPadding)
        ) { tab ->
            when (tab) {
                0 -> BrewSetupScreen(
                    recipe = recipe,
                    onUpdateDose = { viewModel.updateDose(it) },
                    onUpdateRatio = { viewModel.updateRatio(it) },
                    onSelectMethod = { viewModel.selectMethodType(it) },
                    onUpdateStage = { idx, stage -> viewModel.updateStage(idx, stage) },
                    onAddStage = { viewModel.addStage() },
                    onDeleteStage = { viewModel.deleteStage(it) },
                    onAutoBalanceWater = { viewModel.autoBalanceWater() },
                    onResetToAuto = { viewModel.resetToAutoStages() },
                    onStartBrewing = {
                        selectedTab = 1
                        viewModel.startTimer()
                    }
                )

                1 -> BrewTimerScreen(
                    recipe = recipe,
                    timerState = timerState,
                    onStartTimer = { viewModel.startTimer() },
                    onPauseTimer = { viewModel.pauseTimer() },
                    onResumeTimer = { viewModel.resumeTimer() },
                    onResetTimer = { viewModel.resetTimer() },
                    onJumpToStage = { viewModel.jumpToStage(it) },
                    onToggleVibration = { viewModel.toggleVibration() },
                    onToggleSound = { viewModel.toggleSound() },
                    onTestVibration = { viewModel.testVibration() },
                    onSaveHistory = { beanName, rating, notes ->
                        viewModel.saveBrewHistory(beanName, rating, notes)
                    },
                    onDismissSummary = { viewModel.dismissSummaryDialog() },
                    onBackToSetup = { selectedTab = 0 }
                )

                2 -> BrewHistoryScreen(
                    historyList = brewHistory,
                    onDeleteBrew = { viewModel.deleteBrewHistory(it) }
                )
            }
        }
    }
}
