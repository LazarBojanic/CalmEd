package com.calmed.calmedtics.model.raw

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_completion")
data class ExerciseCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "exerciseId")
    val exerciseId: String,
    @ColumnInfo(name = "userId")
    val userId: String,
    @ColumnInfo(name = "date")
    val date: String, // YYYY-MM-DD
    @ColumnInfo(name = "session")
    val session: String, // "morning" or "evening"
    @ColumnInfo(name = "completed")
    val completed: Boolean,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)
