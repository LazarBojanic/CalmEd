package com.calmed.calmedfrontendtourettes.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calmed.calmedfrontendtourettes.model.raw.Message
import com.calmed.calmedfrontendtourettes.model.raw.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IMessageDao {
	@Query("SELECT * FROM message LIMIT 1")
	fun findFirst(): Flow<MessageEntity?>

	@Query("SELECT * FROM message")
	fun findAll(): Flow<List<MessageEntity>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsert(company: MessageEntity)

	@Delete
	suspend fun delete(company: MessageEntity)

	@Query("DELETE FROM message")
	suspend fun clearAll()
}