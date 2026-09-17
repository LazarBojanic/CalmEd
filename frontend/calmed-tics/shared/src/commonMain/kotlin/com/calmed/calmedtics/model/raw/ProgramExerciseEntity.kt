package com.calmed.calmedtics.model.raw

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "program_exercise")
data class ProgramExerciseEntity(
	@PrimaryKey(autoGenerate = false)
	val id: String,
	@ColumnInfo(name = "week_number")
	val weekNumber: Int,
	@ColumnInfo(name = "group_id")
	val groupId: Int?,
	val title: String,
	val description: String?,
	@ColumnInfo(name = "token_480")
	val token480: String,
	@ColumnInfo(name = "token_720")
	val token720: String,
	@ColumnInfo(name = "token_1080")
	val token1080: String,
	@ColumnInfo(name = "preview_token")
	val previewToken: String,
	@ColumnInfo(name = "thumbnail_token")
	val thumbnailToken: String,
	@ColumnInfo(name = "preview_thumbnail_token")
	val previewThumbnailToken: String,
	@ColumnInfo(name = "playback_id")
	val playbackId: String,
	@ColumnInfo(name = "preview_playback_id")
	val previewPlaybackId: String,
	@ColumnInfo(name = "thumbnail_url")
	val thumbnailURL: String,
	@ColumnInfo(name = "preview_thumbnail_url")
	val previewThumbnailURL: String,
	@ColumnInfo(name = "duration_seconds")
	val durationSeconds: Int?,
	val visibility: String,
	@ColumnInfo(name = "created_at")
	val createdAt: String,
	@ColumnInfo(name = "updated_at")
	val updatedAt: String
)
