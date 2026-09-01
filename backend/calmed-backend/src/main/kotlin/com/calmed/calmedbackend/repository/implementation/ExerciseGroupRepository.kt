package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.database.withTransaction
import com.calmed.calmedbackend.model.raw.exercisegroup.ExerciseGroup
import com.calmed.calmedbackend.model.raw.exercisegroup.ExerciseGroupEntity
import com.calmed.calmedbackend.model.toRaw
import com.calmed.calmedbackend.repository.specification.IExerciseGroupRepository

class ExerciseGroupRepository : IExerciseGroupRepository {
	override suspend fun findAll(): List<ExerciseGroup> {
		return withTransaction {
			ExerciseGroupEntity.all()
				.sortedBy { it.id.value }
				.map { it.toRaw() }
		}
	}
}
