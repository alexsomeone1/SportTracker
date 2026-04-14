package com.example.sporttracker.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sport_definitions")
data class SportDefinitionEntity(
    @PrimaryKey val id: String,
    val nameUa: String,
    val emoji: String,
    val sortOrder: Int,
    val isBuiltIn: Boolean
)
