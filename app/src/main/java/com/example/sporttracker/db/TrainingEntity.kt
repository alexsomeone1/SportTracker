package com.example.sporttracker.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "trainings")
data class TrainingEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sport: String,        // наприклад "GYM"
    val dateEpochDay: Long,   // для фільтрів (число)
    val dateText: String      // для показу "13.01.2026"
)
