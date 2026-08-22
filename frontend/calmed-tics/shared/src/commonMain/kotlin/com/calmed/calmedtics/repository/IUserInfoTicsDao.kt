package com.calmed.calmedtics.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calmed.calmedtics.model.raw.UserInfoTicsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IUserInfoTicsDao {
	@Query("SELECT * FROM user_info_tics LIMIT 1")
	fun findFirst(): Flow<UserInfoTicsEntity?>

	@Query("SELECT * FROM user_info_tics")
	fun findAll(): Flow<List<UserInfoTicsEntity>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsert(company: UserInfoTicsEntity)

	@Delete
	suspend fun delete(company: UserInfoTicsEntity)

	@Query("DELETE FROM user_info_tics")
	suspend fun clearAll()
}