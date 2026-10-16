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

import android.content.Context
import com.example.data.BeanEntity
import com.example.data.FirestoreBrewRepository
import com.example.model.BrewRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

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

    private val prefs = application.getSharedPreferences("v60_brew_timer_prefs", Context.MODE_PRIVATE)

    private fun saveRecipePreferences(recipe: BrewRecipe) {
        try {
            val jsonArray = JSONArray()
            recipe.stages.forEach { stage ->
                val obj = JSONObject()
                obj.put("id", stage.id)
                obj.put("name", stage.name)
                obj.put("waterGrams", stage.waterGrams)
                obj.put("accumulatedWaterGrams", stage.accumulatedWaterGrams)
                obj.put("durationSeconds", stage.durationSeconds)
                obj.put("instruction", stage.instruction)
                obj.put("isBloom", stage.isBloom)
                obj.put("isDrawdown", stage.isDrawdown)
                jsonArray.put(obj)
            }
            prefs.edit()
                .putString(KEY_LAST_METHOD, recipe.methodType.name)
                .putFloat(KEY_LAST_DOSE, recipe.doseGrams)
                .putFloat(KEY_LAST_RATIO, recipe.ratio)
                .putString(KEY_LAST_RECIPE_NAME, recipe.name)
                .putString(KEY_LAST_RECIPE_DESC, recipe.description)
                .putString(KEY_LAST_STAGES_JSON, jsonArray.toString())
                .apply()
        } catch (e: Exception) {
            android.util.Log.e("BrewViewModel", "Failed to save recipe preferences", e)
        }
    }

    private fun loadSavedRecipe(): BrewRecipe? {
        if (!prefs.contains(KEY_LAST_METHOD)) return null
        return try {
            val methodName = prefs.getString(KEY_LAST_METHOD, null) ?: return null
            val methodType = try {
                BrewMethodType.valueOf(methodName)
            } catch (e: Exception) {
                BrewMethodType.CLASSIC_3_POUR
            }
            val dose = prefs.getFloat(KEY_LAST_DOSE, 15f)
            val ratio = prefs.getFloat(KEY_LAST_RATIO, 15f)
            val name = prefs.getString(KEY_LAST_RECIPE_NAME, methodType.displayName) ?: methodType.displayName
            val desc = prefs.getString(KEY_LAST_RECIPE_DESC, methodType.shortDesc) ?: methodType.shortDesc
            val stagesJson = prefs.getString(KEY_LAST_STAGES_JSON, null)
            val stages = if (!stagesJson.isNullOrBlank()) {
                val list = mutableListOf<BrewStage>()
                val jsonArray = JSONArray(stagesJson)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val isIndonesian = java.util.Locale.getDefault().language.lowercase().let { it == "in" || it == "id" }
                    list.add(
                        BrewStage(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            name = obj.optString("name", if (isIndonesian) "Tahap ${i + 1}" else "Stage ${i + 1}"),
                            waterGrams = obj.optInt("waterGrams", 40),
                            accumulatedWaterGrams = obj.optInt("accumulatedWaterGrams", 0),
                            durationSeconds = obj.optInt("durationSeconds", 30),
                            instruction = obj.optString("instruction", ""),
                            isBloom = obj.optBoolean("isBloom", false),
                            isDrawdown = obj.optBoolean("isDrawdown", false)
                        )
                    )
                }
                if (list.isNotEmpty()) list else BrewCalculator.generateStages(methodType, dose, ratio)
            } else {
                BrewCalculator.generateStages(methodType, dose, ratio)
            }

            BrewRecipe(
                id = "recipe_saved_${System.currentTimeMillis()}",
                name = name,
                methodType = methodType,
                description = desc,
                doseGrams = dose,
                ratio = ratio,
                stages = stages
            )
        } catch (e: Exception) {
            android.util.Log.e("BrewViewModel", "Failed to load saved recipe", e)
            null
        }
    }

    // Initial recipe: Automatically loaded from last saved setting on startup, or fallback default (Classic 3-Pour)
    private val _recipe = MutableStateFlow(
        loadSavedRecipe() ?: createDefaultRecipe(
            methodType = BrewMethodType.CLASSIC_3_POUR,
            dose = 15f,
            ratio = 15f
        )
    )
    val recipe: StateFlow<BrewRecipe> = _recipe.asStateFlow()

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

    // List of unique saved bean names across beans table, local journal, and cloud sync for autocomplete
    val savedBeanNames: StateFlow<List<String>> = combine(dao.getAllBeans(), brewHistory, cloudBrews) { beans, local, cloud ->
        val set = linkedSetOf<String>()
        beans.forEach { b ->
            val trimmed = b.name.trim()
            if (trimmed.isNotBlank()) set.add(trimmed)
        }
        local.forEach { entity ->
            val trimmed = entity.beanName.trim()
            if (trimmed.isNotBlank()) set.add(trimmed)
        }
        cloud.forEach { record ->
            val trimmed = record.beanName.trim()
            if (trimmed.isNotBlank()) set.add(trimmed)
        }
        set.toList().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.update { !it }
    }

    private var timerJob: Job? = null

    companion object {
        private const val KEY_LAST_METHOD = "last_method"
        private const val KEY_LAST_DOSE = "last_dose"
        private const val KEY_LAST_RATIO = "last_ratio"
        private const val KEY_LAST_STAGES_JSON = "last_stages_json"
        private const val KEY_LAST_RECIPE_NAME = "last_recipe_name"
        private const val KEY_LAST_RECIPE_DESC = "last_recipe_desc"

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
        saveRecipePreferences(_recipe.value)
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
        saveRecipePreferences(_recipe.value)
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
        saveRecipePreferences(_recipe.value)
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
        saveRecipePreferences(_recipe.value)
    }

    /**
     * Adds a new stage manually
     */
    fun addStage() {
        _recipe.update { current ->
            val list = current.stages.toMutableList()
            val isIndonesian = java.util.Locale.getDefault().language.lowercase().let { it == "in" || it == "id" }
            val newStage = BrewStage(
                name = if (isIndonesian) "Penuangan Tambahan #${list.size + 1}" else "Additional Pour #${list.size + 1}",
                waterGrams = 40,
                durationSeconds = 30,
                instruction = if (isIndonesian) "Penuangan tambahan untuk memperpanjang ekstraksi." else "Additional pour to extend extraction."
            )
            list.add(newStage)
            val recalculated = BrewCalculator.recalculateAccumulatedWater(list)
            current.copy(stages = recalculated)
        }
        saveRecipePreferences(_recipe.value)
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
        saveRecipePreferences(_recipe.value)
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
        saveRecipePreferences(_recipe.value)
    }

    /**
     * Resets stages to automatic defaults based on current dose, ratio, and method
     */
    fun resetToAutoStages() {
        _recipe.update { current ->
            val defaultStages = BrewCalculator.generateStages(current.methodType, current.doseGrams, current.ratio)
            current.copy(stages = defaultStages)
        }
        saveRecipePreferences(_recipe.value)
        resetTimer()
    }

    /**
     * Applies the exact settings from a past brew history entry
     */
    fun applyRecipeFromHistory(item: BrewHistoryEntity) {
        val method = findMatchingMethod(item.recipeName)
        val stages = BrewCalculator.generateStages(method, item.doseGrams, item.ratio)
        val newRecipe = BrewRecipe(
            id = "recipe_${System.currentTimeMillis()}",
            name = item.recipeName.ifBlank { method.displayName },
            methodType = method,
            description = method.shortDesc,
            doseGrams = item.doseGrams,
            ratio = item.ratio,
            stages = stages
        )
        _recipe.value = newRecipe
        saveRecipePreferences(newRecipe)
        resetTimer()
    }

    private fun findMatchingMethod(recipeName: String): BrewMethodType {
        val clean = recipeName.lowercase()
        return when {
            clean.contains("kasuya") || clean.contains("4:6") || clean.contains("4-6") -> BrewMethodType.KASUYA_4_6
            clean.contains("hoffmann") || clean.contains("hoffman") -> BrewMethodType.HOFFMANN_V60
            clean.contains("3-pour") || clean.contains("3 pour") || clean.contains("klasik") -> BrewMethodType.CLASSIC_3_POUR
            clean.contains("rao") || clean.contains("single") -> BrewMethodType.RAO_SINGLE_POUR
            clean.contains("kustom") || clean.contains("custom") -> BrewMethodType.CUSTOM
            else -> BrewMethodType.values().find {
                it.displayName.equals(recipeName, ignoreCase = true) || it.name.equals(recipeName, ignoreCase = true)
            } ?: BrewMethodType.KASUYA_4_6
        }
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
            val trimmedBean = beanName.trim()
            val beanId = if (trimmedBean.isNotBlank()) {
                val existing = dao.getBeanByName(trimmedBean)
                existing?.id ?: dao.insertBean(BeanEntity(name = trimmedBean))
            } else 0L

            val entity = BrewHistoryEntity(
                beanId = beanId,
                beanName = trimmedBean,
                recipeName = currentRecipe.name,
                doseGrams = currentRecipe.doseGrams,
                ratio = currentRecipe.ratio,
                totalWaterGrams = currentRecipe.totalWaterGrams,
                durationSeconds = duration,
                rating = rating,
                tastingNotes = notes
            )
            dao.insertBrew(entity)
            saveRecipePreferences(currentRecipe)

            // Sync to Firestore if authenticated with Google
            if (_currentUser.value != null) {
                try {
                    firestoreRepo.saveBrew(
                        beanName = trimmedBean,
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

    /**
     * Updates an existing brew entry.
     * If applyToAllBrewsOfThisBean is true, renames the bean in the beans table and
     * propagates the new bean name across all historical brews for that bean!
     */
    fun updateBrewJournal(
        brew: BrewHistoryEntity,
        newBeanName: String,
        newRating: Int,
        newNotes: String,
        applyToAllBrewsOfThisBean: Boolean
    ) {
        viewModelScope.launch {
            val trimmedNewName = newBeanName.trim()
            val oldName = brew.beanName.trim()

            if (applyToAllBrewsOfThisBean && oldName.isNotBlank() && trimmedNewName.isNotBlank() && trimmedNewName != oldName) {
                // Update beans table and all brews associated with this bean
                val existingBean = if (brew.beanId > 0) dao.getBeanById(brew.beanId) else dao.getBeanByName(oldName)
                val targetBeanId = if (existingBean != null) {
                    dao.updateBeanName(existingBean.id, trimmedNewName)
                    dao.updateBrewsBeanName(existingBean.id, trimmedNewName)
                    existingBean.id
                } else {
                    val newId = dao.insertBean(BeanEntity(name = trimmedNewName))
                    if (newId > 0) dao.updateBrewsBeanName(newId, trimmedNewName)
                    newId
                }
                dao.updateBrewsByOldName(oldName, trimmedNewName)

                val updatedBrew = brew.copy(
                    beanId = targetBeanId,
                    beanName = trimmedNewName,
                    rating = newRating,
                    tastingNotes = newNotes
                )
                dao.updateBrew(updatedBrew)
            } else {
                // Update single brew entry
                val beanId = if (trimmedNewName.isNotBlank()) {
                    val existing = dao.getBeanByName(trimmedNewName)
                    existing?.id ?: dao.insertBean(BeanEntity(name = trimmedNewName))
                } else 0L

                val updatedBrew = brew.copy(
                    beanId = beanId,
                    beanName = trimmedNewName,
                    rating = newRating,
                    tastingNotes = newNotes
                )
                dao.updateBrew(updatedBrew)
            }
        }
    }

    /**
     * Renames a bean globally: updates the bean in the beans table and propagates
     * the new name to all historical brews of that bean.
     */
    fun renameBean(oldName: String, newName: String) {
        val trimmedOld = oldName.trim()
        val trimmedNew = newName.trim()
        if (trimmedNew.isBlank() || trimmedNew == trimmedOld) return
        viewModelScope.launch {
            val bean = dao.getBeanByName(trimmedOld)
            if (bean != null) {
                dao.updateBeanName(bean.id, trimmedNew)
                dao.updateBrewsBeanName(bean.id, trimmedNew)
            } else {
                val newId = dao.insertBean(BeanEntity(name = trimmedNew))
                if (newId > 0) dao.updateBrewsBeanName(newId, trimmedNew)
            }
            dao.updateBrewsByOldName(trimmedOld, trimmedNew)
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
