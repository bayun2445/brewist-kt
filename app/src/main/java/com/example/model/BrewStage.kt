package com.example.model

import java.util.UUID

data class BrewStage(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val waterGrams: Int,
    val accumulatedWaterGrams: Int = 0,
    val durationSeconds: Int,
    val instruction: String = "",
    val isBloom: Boolean = false,
    val isDrawdown: Boolean = false
) {
    val formattedDuration: String
        get() {
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return String.format("%02d:%02d", mins, secs)
        }
}
