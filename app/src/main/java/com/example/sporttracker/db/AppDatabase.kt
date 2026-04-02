package com.example.sporttracker.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sporttracker.db.MIGRATION_1_TO_2
import com.example.sporttracker.db.MIGRATION_2_TO_3

@Database(
    entities = [TrainingEntity::class],
    version = 3,             // Збільшено до 3, щоб виправити невідповідність схеми
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun trainingDao(): TrainingDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sporttracker.db"
                )
                    // Міграція 1→2 залишена для старих інсталяцій
                    .addMigrations(MIGRATION_1_TO_2, MIGRATION_2_TO_3)
                    .build()
                INSTANCE = db
                db
            }
        }
    }
}
