package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BrewDatabase
import com.example.data.BrewHistoryEntity
import com.example.model.BrewMethodType
import com.example.model.BrewRecipe
import com.example.model.BrewStage
import com.example.util.BrewCalculator
import com.example.util.BrewNotifier
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.example.data.FirestoreBrewRepository
import com.example.model.BrewRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf

data class TimerUiState(
    val isBrewing: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val currentStageIndex: Int = 0,
    val elapsedStageSeconds: Int = 0,
    val totalElapsedSeconds: Int = 0,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val showSummaryDialog: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class BrewViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BrewDatabase.getDatabase(application)
    private val dao = db.brewDao()
    private val notifier = BrewNotifier(application)
    private val firestoreRepo = FirestoreBrewRepository(application)
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        _currentUser.value = auth.currentUser
    }

    init {
        firebaseAuth.addAuthStateListener(authListener)
    }

    val brewHistory: StateFlow<List<BrewHistoryEntity>> = dao.getAllBrews()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val cloudBrews: StateFlow<List<BrewRecord>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            firestoreRepo.observeUserBrews()
                .catch { emit(emptyList<BrewRecord>()) }
        } else {
            flowOf(emptyList<BrewRecord>())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Initial default recipe: Kasuya 4:6 with 15g coffee, 1:15 ratio = 225g water
    private val _recipe = MutableStateFlow(
        createDefaultRecipe(
            methodType = BrewMethodType.KASUYA_4_6,
            dose = 15f,
            ratio = 15f
        )
    )
    val recipe: StateFlow<BrewRecipe> = _recipe.asStateFlow()

    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.update { !it }
    }

    private var timerJob: Job? = null

    companion object {
        fun createDefaultRecipe(
            methodType: BrewMethodType,
            dose: Float,
            ratio: Float
        ): BrewRecipe {
            val stages = BrewCalculator.generateStages(methodType, dose, ratio)
            return BrewRecipe(
                id = "recipe_${System.currentTimeMillis()}",
                name = methodType.displayName,
                methodType = methodType,
                description = methodType.shortDesc,
                doseGrams = dose,
                ratio = ratio,
                stages = stages
            )
        }
    }

    /**
     * Updates Dose (grams) and recalculates all stages automatically
     */
    fun updateDose(newDose: Float) {
        val clampedDose = newDose.coerceIn(1f, 100f)
        _recipe.update { current ->
            val updatedStages = BrewCalculator.generateStages(current.methodType, clampedDose, current.ratio)
            current.copy(doseGrams = clampedDose, stages = updatedStages)
        }
        if (!_timerState.value.isBrewing) {
            resetTimer()
        }
    }

    /**
     * Updates Ratio (1:X) and recalculates all stages automatically
     */
    fun updateRatio(newRatio: Float) {
        val clampedRatio = newRatio.coerceIn(10f, 25f)
        _recipe.update { current ->
            val updatedStages = BrewCalculator.generateStages(current.methodType, current.doseGrams, clampedRatio)
            current.copy(ratio = clampedRatio, stages = updatedStages)
        }
        if (!_timerState.value.isBrewing) {
            resetTimer()
        }
    }

    /**
     * Changes brewing method preset
     */
    fun selectMethodType(methodType: BrewMethodType) {
        _recipe.update { current ->
            val updatedStages = BrewCalculator.generateStages(methodType, current.doseGrams, current.ratio)
            current.copy(
                name = methodType.displayName,
                methodType = methodType,
                description = methodType.shortDesc,
                stages = updatedStages
            )
        }
        resetTimer()
    }

    /**
     * Manually modifies a specific stage's duration, water volume, and instruction
     */
    fun updateStage(index: Int, modifiedStage: BrewStage) {
        _recipe.update { current ->
            val list = current.stages.toMutableList()
            if (index in list.indices) {
                list[index] = modifiedStage
                val recalculated = BrewCalculator.recalculateAccumulatedWater(list)
                current.copy(stages = recalculated)
            } else {
                current
            }
        }
    }

    /**
     * Adds a new stage manually
     */
    fun addStage() {
        _recipe.update { current ->
            val list = current.stages.toMutableList()
            val newStage = BrewStage(
                name = "Penuangan Tambahan #${list.size + 1}",
                waterGrams = 40,
                durationSeconds = 30,
                instruction = "Penuangan tambahan untuk memperpanjang ekstraksi."
            )
            list.add(newStage)
            val recalculated = BrewCalculator.recalculateAccumulatedWater(list)
            current.copy(stages = recalculated)
        }
    }

    /**
     * Deletes a stage manually
     */
    fun deleteStage(index: Int) {
        _recipe.update { current ->
            val list = current.stages.toMutableList()
            if (list.size > 1 && index in list.indices) {
                list.removeAt(index)
                val recalculated = BrewCalculator.recalculateAccumulatedWater(list)
                current.copy(stages = recalculated)
            } else {
                current
            }
        }
    }

    /**
     * Auto balances remaining water to match theoretical dose * ratio
     */
    fun autoBalanceWater() {
        _recipe.update { current ->
            val targetTotal = current.totalWaterGrams
            val balanced = BrewCalculator.balanceRemainingWater(current.stages, targetTotal)
            current.copy(stages = balanced)
        }
    }

    /**
     * Resets stages to automatic defaults based on current dose, ratio, and method
     */
    fun resetToAutoStages() {
        _recipe.update { current ->
            val defaultStages = BrewCalculator.generateStages(current.methodType, current.doseGrams, current.ratio)
            current.copy(stages = defaultStages)
        }
        resetTimer()
    }

    /**
     * Starts or resumes the brewing timer
     */
    fun startTimer() {
        if (_timerState.value.isFinished) {
            resetTimer()
        }

        _timerState.update { it.copy(isBrewing = true, isPaused = false) }

        // Trigger starting haptic and sound cue
        if (_timerState.value.totalElapsedSeconds == 0) {
            notifier.notifyStageTransition(
                vibrationEnabled = _timerState.value.vibrationEnabled,
                soundEnabled = _timerState.value.soundEnabled
            )
        }

        startTickerJob()
    }

    /**
     * Pauses the brewing timer
     */
    fun pauseTimer() {
        timerJob?.cancel()
        _timerState.update { it.copy(isPaused = true) }
    }

    /**
     * Resumes the brewing timer
     */
    fun resumeTimer() {
        if (_timerState.value.isPaused) {
            _timerState.update { it.copy(isPaused = false) }
            startTickerJob()
        }
    }

    /**
     * Resets the brewing timer to 0
     */
    fun resetTimer() {
        timerJob?.cancel()
        _timerState.update {
            it.copy(
                isBrewing = false,
                isPaused = false,
                isFinished = false,
                currentStageIndex = 0,
                elapsedStageSeconds = 0,
                totalElapsedSeconds = 0,
                showSummaryDialog = false
            )
        }
    }

    /**
     * Allows barista to jump directly to a chosen stage during live brew
     */
    fun jumpToStage(index: Int) {
        val stages = _recipe.value.stages
        if (index in stages.indices) {
            // Calculate total elapsed seconds up to this stage
            var elapsed = 0
            for (i in 0 until index) {
                elapsed += stages[i].durationSeconds
            }
            _timerState.update {
                it.copy(
                    currentStageIndex = index,
                    elapsedStageSeconds = 0,
                    totalElapsedSeconds = elapsed,
                    isFinished = false
                )
            }
            notifier.notifyStageTransition(
                vibrationEnabled = _timerState.value.vibrationEnabled,
                soundEnabled = _timerState.value.soundEnabled
            )
        }
    }

    private fun startTickerJob() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val currentState = _timerState.value
                val currentRecipe = _recipe.value
                val stages = currentRecipe.stages

                if (!currentState.isBrewing || currentState.isPaused || stages.isEmpty()) {
                    break
                }

                val currentStage = stages.getOrNull(currentState.currentStageIndex)
                if (currentStage == null) {
                    finishBrewing()
                    break
                }

                val nextElapsedInStage = currentState.elapsedStageSeconds + 1
                val nextTotalElapsed = currentState.totalElapsedSeconds + 1

                // Pre-warning 3 seconds before stage transition: get kettle ready!
                val remainingInStage = currentStage.durationSeconds - nextElapsedInStage
                if (remainingInStage == 3 && currentState.currentStageIndex < stages.size - 1) {
                    notifier.notifyStagePreWarning(
                        vibrationEnabled = currentState.vibrationEnabled,
                        soundEnabled = false
                    )
                }

                if (nextElapsedInStage >= currentStage.durationSeconds) {
                    // Transition to next stage or finish!
                    val nextStageIndex = currentState.currentStageIndex + 1
                    if (nextStageIndex < stages.size) {
                        _timerState.update {
                            it.copy(
                                currentStageIndex = nextStageIndex,
                                elapsedStageSeconds = 0,
                                totalElapsedSeconds = nextTotalElapsed
                            )
                        }
                        // Trigger HAPTIC VIBRATION on stage transition!
                        notifier.notifyStageTransition(
                            vibrationEnabled = currentState.vibrationEnabled,
                            soundEnabled = currentState.soundEnabled
                        )
                    } else {
                        // All stages finished!
                        _timerState.update {
                            it.copy(
                                elapsedStageSeconds = nextElapsedInStage,
                                totalElapsedSeconds = nextTotalElapsed
                            )
                        }
                        finishBrewing()
                        break
                    }
                } else {
                    _timerState.update {
                        it.copy(
                            elapsedStageSeconds = nextElapsedInStage,
                            totalElapsedSeconds = nextTotalElapsed
                        )
                    }
                }
            }
        }
    }

    private fun finishBrewing() {
        timerJob?.cancel()
        _timerState.update {
            it.copy(
                isBrewing = false,
                isPaused = false,
                isFinished = true,
                showSummaryDialog = true
            )
        }
        notifier.notifyBrewFinished(
            vibrationEnabled = _timerState.value.vibrationEnabled,
            soundEnabled = _timerState.value.soundEnabled
        )
    }

    fun dismissSummaryDialog() {
        _timerState.update { it.copy(showSummaryDialog = false) }
    }

    fun toggleVibration() {
        _timerState.update { it.copy(vibrationEnabled = !it.vibrationEnabled) }
    }

    fun toggleSound() {
        _timerState.update { it.copy(soundEnabled = !it.soundEnabled) }
    }

    fun testVibration() {
        notifier.notifyStageTransition(
            vibrationEnabled = true,
            soundEnabled = _timerState.value.soundEnabled
        )
    }

    fun saveBrewHistory(beanName: String, rating: Int, notes: String) {
        val currentRecipe = _recipe.value
        val duration = _timerState.value.totalElapsedSeconds
        viewModelScope.launch {
            val entity = BrewHistoryEntity(
                beanName = beanName,
                recipeName = currentRecipe.name,
                doseGrams = currentRecipe.doseGrams,
                ratio = currentRecipe.ratio,
                totalWaterGrams = currentRecipe.totalWaterGrams,
                durationSeconds = duration,
                rating = rating,
                tastingNotes = notes
            )
            dao.insertBrew(entity)

            // Sync to Firestore if authenticated with Google
            if (_currentUser.value != null) {
                try {
                    firestoreRepo.saveBrew(
                        beanName = beanName,
                        recipeName = currentRecipe.name,
                        doseGrams = currentRecipe.doseGrams,
                        ratio = currentRecipe.ratio,
                        totalWaterGrams = currentRecipe.totalWaterGrams,
                        durationSeconds = duration,
                        rating = rating,
                        tastingNotes = notes
                    )
                } catch (e: Exception) {
                    android.util.Log.w("BrewViewModel", "Failed to sync brew to Firestore", e)
                }
            }

            dismissSummaryDialog()
        }
    }

    fun deleteBrewHistory(item: BrewHistoryEntity) {
        viewModelScope.launch {
            dao.deleteBrew(item)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        notifier.release()
        firebaseAuth.removeAuthStateListener(authListener)
    }
}
