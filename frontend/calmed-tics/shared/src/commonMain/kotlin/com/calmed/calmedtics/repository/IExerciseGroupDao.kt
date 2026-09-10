package com.calmed.calmedtics.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calmed.calmedtics.model.raw.ExerciseGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IExerciseGroupDao {
	@Query("SELECT * FROM exercise_group ORDER BY id ASC")
	fun getAll(): Flow<List<ExerciseGroupEntity>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsertAll(groups: List<ExerciseGroupEntity>)

	@Query("DELETE FROM exercise_group")
	suspend fun clearAll()
}
