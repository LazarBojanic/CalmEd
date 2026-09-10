package com.calmed.calmedtics.model.raw

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_group")
data class ExerciseGroupEntity(
	@PrimaryKey(autoGenerate = false)
	val id: Int,
	val name: String,
	@ColumnInfo(name = "description")
	val description: String?
)
