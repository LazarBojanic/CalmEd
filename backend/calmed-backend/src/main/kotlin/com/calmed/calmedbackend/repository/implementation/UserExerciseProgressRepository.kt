package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.database.withTransaction
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
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import java.time.LocalDate
import java.util.UUID

class UserExerciseProgressRepository : IUserExerciseProgressRepository {
	override suspend fun findAll(): List<UserExerciseProgress> = withTransaction {
		UserExerciseProgressEntity.all().map { it.toRaw() }
	}

	override suspend fun findById(id: UUID): UserExerciseProgress? = withTransaction {
		UserExerciseProgressEntity.findById(id)?.toRaw()
	}

	override suspend fun findAllByUserId(userId: UUID): List<UserExerciseProgress> = withTransaction {
		UserExerciseProgressEntity.find { UserExerciseProgressTable.userId eq userId }.map { it.toRaw() }
	}

	override suspend fun create(progress: UserExerciseProgress): UserExerciseProgress? = withTransaction {
		val existing = UserExerciseProgressEntity.findById(progress.id)
		if (existing == null) {
			UserExerciseProgressEntity.new(progress.id) { setFrom(progress, MapMode.CREATE) }.toRaw()
		} else null
	}

	override suspend fun update(progress: UserExerciseProgress): UserExerciseProgress? = withTransaction {
		val e = UserExerciseProgressEntity.findById(progress.id)
		e?.apply { setFrom(progress, MapMode.UPDATE) }?.toRaw()
	}

	override suspend fun delete(id: UUID): Boolean = withTransaction {
		UserExerciseProgressEntity.findById(id)?.let { it.delete(); true } ?: false
	}

	override suspend fun deleteByCriteria(userId: UUID, exerciseId: UUID, session: ExerciseSession, day: LocalDate): Boolean = withTransaction {
		val found = UserExerciseProgressEntity.find {
			(UserExerciseProgressTable.userId eq userId) and
					(UserExerciseProgressTable.programExerciseId eq exerciseId) and
					(UserExerciseProgressTable.session eq session) and
					(UserExerciseProgressTable.day eq day)
		}
		if (found.empty()) false
		else {
			found.forEach { it.delete() }
			true
		}
	}

	override suspend fun findByCriteria(userId: UUID, exerciseId: UUID, session: ExerciseSession, day: LocalDate): UserExerciseProgress? = withTransaction {
		UserExerciseProgressEntity.find {
			(UserExerciseProgressTable.userId eq userId) and
					(UserExerciseProgressTable.programExerciseId eq exerciseId) and
					(UserExerciseProgressTable.session eq session) and
					(UserExerciseProgressTable.day eq day)
		}.firstOrNull()?.toRaw()
	}

	override suspend fun findAllByUserIdAndMonth(userId: UUID, year: Int, month: Int): List<UserExerciseProgress> = withTransaction {
		val start = LocalDate.of(year, month, 1)
		val end = start.plusMonths(1).minusDays(1)
		UserExerciseProgressEntity.find {
			(UserExerciseProgressTable.userId eq userId) and
					(UserExerciseProgressTable.day greaterEq start) and
					(UserExerciseProgressTable.day lessEq end)
		}.map { it.toRaw() }
	}
}