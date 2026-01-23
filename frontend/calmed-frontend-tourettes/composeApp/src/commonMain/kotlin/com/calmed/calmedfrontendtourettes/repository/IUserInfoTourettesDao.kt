package com.calmed.calmedfrontendtourettes.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calmed.calmedfrontendtourettes.model.raw.UserInfoTourettesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IUserInfoTourettesDao {
	@Query("SELECT * FROM user_info_tourettes LIMIT 1")
	fun findFirst(): Flow<UserInfoTourettesEntity?>

	@Query("SELECT * FROM user_info_tourettes")
	fun findAll(): Flow<List<UserInfoTourettesEntity>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsert(company: UserInfoTourettesEntity)

	@Delete
	suspend fun delete(company: UserInfoTourettesEntity)

	@Query("DELETE FROM user_info_tourettes")
	suspend fun clearAll()
}