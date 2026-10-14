package com.example.util

import com.example.model.BrewMethodType
import com.example.model.BrewStage
import kotlin.math.roundToInt

object BrewCalculator {

    /**
     * Calculates total water from dose and ratio
     */
    fun calculateTotalWater(doseGrams: Float, ratio: Float): Int {
        return (doseGrams * ratio).roundToInt()
    }

    /**
     * Generates stages automatically based on dose, ratio, and brewing method
     */
    fun generateStages(methodType: BrewMethodType, doseGrams: Float, ratio: Float): List<BrewStage> {
        val totalWater = calculateTotalWater(doseGrams, ratio)

        return when (methodType) {
            BrewMethodType.KASUYA_4_6 -> generateKasuyaStages(doseGrams, totalWater)
            BrewMethodType.HOFFMANN_V60 -> generateHoffmannStages(doseGrams, totalWater)
            BrewMethodType.CLASSIC_3_POUR -> generateClassic3PourStages(doseGrams, totalWater)
            BrewMethodType.RAO_SINGLE_POUR -> generateRaoStages(doseGrams, totalWater)
            BrewMethodType.CUSTOM -> generateClassic3PourStages(doseGrams, totalWater)
        }
    }

    /**
     * Tetsu Kasuya 4:6 Method:
     * 40% water split into 2 pours (bloom + 2nd pour)
     * 60% water split into 3 equal pours
     * Total 5 pours ~45s intervals
     */
    private fun generateKasuyaStages(doseGrams: Float, totalWater: Int): List<BrewStage> {
        val fortyPercent = (totalWater * 0.40f).roundToInt()
        val sixtyPercent = totalWater - fortyPercent

        // Sweetness balance: bloom slightly smaller than 2nd pour
        val bloomWater = (fortyPercent * 0.42f).roundToInt().coerceAtLeast((doseGrams * 2).roundToInt())
        val pour2Water = fortyPercent - bloomWater

        // Strength: split remaining into 3 equal pours
        val p3 = sixtyPercent / 3
        val p4 = sixtyPercent / 3
        val p5 = sixtyPercent - (p3 + p4)

        val stages = listOf(
            BrewStage(
                name = "Blooming (Tahap 1/5)",
                waterGrams = bloomWater,
                durationSeconds = 45,
                instruction = "Basahi seluruh bubuk kopi secara melingkar. Biarkan mekar.",
                isBloom = true
            ),
            BrewStage(
                name = "Penuangan 2 (Tahap 2/5)",
                waterGrams = pour2Water,
                durationSeconds = 45,
                instruction = "Tuang lembut dari tengah ke luar untuk ekstraksi keasaman/manis."
            ),
            BrewStage(
                name = "Penuangan 3 (Tahap 3/5)",
                waterGrams = p3,
                durationSeconds = 45,
                instruction = "Tuang mantap secara spiral untuk membangun body kopi."
            ),
            BrewStage(
                name = "Penuangan 4 (Tahap 4/5)",
                waterGrams = p4,
                durationSeconds = 45,
                instruction = "Jaga level air tetap konsisten dengan aliran teratur."
            ),
            BrewStage(
                name = "Penuangan 5 & Tetes Akhir (Tahap 5/5)",
                waterGrams = p5,
                durationSeconds = 45,
                instruction = "Penuangan terakhir, tunggu hingga air turun tuntas (drawdown).",
                isDrawdown = true
            )
        )

        return recalculateAccumulatedWater(stages)
    }

    /**
     * James Hoffmann Ultimate V60 Technique:
     * Bloom: 2x - 3x dose with gentle swirl (45s)
     * Pour up to 60% total water by 1:15 (30s)
     * Pour remaining up to 100% total water by 1:45 (30s)
     * Gentle stir, swirl & drawdown to ~3:15 (90s)
     */
    private fun generateHoffmannStages(doseGrams: Float, totalWater: Int): List<BrewStage> {
        val bloomWater = (doseGrams * 3f).roundToInt().coerceAtMost((totalWater * 0.3f).roundToInt())
        val target60 = (totalWater * 0.60f).roundToInt()
        val pour1Water = target60 - bloomWater
        val pour2Water = totalWater - target60

        val stages = listOf(
            BrewStage(
                name = "Blooming & Swirl",
                waterGrams = bloomWater,
                durationSeconds = 45,
                instruction = "Basahi kopi merata dan putar (swirl) dripper perlahan agar semua bubuk basah.",
                isBloom = true
            ),
            BrewStage(
                name = "Penuangan Utama (Hingga 60%)",
                waterGrams = pour1Water,
                durationSeconds = 30,
                instruction = "Tuang cepat dan teratur secara melingkar hingga mencapai 60% total air."
            ),
            BrewStage(
                name = "Penuangan Akhir (Hingga 100%)",
                waterGrams = pour2Water,
                durationSeconds = 30,
                instruction = "Tuang lebih lembut ke tengah hingga target total air tercapai."
            ),
            BrewStage(
                name = "Aduk Ringan & Drawdown",
                waterGrams = 0,
                durationSeconds = 75,
                instruction = "Aduk 1x searah jarum jam, putar lembut (swirl) dripper, biarkan air surut rata.",
                isDrawdown = true
            )
        )

        return recalculateAccumulatedWater(stages)
    }

    /**
     * Classic 3-Pour Method:
     * Bloom: 3x dose (~45s)
     * Pour 1: 50% remaining water (45s)
     * Pour 2: 50% remaining water + drawdown (60s)
     */
    private fun generateClassic3PourStages(doseGrams: Float, totalWater: Int): List<BrewStage> {
        val bloomWater = (doseGrams * 3f).roundToInt().coerceAtMost((totalWater * 0.35f).roundToInt())
        val remaining = totalWater - bloomWater
        val pour1Water = remaining / 2
        val pour2Water = remaining - pour1Water

        val stages = listOf(
            BrewStage(
                name = "Blooming",
                waterGrams = bloomWater,
                durationSeconds = 45,
                instruction = "Tuang air dari tengah ke luar, basahi semua bubuk. Gas CO2 keluar.",
                isBloom = true
            ),
            BrewStage(
                name = "Penuangan 1",
                waterGrams = pour1Water,
                durationSeconds = 45,
                instruction = "Tuang spiral konsisten dari tengah ke tepi tanpa mengenai kertas filter."
            ),
            BrewStage(
                name = "Penuangan 2 & Drawdown",
                waterGrams = pour2Water,
                durationSeconds = 60,
                instruction = "Tuang sisa air perlahan, tunggu hingga air turun tuntas ke server.",
                isDrawdown = true
            )
        )

        return recalculateAccumulatedWater(stages)
    }

    /**
     * Rao Single Pour:
     * Bloom (45s), then continuous gentle pour up to total water, then drawdown.
     */
    private fun generateRaoStages(doseGrams: Float, totalWater: Int): List<BrewStage> {
        val bloomWater = (doseGrams * 3f).roundToInt().coerceAtMost((totalWater * 0.3f).roundToInt())
        val mainPourWater = totalWater - bloomWater

        val stages = listOf(
            BrewStage(
                name = "Blooming",
                waterGrams = bloomWater,
                durationSeconds = 45,
                instruction = "Basahi kopi merata dan lakukan penggalian ringan (excavation) jika perlu.",
                isBloom = true
            ),
            BrewStage(
                name = "Penuangan Kontinu",
                waterGrams = mainPourWater,
                durationSeconds = 50,
                instruction = "Tuang secara terus menerus dengan debit stabil ~4-5 g/s hingga batas total."
            ),
            BrewStage(
                name = "Drawdown (Tetesan Akhir)",
                waterGrams = 0,
                durationSeconds = 65,
                instruction = "Putar dripper perlahan (Rao spin) untuk meratakan ampas kopi.",
                isDrawdown = true
            )
        )

        return recalculateAccumulatedWater(stages)
    }

    /**
     * Recalculates cumulative water target for each stage sequentially.
     */
    fun recalculateAccumulatedWater(stages: List<BrewStage>): List<BrewStage> {
        var currentAcc = 0
        return stages.map { stage ->
            currentAcc += stage.waterGrams
            stage.copy(accumulatedWaterGrams = currentAcc)
        }
    }

    /**
     * Auto balances remaining water among non-bloom stages so total equals targetTotalWater
     */
    fun balanceRemainingWater(stages: List<BrewStage>, targetTotalWater: Int): List<BrewStage> {
        if (stages.isEmpty()) return stages
        val bloomStage = stages.firstOrNull { it.isBloom } ?: stages.first()
        val bloomWater = bloomStage.waterGrams
        val nonBloomStages = stages.filter { it != bloomStage && !it.isDrawdown }

        if (nonBloomStages.isEmpty()) {
            return recalculateAccumulatedWater(stages)
        }

        val remainingWater = (targetTotalWater - bloomWater).coerceAtLeast(0)
        val perStage = remainingWater / nonBloomStages.size
        var leftover = remainingWater % nonBloomStages.size

        val updatedStages = stages.map { stage ->
            when {
                stage == bloomStage -> stage
                stage.isDrawdown && stage.waterGrams == 0 -> stage
                stage in nonBloomStages -> {
                    val extra = if (leftover > 0) { leftover--; 1 } else 0
                    stage.copy(waterGrams = perStage + extra)
                }
                else -> stage
            }
        }

        return recalculateAccumulatedWater(updatedStages)
    }
}
