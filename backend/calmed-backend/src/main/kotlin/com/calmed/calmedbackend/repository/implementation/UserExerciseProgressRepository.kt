package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.model.MapMode
import com.calmed.calmedbackend.model.raw.userexerciseprogress.ExerciseSession
import com.calmed.calmedbackend.model.raw.userexerciseprogress.UserExerciseProgress
import com.calmed.calmedbackend.model.raw.userexerciseprogress.UserExerciseProgressEntity
import com.calmed.calmedbackend.model.raw.userexerciseprogress.UserExerciseProgressTable
import com.calmed.calmedbackend.model.setFrom
import com.calmed.calmedbackend.model.toRaw
import com.calmed.calmedbackend.repository.specification.IUserExerciseProgressRepository
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import java.util.UUID

class UserExerciseProgressRepository : IUserExerciseProgressRepository {
	override suspend fun findAll(): List<UserExerciseProgress> =
		UserExerciseProgressEntity.all().map { it.toRaw() }

	override suspend fun findById(id: UUID): UserExerciseProgress? =
		UserExerciseProgressEntity.findById(id)?.toRaw()

	override suspend fun findAllByUserId(userId: UUID): List<UserExerciseProgress> =
		UserExerciseProgressEntity.find { UserExerciseProgressTable.userId eq userId }.map { it.toRaw() }

	override suspend fun create(progress: UserExerciseProgress): UserExerciseProgress? {
		if (UserExerciseProgressEntity.findById(progress.id) != null) return null
		return UserExerciseProgressEntity.new(progress.id) { setFrom(progress, MapMode.CREATE) }.toRaw()
	}

	override suspend fun createBatch(progresses: List<UserExerciseProgress>): Int {
		var inserted = 0
		for (progress in progresses) {
			if (UserExerciseProgressEntity.findById(progress.id) == null) {
				UserExerciseProgressEntity.new(progress.id) { setFrom(progress, MapMode.CREATE) }
				inserted++
			}
		}
		return inserted
	}

	override suspend fun update(progress: UserExerciseProgress): UserExerciseProgress? {
		val e = UserExerciseProgressEntity.findById(progress.id) ?: return null
		e.setFrom(progress, MapMode.UPDATE)
		return e.toRaw()
	}

	override suspend fun delete(id: UUID): Boolean {
		val e = UserExerciseProgressEntity.findById(id) ?: return false
		e.delete()
		return true
	}

	override suspend fun deleteByUserId(userId: UUID): Boolean {
		val entities = UserExerciseProgressEntity.find { UserExerciseProgressTable.userId eq userId }
		var deleted = false
		for (e in entities) {
			e.delete()
			deleted = true
		}
		return deleted
	}

	override suspend fun deleteByCriteria(userId: UUID, week: Int, day: Int, session: ExerciseSession): Boolean {
		val found = UserExerciseProgressEntity.find {
			(UserExerciseProgressTable.userId eq userId) and
					(UserExerciseProgressTable.week eq week) and
					(UserExerciseProgressTable.day eq day) and
					(UserExerciseProgressTable.exerciseSession eq session)
		}
		if (found.empty()) return false
		found.forEach { it.delete() }
		return true
	}

	override suspend fun findByCriteria(userId: UUID, week: Int, day: Int, session: ExerciseSession): UserExerciseProgress? {
		return UserExerciseProgressEntity.find {
			(UserExerciseProgressTable.userId eq userId) and
					(UserExerciseProgressTable.week eq week) and
					(UserExerciseProgressTable.day eq day) and
					(UserExerciseProgressTable.exerciseSession eq session)
		}.firstOrNull()?.toRaw()
	}

	override suspend fun findAllByUserIdAndWeek(userId: UUID, week: Int): List<UserExerciseProgress> {
		return UserExerciseProgressEntity.find {
			(UserExerciseProgressTable.userId eq userId) and
					(UserExerciseProgressTable.week eq week)
		}.map { it.toRaw() }
	}
}
