package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BrewHistoryEntity::class], version = 1, exportSchema = false)
abstract class BrewDatabase : RoomDatabase() {
    abstract fun brewDao(): BrewDao

    companion object {
        @Volatile
        private var INSTANCE: BrewDatabase? = null

        fun getDatabase(context: Context): BrewDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrewDatabase::class.java,
                    "v60_timer_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
