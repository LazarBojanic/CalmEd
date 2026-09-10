package com.calmed.calmedtics.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calmed.calmedtics.model.raw.ProgramExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IProgramExerciseDao {
	@Query("SELECT * FROM program_exercise ORDER BY week_number ASC")
	fun getAll(): Flow<List<ProgramExerciseEntity>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsertAll(exercises: List<ProgramExerciseEntity>)

	@Query("DELETE FROM program_exercise")
	suspend fun clearAll()
}
