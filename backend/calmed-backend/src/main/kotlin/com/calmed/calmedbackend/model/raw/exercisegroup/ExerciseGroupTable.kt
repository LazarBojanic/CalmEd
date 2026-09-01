package com.calmed.calmedbackend.model.raw.exercisegroup

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object ExerciseGroupTable : IntIdTable("exercise_group") {
	val name = text("name")
	val description = text("description").nullable()
}
