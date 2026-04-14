package com.example.sporttracker.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_TO_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `sport_definitions` (
                `id` TEXT NOT NULL,
                `nameUa` TEXT NOT NULL,
                `emoji` TEXT NOT NULL,
                `sortOrder` INTEGER NOT NULL,
                `isBuiltIn` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        insertBuiltinSportSeeds(database)
    }
}
