package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.response.ExerciseGroupDto
import com.calmed.calmedbackend.model.toDto
import com.calmed.calmedbackend.repository.specification.IExerciseGroupRepository
import com.calmed.calmedbackend.service.specification.IExerciseGroupService

class ExerciseGroupService(private val repository: IExerciseGroupRepository) : IExerciseGroupService {
	override suspend fun getAll(): AppResult<List<ExerciseGroupDto>> {
		return AppResult.Success(repository.findAll().map { it.toDto() })
	}
}
