package com.calmed.calmedtics.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calmed.calmedtics.model.raw.ExerciseCompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IExerciseCompletionDao {
    @Query("SELECT * FROM exercise_completion WHERE userId = :userId AND date = :date")
    fun getCompletionForDay(userId: String, date: String): Flow<List<ExerciseCompletionEntity>>

    @Query("SELECT * FROM exercise_completion WHERE exerciseId = :exerciseId AND userId = :userId AND date = :date AND session = :session LIMIT 1")
    suspend fun findExisting(exerciseId: String, userId: String, date: String, session: String): ExerciseCompletionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(completion: ExerciseCompletionEntity)

    @Query("DELETE FROM exercise_completion WHERE exerciseId = :exerciseId AND userId = :userId AND date = :date AND session = :session")
    suspend fun delete(exerciseId: String, userId: String, date: String, session: String)

    @Query("SELECT * FROM exercise_completion")
    fun getAllCompletions(): Flow<List<ExerciseCompletionEntity>>

    @Query("DELETE FROM exercise_completion")
    suspend fun clearAll()
}
