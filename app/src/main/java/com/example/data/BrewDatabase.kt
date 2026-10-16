package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BrewHistoryEntity::class, BeanEntity::class],
    version = 2,
    exportSchema = false
)
abstract class BrewDatabase : RoomDatabase() {
    abstract fun brewDao(): BrewDao

    companion object {
        @Volatile
        private var INSTANCE: BrewDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create beans table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `beans` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_beans_name` ON `beans` (`name`)")

                // 2. Add beanId column to brew_history table and create indices
                db.execSQL("ALTER TABLE `brew_history` ADD COLUMN `beanId` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_brew_history_beanId` ON `brew_history` (`beanId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_brew_history_timestamp` ON `brew_history` (`timestamp`)")

                // 3. Migrate existing distinct bean names into beans table
                db.execSQL("""
                    INSERT OR IGNORE INTO `beans` (`name`, `createdAt`, `updatedAt`)
                    SELECT `beanName`, MIN(`timestamp`), MAX(`timestamp`)
                    FROM `brew_history`
                    WHERE `beanName` IS NOT NULL AND TRIM(`beanName`) != ''
                    GROUP BY `beanName`
                """.trimIndent())

                // 4. Update beanId in brew_history matching beans.name
                db.execSQL("""
                    UPDATE `brew_history`
                    SET `beanId` = COALESCE((
                        SELECT `beans`.`id`
                        FROM `beans`
                        WHERE `beans`.`name` = `brew_history`.`beanName`
                        LIMIT 1
                    ), 0)
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): BrewDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrewDatabase::class.java,
                    "v60_timer_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
