package com.calmed.calmedtics.repository

import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.model.dto.response.ExerciseGroupDto
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.model.toDto
import com.calmed.calmedtics.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExercisesRepository(
	private val api: IAppApi,
	private val programExerciseDao: IProgramExerciseDao,
	private val exerciseGroupDao: IExerciseGroupDao
) {
	val exercises: Flow<List<ProgramExerciseDto>> =
		programExerciseDao.getAll().map { list -> list.map { it.toDto() } }

	val groups: Flow<List<ExerciseGroupDto>> =
		exerciseGroupDao.getAll().map { list -> list.map { it.toDto() } }

	suspend fun refresh() {
		val remoteExercises = api.getAllProgramExercises()
		val remoteGroups = api.getAllExerciseGroups()

		if (remoteExercises.isNotEmpty()) {
			programExerciseDao.clearAll()
			programExerciseDao.upsertAll(remoteExercises.map { it.toEntity() })
		}
		if (remoteGroups.isNotEmpty()) {
			exerciseGroupDao.clearAll()
			exerciseGroupDao.upsertAll(remoteGroups.map { it.toEntity() })
		}
	}
}
