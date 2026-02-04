package com.calmed.calmedbackend.repository.specification

import com.calmed.calmedbackend.model.raw.userexerciseprogress.UserExerciseProgress
import java.util.UUID

interface IUserExerciseProgressRepository {
	suspend fun findAll(): List<UserExerciseProgress>
	suspend fun findById(id: UUID): UserExerciseProgress?
	suspend fun findAllByUserId(userId: UUID): List<UserExerciseProgress>
	suspend fun create(progress: UserExerciseProgress): UserExerciseProgress?
	suspend fun update(progress: UserExerciseProgress): UserExerciseProgress?
	suspend fun delete(id: UUID): Boolean
}