package com.calmed.calmedtics.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calmed.calmedtics.model.raw.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IUserDao {
	@Query("SELECT * FROM user LIMIT 1")
	fun findFirst(): Flow<UserEntity?>

	@Query("SELECT * FROM user")
	fun findAll(): Flow<List<UserEntity>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsert(company: UserEntity)

	@Delete
	suspend fun delete(company: UserEntity)

	@Query("DELETE FROM user")
	suspend fun clearAll()
}