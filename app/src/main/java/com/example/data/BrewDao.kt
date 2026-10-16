package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BrewDao {

    @Query("""
        SELECT h.id, h.beanId, COALESCE(b.name, h.beanName) as beanName,
               h.recipeName, h.doseGrams, h.ratio, h.totalWaterGrams,
               h.durationSeconds, h.rating, h.tastingNotes, h.timestamp
        FROM brew_history h
        LEFT JOIN beans b ON h.beanId = b.id
        ORDER BY h.timestamp DESC
    """)
    fun getAllBrews(): Flow<List<BrewHistoryEntity>>

    @Query("SELECT * FROM beans ORDER BY updatedAt DESC")
    fun getAllBeans(): Flow<List<BeanEntity>>

    @Query("SELECT * FROM beans WHERE name = :name LIMIT 1")
    suspend fun getBeanByName(name: String): BeanEntity?

    @Query("SELECT * FROM beans WHERE id = :id LIMIT 1")
    suspend fun getBeanById(id: Long): BeanEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBean(bean: BeanEntity): Long

    @Update
    suspend fun updateBean(bean: BeanEntity)

    @Query("UPDATE beans SET name = :newName, updatedAt = :timestamp WHERE id = :beanId")
    suspend fun updateBeanName(beanId: Long, newName: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE brew_history SET beanName = :newName WHERE beanId = :beanId")
    suspend fun updateBrewsBeanName(beanId: Long, newName: String)

    @Query("UPDATE brew_history SET beanName = :newName WHERE beanName = :oldName")
    suspend fun updateBrewsByOldName(oldName: String, newName: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrew(brew: BrewHistoryEntity): Long

    @Update
    suspend fun updateBrew(brew: BrewHistoryEntity)

    @Delete
    suspend fun deleteBrew(brew: BrewHistoryEntity)

    @Query("DELETE FROM brew_history WHERE id = :id")
    suspend fun deleteBrewById(id: Long)

    @Query("DELETE FROM brew_history")
    suspend fun clearHistory()
}
