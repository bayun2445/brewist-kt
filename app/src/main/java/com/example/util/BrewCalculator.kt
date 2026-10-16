package com.example.util

import com.example.model.BrewMethodType
import com.example.model.BrewStage
import java.util.Locale
import kotlin.math.roundToInt

object BrewCalculator {

    private val isIndonesian: Boolean
        get() {
            val lang = Locale.getDefault().language.lowercase()
            return lang == "in" || lang == "id"
        }

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

        val id = isIndonesian
        val stages = listOf(
            BrewStage(
                name = if (id) "Blooming (Tahap 1/5)" else "Blooming (Stage 1/5)",
                waterGrams = bloomWater,
                durationSeconds = 45,
                instruction = if (id) "Basahi seluruh bubuk kopi secara melingkar. Biarkan mekar."
                else "Saturate all coffee grounds in circular motion. Allow to bloom.",
                isBloom = true
            ),
            BrewStage(
                name = if (id) "Penuangan 2 (Tahap 2/5)" else "Pour 2 (Stage 2/5)",
                waterGrams = pour2Water,
                durationSeconds = 45,
                instruction = if (id) "Tuang lembut dari tengah ke luar untuk ekstraksi keasaman/manis."
                else "Gentle pour from center outwards to extract sweetness and acidity."
            ),
            BrewStage(
                name = if (id) "Penuangan 3 (Tahap 3/5)" else "Pour 3 (Stage 3/5)",
                waterGrams = p3,
                durationSeconds = 45,
                instruction = if (id) "Tuang mantap secara spiral untuk membangun body kopi."
                else "Steady spiral pour to build coffee body."
            ),
            BrewStage(
                name = if (id) "Penuangan 4 (Tahap 4/5)" else "Pour 4 (Stage 4/5)",
                waterGrams = p4,
                durationSeconds = 45,
                instruction = if (id) "Jaga level air tetap konsisten dengan aliran teratur."
                else "Maintain consistent water level with an even flow."
            ),
            BrewStage(
                name = if (id) "Penuangan 5 & Tetes Akhir (Tahap 5/5)" else "Pour 5 & Drawdown (Stage 5/5)",
                waterGrams = p5,
                durationSeconds = 45,
                instruction = if (id) "Penuangan terakhir, tunggu hingga air turun tuntas (drawdown)."
                else "Final pour, allow water to draw down completely.",
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

        val id = isIndonesian
        val stages = listOf(
            BrewStage(
                name = if (id) "Blooming & Swirl" else "Blooming & Swirl",
                waterGrams = bloomWater,
                durationSeconds = 45,
                instruction = if (id) "Basahi kopi merata dan putar (swirl) dripper perlahan agar semua bubuk basah."
                else "Saturate grounds evenly and gently swirl dripper to wet all coffee.",
                isBloom = true
            ),
            BrewStage(
                name = if (id) "Penuangan Utama (Hingga 60%)" else "Main Pour (Up to 60%)",
                waterGrams = pour1Water,
                durationSeconds = 30,
                instruction = if (id) "Tuang cepat dan teratur secara melingkar hingga mencapai 60% total air."
                else "Pour steadily in spirals until reaching 60% of total water."
            ),
            BrewStage(
                name = if (id) "Penuangan Akhir (Hingga 100%)" else "Final Pour (Up to 100%)",
                waterGrams = pour2Water,
                durationSeconds = 30,
                instruction = if (id) "Tuang lebih lembut ke tengah hingga target total air tercapai."
                else "Gentle pour toward center until target total water is reached."
            ),
            BrewStage(
                name = if (id) "Aduk Ringan & Drawdown" else "Gentle Stir & Drawdown",
                waterGrams = 0,
                durationSeconds = 75,
                instruction = if (id) "Aduk 1x searah jarum jam, putar lembut (swirl) dripper, biarkan air surut rata."
                else "Stir once clockwise, swirl gently, and allow water to draw down into a flat bed.",
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

        val id = isIndonesian
        val stages = listOf(
            BrewStage(
                name = if (id) "Blooming" else "Blooming",
                waterGrams = bloomWater,
                durationSeconds = 45,
                instruction = if (id) "Tuang air dari tengah ke luar, basahi semua bubuk. Gas CO2 keluar."
                else "Pour from center out to wet all coffee grounds. Allow CO2 degassing.",
                isBloom = true
            ),
            BrewStage(
                name = if (id) "Penuangan 1" else "Pour 1",
                waterGrams = pour1Water,
                durationSeconds = 45,
                instruction = if (id) "Tuang spiral konsisten dari tengah ke tepi tanpa mengenai kertas filter."
                else "Consistent spiral pour from center outward without hitting the paper filter."
            ),
            BrewStage(
                name = if (id) "Penuangan 2 & Drawdown" else "Pour 2 & Drawdown",
                waterGrams = pour2Water,
                durationSeconds = 60,
                instruction = if (id) "Tuang sisa air perlahan, tunggu hingga air turun tuntas ke server."
                else "Pour remaining water gently, wait for complete drawdown into the server.",
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

        val id = isIndonesian
        val stages = listOf(
            BrewStage(
                name = if (id) "Blooming" else "Blooming",
                waterGrams = bloomWater,
                durationSeconds = 45,
                instruction = if (id) "Basahi kopi merata dan lakukan penggalian ringan (excavation) jika perlu."
                else "Wet coffee evenly and perform gentle excavation if necessary.",
                isBloom = true
            ),
            BrewStage(
                name = if (id) "Penuangan Kontinu" else "Continuous Pour",
                waterGrams = mainPourWater,
                durationSeconds = 50,
                instruction = if (id) "Tuang secara terus menerus dengan debit stabil ~4-5 g/s hingga batas total."
                else "Pour continuously at steady ~4-5 g/s flow until target total water.",
            ),
            BrewStage(
                name = if (id) "Drawdown (Tetesan Akhir)" else "Drawdown (Final Drip)",
                waterGrams = 0,
                durationSeconds = 65,
                instruction = if (id) "Putar dripper perlahan (Rao spin) untuk meratakan ampas kopi."
                else "Swirl dripper gently (Rao spin) to settle the coffee bed flat.",
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
