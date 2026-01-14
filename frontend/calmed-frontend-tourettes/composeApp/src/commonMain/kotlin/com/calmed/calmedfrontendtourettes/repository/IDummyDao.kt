package com.calmed.calmedfrontendtourettes.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calmed.calmedfrontendtourettes.model.raw.DummyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IDummyDao {
	@Query("SELECT * FROM dummy LIMIT 1")
	fun findFirst(): Flow<DummyEntity?>

	@Query("SELECT * FROM dummy")
	fun findAll(): Flow<List<DummyEntity>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsert(company: DummyEntity)

	@Delete
	suspend fun delete(company: DummyEntity)

	@Query("DELETE FROM dummy")
	suspend fun clearAll()
}