package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.joined.UserExerciseProgressJoined
import com.calmed.calmedbackend.model.raw.userexerciseprogress.UserExerciseProgress
import java.time.LocalDate
import java.util.UUID

interface IUserExerciseProgressService {
	suspend fun getAll(): AppResult<List<UserExerciseProgressJoined>>
	suspend fun getById(id: UUID): AppResult<UserExerciseProgressJoined>
	suspend fun getAllByUserId(userId: UUID): AppResult<List<UserExerciseProgressJoined>>
	suspend fun create(progress: UserExerciseProgress): AppResult<UserExerciseProgressJoined>
	suspend fun initializeUserProgress(userId: UUID, startDate: LocalDate): AppResult<Unit>
	suspend fun update(progress: UserExerciseProgress): AppResult<UserExerciseProgressJoined>
	suspend fun delete(id: UUID): AppResult<Unit>
	suspend fun syncProgress(userId: UUID, dto: com.calmed.calmedbackend.model.dto.request.UserExerciseProgressUpdateDto): AppResult<Unit>
}