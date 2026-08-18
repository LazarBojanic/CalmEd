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
	suspend fun deleteByUserId(userId: UUID): Boolean
	suspend fun deleteByCriteria(userId: UUID, week: Int, day: Int, session: ExerciseSession): Boolean
	suspend fun findByCriteria(userId: UUID, week: Int, day: Int, session: ExerciseSession): UserExerciseProgress?
	suspend fun findAllByUserIdAndWeek(userId: UUID, week: Int): List<UserExerciseProgress>
}