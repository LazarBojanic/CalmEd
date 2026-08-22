package com.calmed.calmedtics.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calmed.calmedtics.model.raw.ExerciseCompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IExerciseCompletionDao {
    @Query("SELECT * FROM exercise_completion WHERE userId = :userId AND week = :week")
    fun getCompletionForWeek(userId: String, week: Int): Flow<List<ExerciseCompletionEntity>>

    @Query("SELECT * FROM exercise_completion WHERE week = :week AND userId = :userId AND day = :day AND session = :session LIMIT 1")
    suspend fun findExisting(week: Int, userId: String, day: Int, session: String): ExerciseCompletionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(completion: ExerciseCompletionEntity)

    @Query("DELETE FROM exercise_completion WHERE week = :week AND userId = :userId AND day = :day AND session = :session")
    suspend fun delete(week: Int, userId: String, day: Int, session: String)

    @Query("SELECT * FROM exercise_completion")
    fun getAllCompletions(): Flow<List<ExerciseCompletionEntity>>

    @Query("SELECT * FROM exercise_completion WHERE userId = :userId")
    suspend fun getAllForUser(userId: String): List<ExerciseCompletionEntity>

    @Query("DELETE FROM exercise_completion")
    suspend fun clearAll()
}
