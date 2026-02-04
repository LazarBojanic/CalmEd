package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.database.withTransaction
import com.calmed.calmedbackend.model.MapMode
import com.calmed.calmedbackend.model.raw.userexerciseprogress.UserExerciseProgress
import com.calmed.calmedbackend.model.raw.userexerciseprogress.UserExerciseProgressEntity
import com.calmed.calmedbackend.model.raw.userexerciseprogress.UserExerciseProgressTable
import com.calmed.calmedbackend.model.setFrom
import com.calmed.calmedbackend.model.toRaw
import com.calmed.calmedbackend.repository.specification.IUserExerciseProgressRepository
import org.jetbrains.exposed.v1.core.eq
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
}