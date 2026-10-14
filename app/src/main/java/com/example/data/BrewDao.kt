package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BrewDao {
    @Query("SELECT * FROM brew_history ORDER BY timestamp DESC")
    fun getAllBrews(): Flow<List<BrewHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrew(brew: BrewHistoryEntity): Long

    @Delete
    suspend fun deleteBrew(brew: BrewHistoryEntity)

    @Query("DELETE FROM brew_history")
    suspend fun clearHistory()
}
