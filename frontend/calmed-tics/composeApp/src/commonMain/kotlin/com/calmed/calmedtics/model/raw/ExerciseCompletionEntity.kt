package com.calmed.calmedtics.model.raw

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_completion")
data class ExerciseCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "week")
    val week: Int,
    @ColumnInfo(name = "day")
    val day: Int, // 1-7
    @ColumnInfo(name = "userId")
    val userId: String,
    @ColumnInfo(name = "session")
    val session: String, // "MORNING" or "EVENING"
    @ColumnInfo(name = "completed")
    val completed: Boolean,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)
