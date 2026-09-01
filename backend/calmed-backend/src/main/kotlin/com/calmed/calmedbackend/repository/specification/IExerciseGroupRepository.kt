package com.calmed.calmedbackend.repository.specification

import com.calmed.calmedbackend.model.raw.exercisegroup.ExerciseGroup

interface IExerciseGroupRepository {
	suspend fun findAll(): List<ExerciseGroup>
}
