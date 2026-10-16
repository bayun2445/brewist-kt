package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(
    tableName = "brew_history",
    indices = [
        Index(value = ["beanId"]),
        Index(value = ["timestamp"])
    ]
)
data class BrewHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val beanId: Long = 0,
    val beanName: String,
    val recipeName: String,
    val doseGrams: Float,
    val ratio: Float,
    val totalWaterGrams: Int,
    val durationSeconds: Int,
    val rating: Int,
    val tastingNotes: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            return sdf.format(Date(timestamp))
        }

    val formattedDuration: String
        get() {
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return String.format("%02d:%02d", mins, secs)
        }
}
