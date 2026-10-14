package com.example.model

enum class BrewMethodType(val displayName: String, val shortDesc: String) {
    KASUYA_4_6("Metode 4:6 (Tetsu Kasuya)", "5 penuangan seimbang untuk atur rasa manis, keasaman, dan body."),
    HOFFMANN_V60("Metode James Hoffmann", "Teknik pusaran (swirl) dengan penuangan konsisten."),
    CLASSIC_3_POUR("Metode 3-Pour Klasik", "Blooming + 2 penuangan teratur, mudah dan konsisten."),
    RAO_SINGLE_POUR("Metode Rao Single Pour", "Blooming lalu 1 penuangan kontinu lambat."),
    CUSTOM("Metode Kustom", "Atur bebas setiap tahapan sesuai selera Anda.")
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
