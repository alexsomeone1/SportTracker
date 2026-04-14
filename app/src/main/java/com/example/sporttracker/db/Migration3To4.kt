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
        val seeds = listOf(
            Triple("GYM", "Силове тренування", "🏋️"),
            Triple("FOOTBALL", "Футбол", "⚽"),
            Triple("RUNNING", "Біг", "🏃"),
            Triple("TABLE_TENNIS", "Настільний теніс", "🏓"),
            Triple("TENNIS", "Теніс", "🎾"),
            Triple("SWIMMING", "Плавання", "🏊"),
            Triple("CYCLING", "Велосипед", "🚴")
        )
        seeds.forEachIndexed { index, (id, name, emoji) ->
            database.execSQL(
                "INSERT OR IGNORE INTO sport_definitions (id, nameUa, emoji, sortOrder, isBuiltIn) VALUES (?, ?, ?, ?, 1)",
                arrayOf<Any>(id, name, emoji, index)
            )
        }
    }
}
