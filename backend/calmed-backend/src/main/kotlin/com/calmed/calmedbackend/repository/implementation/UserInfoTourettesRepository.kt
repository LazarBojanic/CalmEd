package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.database.withTransaction
import com.calmed.calmedbackend.model.MapMode
import com.calmed.calmedbackend.model.dto.request.UserInfoTicsUpdateDto
import com.calmed.calmedbackend.model.raw.userinfo.tics.UserInfoTics
import com.calmed.calmedbackend.model.raw.userinfo.tics.UserInfoTicsEntity
import com.calmed.calmedbackend.model.raw.userinfo.tics.UserInfoTicsTable
import com.calmed.calmedbackend.model.setFrom
import com.calmed.calmedbackend.model.toRaw
import com.calmed.calmedbackend.repository.specification.IUserInfoTicsRepository
import org.jetbrains.exposed.v1.core.eq
import java.time.Instant
import java.util.*

class UserInfoTicsRepository : IUserInfoTicsRepository {
	override suspend fun findAll(): List<UserInfoTics> {
		return withTransaction {
			UserInfoTicsEntity.all().map { it.toRaw() }
		}
	}

	override suspend fun findById(id: UUID): UserInfoTics? {
		return withTransaction {
			val e = UserInfoTicsEntity.findById(id)
			if (e != null) {
				return@withTransaction e.toRaw()
			}
			else {
				return@withTransaction null
			}
		}
	}

	override suspend fun findByUserId(userId: UUID): UserInfoTics? {
		return withTransaction {
			val e = UserInfoTicsEntity.find { UserInfoTicsTable.userId eq userId }.firstOrNull()
			if (e != null) {
				return@withTransaction e.toRaw()
			}
			else {
				return@withTransaction null
			}
		}
	}

	override suspend fun create(userInfoTics: UserInfoTics): UserInfoTics? {
		return withTransaction {
			val existing = UserInfoTicsEntity.findById(userInfoTics.id)
			if (existing == null) {
				return@withTransaction UserInfoTicsEntity.new(userInfoTics.id) {
					setFrom(userInfoTics, MapMode.CREATE)
				}.toRaw()
			}
			else {
				return@withTransaction null
			}
		}
	}

	override suspend fun update(userInfoTics: UserInfoTics): UserInfoTics? {
		return withTransaction {
			val e = UserInfoTicsEntity.findById(userInfoTics.id)
			if (e != null) {
				e.setFrom(userInfoTics, MapMode.UPDATE)
				return@withTransaction e.toRaw()
			}
			else {
				return@withTransaction null
			}
		}
	}

	override suspend fun delete(id: UUID): Boolean {
		return withTransaction {
			val e = UserInfoTicsEntity.findById(id)
			if (e != null) {
				e.delete()
				return@withTransaction true
			}
			else {
				return@withTransaction false
			}
		}
	}

	override suspend fun deleteByUserId(userId: UUID): Boolean {
		return withTransaction {
			val entities = UserInfoTicsEntity.find { UserInfoTicsTable.userId eq userId }
			var deleted = false
			for (e in entities) {
				e.delete()
				deleted = true
			}
			deleted
		}
	}

	override suspend fun updateById(id: UUID, dto: UserInfoTicsUpdateDto
	): UserInfoTics? {
		return withTransaction {
			val e = UserInfoTicsEntity.findById(id)
			if (e != null) {
				if (e.userId == dto.userId) {
					val raw = UserInfoTics(
						id = e.id.value,
						userId = e.userId,
						preferredName = dto.preferredName,
						age = dto.age,
						stressLevel = dto.stressLevel,
						tickType = dto.tickType,
						tickFrequency = dto.tickFrequency,
						ticDuration = dto.ticDuration,
						goal = dto.goal,
						createdAt = e.createdAt,
						updatedAt = Instant.now()
					)

					e.setFrom(raw, MapMode.UPDATE)
					return@withTransaction e.toRaw()
				}
				else {
					return@withTransaction null
				}
			}
			else {
				return@withTransaction null
			}
		}
	}
}