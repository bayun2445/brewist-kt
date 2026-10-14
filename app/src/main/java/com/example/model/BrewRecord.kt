package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@IgnoreExtraProperties
data class BrewRecord(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val beanName: String = "",
    val recipeName: String = "",
    val doseGrams: Double = 15.0,
    val ratio: Double = 15.0,
    val totalWaterGrams: Long = 225L,
    val durationSeconds: Long = 0L,
    val rating: Long = 5L,
    val tastingNotes: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    val doseGramsFloat: Float
        get() = doseGrams.toFloat()

    val ratioFloat: Float
        get() = ratio.toFloat()

    val formattedDate: String
        get() {
            val date = createdAt?.toDate() ?: Date()
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            return sdf.format(date)
        }

    val formattedDuration: String
        get() {
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return String.format("%02d:%02d", mins, secs)
        }
}
