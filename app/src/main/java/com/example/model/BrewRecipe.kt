package com.example.model

import androidx.annotation.StringRes
import com.example.R

enum class BrewMethodType(
    val displayName: String,
    val shortDesc: String,
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int
) {
    CLASSIC_3_POUR(
        "Metode 3-Pour Klasik",
        "Blooming + 2 penuangan teratur, mudah dan konsisten.",
        R.string.method_classic_title,
        R.string.method_classic_desc
    ),
    KASUYA_4_6(
        "Metode 4:6 (Tetsu Kasuya)",
        "5 penuangan seimbang untuk atur rasa manis, keasaman, dan body.",
        R.string.method_kasuya_title,
        R.string.method_kasuya_desc
    ),
    HOFFMANN_V60(
        "Metode James Hoffmann",
        "Teknik pusaran (swirl) dengan penuangan konsisten.",
        R.string.method_hoffmann_title,
        R.string.method_hoffmann_desc
    ),
    RAO_SINGLE_POUR(
        "Metode Rao Single Pour",
        "Blooming lalu 1 penuangan kontinu lambat.",
        R.string.method_rao_title,
        R.string.method_rao_desc
    ),
    CUSTOM(
        "Metode Kustom",
        "Atur bebas setiap tahapan sesuai selera Anda.",
        R.string.method_custom_title,
        R.string.method_custom_desc
    )
}

data class BrewRecipe(
    val id: String,
    val name: String,
    val methodType: BrewMethodType,
    val description: String,
    val doseGrams: Float = 15f,
    val ratio: Float = 15f, // 1:15
    val grindSize: String = "Medium-Coarse",
    val waterTempCelsius: Int = 92,
    val stages: List<BrewStage> = emptyList()
) {
    val totalWaterGrams: Int
        get() = (doseGrams * ratio).toInt()

    val totalPlannedSeconds: Int
        get() = stages.sumOf { it.durationSeconds }

    val formattedTotalTime: String
        get() {
            val mins = totalPlannedSeconds / 60
            val secs = totalPlannedSeconds % 60
            return String.format("%02d:%02d", mins, secs)
        }
}
