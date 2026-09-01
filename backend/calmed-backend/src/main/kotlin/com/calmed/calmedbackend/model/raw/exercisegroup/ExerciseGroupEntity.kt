package com.calmed.calmedbackend.model.raw.exercisegroup

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class ExerciseGroupEntity(id: EntityID<Int>) : IntEntity(id) {
	companion object : IntEntityClass<ExerciseGroupEntity>(ExerciseGroupTable)
	var name by ExerciseGroupTable.name
	var description by ExerciseGroupTable.description
}
