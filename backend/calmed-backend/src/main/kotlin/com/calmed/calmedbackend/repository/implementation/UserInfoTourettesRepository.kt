package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.database.withTransaction
import com.calmed.calmedbackend.model.MapMode
import com.calmed.calmedbackend.model.dto.request.UserInfoTourettesUpdateDto
import com.calmed.calmedbackend.model.raw.userinfo.tourettes.UserInfoTourettes
import com.calmed.calmedbackend.model.raw.userinfo.tourettes.UserInfoTourettesEntity
import com.calmed.calmedbackend.model.raw.userinfo.tourettes.UserInfoTourettesTable
import com.calmed.calmedbackend.model.setFrom
import com.calmed.calmedbackend.model.toRaw
import com.calmed.calmedbackend.repository.specification.IUserInfoTourettesRepository
import org.jetbrains.exposed.v1.core.eq
import java.time.Instant
import java.util.*

class UserInfoTourettesRepository : IUserInfoTourettesRepository {
	override suspend fun findAll(): List<UserInfoTourettes> {
		return withTransaction {
			UserInfoTourettesEntity.all().map { it.toRaw() }
		}
	}

	override suspend fun findById(id: UUID): UserInfoTourettes? {
		return withTransaction {
			val e = UserInfoTourettesEntity.findById(id)
			if (e != null) {
				return@withTransaction e.toRaw()
			}
			else {
				return@withTransaction null
			}
		}
	}

	override suspend fun findByUserId(userId: UUID): UserInfoTourettes? {
		return withTransaction {
			val e = UserInfoTourettesEntity.find { UserInfoTourettesTable.userId eq userId }.firstOrNull()
			if (e != null) {
				return@withTransaction e.toRaw()
			}
			else {
				return@withTransaction null
			}
		}
	}

	override suspend fun create(userInfoTourettes: UserInfoTourettes): UserInfoTourettes? {
		return withTransaction {
			val existing = UserInfoTourettesEntity.findById(userInfoTourettes.id)
			if (existing == null) {
				return@withTransaction UserInfoTourettesEntity.new(userInfoTourettes.id) {
					setFrom(userInfoTourettes, MapMode.CREATE)
				}.toRaw()
			}
			else {
				return@withTransaction null
			}
		}
	}

	override suspend fun update(userInfoTourettes: UserInfoTourettes): UserInfoTourettes? {
		return withTransaction {
			val e = UserInfoTourettesEntity.findById(userInfoTourettes.id)
			if (e != null) {
				e.setFrom(userInfoTourettes, MapMode.UPDATE)
				return@withTransaction e.toRaw()
			}
			else {
				return@withTransaction null
			}
		}
	}

	override suspend fun delete(id: UUID): Boolean {
		return withTransaction {
			val e = UserInfoTourettesEntity.findById(id)
			if (e != null) {
				e.delete()
				return@withTransaction true
			}
			else {
				return@withTransaction false
			}
		}
	}

	override suspend fun updateById(id: UUID, dto: UserInfoTourettesUpdateDto
	): UserInfoTourettes? {
		return withTransaction {
			val e = UserInfoTourettesEntity.findById(id)
			if (e != null) {
				if (e.userId == dto.userId) {
					val raw = UserInfoTourettes(
						id = e.id.value,
						userId = e.userId,
						preferredName = dto.preferredName,
						age = dto.age,
						stressLevel = dto.stressLevel,
						tickType = dto.tickType,
						tickFrequency = dto.tickFrequency,
						goal = dto.goal,
						followProgress = dto.followProgress,
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