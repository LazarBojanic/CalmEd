package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.response.ExerciseGroupDto

interface IExerciseGroupService {
	suspend fun getAll(): AppResult<List<ExerciseGroupDto>>
}
