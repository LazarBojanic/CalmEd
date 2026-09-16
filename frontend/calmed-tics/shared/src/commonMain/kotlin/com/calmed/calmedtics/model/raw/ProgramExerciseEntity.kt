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
	val token: String,
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
	val url: String,
	@ColumnInfo(name = "preview_url")
	val previewURL: String,
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
