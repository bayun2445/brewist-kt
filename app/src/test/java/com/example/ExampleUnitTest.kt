package com.example

import com.example.model.BrewMethodType
import com.example.model.BrewStage
import com.example.util.BrewCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testTotalWaterCalculation() {
        val totalWater = BrewCalculator.calculateTotalWater(15f, 15f)
        assertEquals(225, totalWater)

        val totalWater16 = BrewCalculator.calculateTotalWater(18f, 16f)
        assertEquals(288, totalWater16)
    }

    @Test
    fun testKasuyaStagesGeneration() {
        val stages = BrewCalculator.generateStages(BrewMethodType.KASUYA_4_6, 15f, 15f)
        assertEquals(5, stages.size)
        assertTrue(stages.first().isBloom)
        assertEquals(225, stages.last().accumulatedWaterGrams)
    }

    @Test
    fun testStageRecalculation() {
        val rawStages = listOf(
            BrewStage(name = "Bloom", waterGrams = 45, durationSeconds = 45),
            BrewStage(name = "Pour 1", waterGrams = 80, durationSeconds = 30),
            BrewStage(name = "Pour 2", waterGrams = 100, durationSeconds = 45)
        )
        val recalculated = BrewCalculator.recalculateAccumulatedWater(rawStages)
        assertEquals(45, recalculated[0].accumulatedWaterGrams)
        assertEquals(125, recalculated[1].accumulatedWaterGrams)
        assertEquals(225, recalculated[2].accumulatedWaterGrams)
    }

    @Test
    fun testWaterAutoBalance() {
        val rawStages = listOf(
            BrewStage(name = "Bloom", waterGrams = 45, durationSeconds = 45, isBloom = true),
            BrewStage(name = "Pour 1", waterGrams = 50, durationSeconds = 30),
            BrewStage(name = "Pour 2", waterGrams = 50, durationSeconds = 45)
        )
        // Target is 240g total water (e.g. 15g x 16)
        val balanced = BrewCalculator.balanceRemainingWater(rawStages, 240)
        assertEquals(45, balanced[0].waterGrams)
        val sumWater = balanced.sumOf { it.waterGrams }
        assertEquals(240, sumWater)
    }
}
