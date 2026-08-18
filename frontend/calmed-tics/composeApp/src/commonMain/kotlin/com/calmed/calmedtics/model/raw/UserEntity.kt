package com.calmed.calmedtics.model.raw

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
	@PrimaryKey(autoGenerate = false)
	val id: String,
	@ColumnInfo(name = "email")
	val email: String,
	@ColumnInfo(name = "username")
	val username: String,
	val profileImageUrl: String? = null,
	@ColumnInfo(name = "is_email_verified")
	val isEmailVerified: Boolean,
	@ColumnInfo(name = "is_onboarded")
	val isOnboarded: Boolean,
	@ColumnInfo(name = "confirm_over_eighteen")
	val confirmOverEighteen: Boolean,
	@ColumnInfo(name = "created_at")
	val createdAt: String,
	@ColumnInfo(name = "updated_at")
	val updatedAt: String
)

