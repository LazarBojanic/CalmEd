package com.calmed.calmedbackend.repository.specification

import com.calmed.calmedbackend.model.raw.userexerciseprogress.ExerciseSession
import com.calmed.calmedbackend.model.raw.userexerciseprogress.UserExerciseProgress
import java.time.LocalDate
import java.util.UUID

interface IUserExerciseProgressRepository {
	suspend fun findAll(): List<UserExerciseProgress>
	suspend fun findById(id: UUID): UserExerciseProgress?
	suspend fun findAllByUserId(userId: UUID): List<UserExerciseProgress>
	suspend fun create(progress: UserExerciseProgress): UserExerciseProgress?
	suspend fun update(progress: UserExerciseProgress): UserExerciseProgress?
	suspend fun delete(id: UUID): Boolean
	suspend fun deleteByCriteria(userId: UUID, exerciseId: UUID, session: ExerciseSession, day: LocalDate): Boolean
	suspend fun findByCriteria(userId: UUID, exerciseId: UUID, session: ExerciseSession, day: LocalDate): UserExerciseProgress?
	suspend fun findAllByUserIdAndMonth(userId: UUID, year: Int, month: Int): List<UserExerciseProgress>
}