package com.example.sporttracker.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SportDefinitionDao {

    @Query("SELECT * FROM sport_definitions ORDER BY sortOrder ASC, nameUa ASC")
    fun observeAll(): Flow<List<SportDefinitionEntity>>

    @Query("SELECT * FROM sport_definitions ORDER BY sortOrder ASC, nameUa ASC")
    suspend fun getAll(): List<SportDefinitionEntity>

    @Query("SELECT * FROM sport_definitions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SportDefinitionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: SportDefinitionEntity)

    @Delete
    suspend fun delete(item: SportDefinitionEntity)

    @Query("SELECT COUNT(*) FROM trainings WHERE sport = :sportId")
    suspend fun countTrainingsUsingSport(sportId: String): Int

    @Query("SELECT COALESCE(MAX(sortOrder), 0) FROM sport_definitions")
    suspend fun maxSortOrder(): Int
}
