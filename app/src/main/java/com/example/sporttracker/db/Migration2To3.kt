package com.example.sporttracker.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_TO_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Видаляємо таблицю custom_sport_types, оскільки вона більше не потрібна
        database.execSQL("DROP TABLE IF EXISTS `custom_sport_types`")
    }
}
