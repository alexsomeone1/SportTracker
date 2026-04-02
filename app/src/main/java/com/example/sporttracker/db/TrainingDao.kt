package com.example.sporttracker.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Delete
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: TrainingEntity)

    @Query("SELECT * FROM trainings ORDER BY dateEpochDay DESC")
    fun observeAll(): Flow<List<TrainingEntity>>

    @Query("""
        SELECT * FROM trainings
        WHERE (:sport IS NULL OR sport = :sport)
          AND dateEpochDay BETWEEN :fromDay AND :toDay
        ORDER BY dateEpochDay DESC
    """)
    fun observeFiltered(
        sport: String?,
        fromDay: Long,
        toDay: Long
    ): Flow<List<TrainingEntity>>

    @Query("""
        SELECT COUNT(*) FROM trainings
        WHERE (:sport IS NULL OR sport = :sport)
          AND dateEpochDay BETWEEN :fromDay AND :toDay
    """)
    fun observeCount(
        sport: String?,
        fromDay: Long,
        toDay: Long
    ): Flow<Int>
    @Query("""
    SELECT COUNT(*) FROM trainings
    WHERE dateEpochDay BETWEEN :fromDay AND :toDay
""")
    fun observeCountForRange(fromDay: Long, toDay: Long): Flow<Int>

    @Query("""
    SELECT sport, COUNT(*) as cnt FROM trainings
    WHERE dateEpochDay BETWEEN :fromDay AND :toDay
    GROUP BY sport
""")
    fun observeCountBySportForRange(fromDay: Long, toDay: Long): Flow<List<SportCountRow>>
    @Query("SELECT * FROM trainings WHERE dateEpochDay BETWEEN :fromDay AND :toDay")
    fun observeForRange(fromDay: Long, toDay: Long): kotlinx.coroutines.flow.Flow<List<TrainingEntity>>

    @Delete
    suspend fun delete(item: TrainingEntity)

}

