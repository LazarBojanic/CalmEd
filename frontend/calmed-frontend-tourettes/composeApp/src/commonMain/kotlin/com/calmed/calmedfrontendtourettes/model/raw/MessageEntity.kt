package com.calmed.calmedfrontendtourettes.model.raw

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "message")
data class MessageEntity(
	@PrimaryKey(autoGenerate = false)
	val id: String,
	@ColumnInfo(name = "text")
	val text: String?,
	@ColumnInfo(name = "created_at")
	val createdAt: String?,
	@ColumnInfo(name = "updated_at")
	val updatedAt: String?,
)