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
	@ColumnInfo(name = "playback_id")
	val playbackId: String?,
	@ColumnInfo(name = "preview_playback_id")
	val previewPlaybackId: String?,
	@ColumnInfo(name = "preview_video_url")
	val previewVideoURL: String?,
	@ColumnInfo(name = "video_url")
	val videoURL: String?,
	@ColumnInfo(name = "thumbnail_url")
	val thumbnailURL: String?,
	@ColumnInfo(name = "duration_seconds")
	val durationSeconds: Int?,
	val visibility: String,
	@ColumnInfo(name = "created_at")
	val createdAt: String,
	@ColumnInfo(name = "updated_at")
	val updatedAt: String
)
