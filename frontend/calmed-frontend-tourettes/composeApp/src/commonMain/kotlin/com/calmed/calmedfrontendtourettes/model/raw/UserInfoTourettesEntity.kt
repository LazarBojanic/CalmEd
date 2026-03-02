package com.calmed.calmedfrontendtourettes.model.raw

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_info_tourettes")
data class UserInfoTourettesEntity(
	@PrimaryKey(autoGenerate = false)
	val id: String,
	@ColumnInfo(name = "userId")
	val userId: String,
	@ColumnInfo(name = "preferred_name")
	val preferredName: String?,
	@ColumnInfo(name = "age")
	val age: Int?,
	@ColumnInfo(name = "stress_level")
	val stressLevel: Int?,
	@ColumnInfo(name = "tick_type")
	val tickType: TickType?,
	@ColumnInfo(name = "tick_frequency")
	val tickFrequency: TickFrequency?,
	@ColumnInfo(name = "goal")
	val goal: String?,
	@ColumnInfo(name = "follow_progress")
	val followProgress: Boolean?,
	@ColumnInfo(name = "created_at")
	val createdAt: String,
	@ColumnInfo(name = "updated_at")
	val updatedAt: String
)
