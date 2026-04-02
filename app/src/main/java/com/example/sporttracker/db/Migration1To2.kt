package com.example.sporttracker.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Залишаємо міграцію 1→2, навіть якщо таблиця custom_sport_types більше не використовується в коді.
// Вона просто створює додаткову таблицю, яка не заважає роботі додатку.
val MIGRATION_1_TO_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `custom_sport_types` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `emoji` TEXT NOT NULL DEFAULT '❓')"
        )
    }
}

