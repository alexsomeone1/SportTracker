package com.example.sporttracker.db

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Вбудовані види спорту. Мають збігатися з міграцією 3→4.
 * При першій установці Room не виконує міграції — таблиця створюється порожньою,
 * тому записи додаються через Callback і через [TrainingRepository.ensureBuiltinSportRows].
 */
fun builtinSportDefinitions(): List<SportDefinitionEntity> = listOf(
    SportDefinitionEntity("GYM", "Силове тренування", "🏋️", 0, true),
    SportDefinitionEntity("FOOTBALL", "Футбол", "⚽", 1, true),
    SportDefinitionEntity("RUNNING", "Біг", "🏃", 2, true),
    SportDefinitionEntity("TABLE_TENNIS", "Настільний теніс", "🏓", 3, true),
    SportDefinitionEntity("TENNIS", "Теніс", "🎾", 4, true),
    SportDefinitionEntity("SWIMMING", "Плавання", "🏊", 5, true),
    SportDefinitionEntity("CYCLING", "Велосипед", "🚴", 6, true)
)

fun insertBuiltinSportSeeds(database: SupportSQLiteDatabase) {
    builtinSportDefinitions().forEach { e ->
        database.execSQL(
            "INSERT OR IGNORE INTO sport_definitions (id, nameUa, emoji, sortOrder, isBuiltIn) VALUES (?, ?, ?, ?, 1)",
            arrayOf<Any>(e.id, e.nameUa, e.emoji, e.sortOrder)
        )
    }
}
