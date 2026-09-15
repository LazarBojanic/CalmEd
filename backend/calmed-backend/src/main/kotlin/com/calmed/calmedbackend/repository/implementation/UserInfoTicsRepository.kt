package com.calmed.calmedbackend.repository.implementation

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
		return UserInfoTicsEntity.all().map { it.toRaw() }
	}

	override suspend fun findById(id: UUID): UserInfoTics? {
		return UserInfoTicsEntity.findById(id)?.toRaw()
	}

	override suspend fun findByUserId(userId: UUID): UserInfoTics? {
		return UserInfoTicsEntity.find { UserInfoTicsTable.userId eq userId }.firstOrNull()?.toRaw()
	}

	override suspend fun create(userInfoTics: UserInfoTics): UserInfoTics? {
		if (UserInfoTicsEntity.findById(userInfoTics.id) != null) return null
		return UserInfoTicsEntity.new(userInfoTics.id) {
			setFrom(userInfoTics, MapMode.CREATE)
		}.toRaw()
	}

	override suspend fun update(userInfoTics: UserInfoTics): UserInfoTics? {
		val e = UserInfoTicsEntity.findById(userInfoTics.id) ?: return null
		e.setFrom(userInfoTics, MapMode.UPDATE)
		return e.toRaw()
	}

	override suspend fun delete(id: UUID): Boolean {
		val e = UserInfoTicsEntity.findById(id) ?: return false
		e.delete()
		return true
	}

	override suspend fun deleteByUserId(userId: UUID): Boolean {
		val entities = UserInfoTicsEntity.find { UserInfoTicsTable.userId eq userId }
		var deleted = false
		for (e in entities) {
			e.delete()
			deleted = true
		}
		return deleted
	}

	override suspend fun updateById(id: UUID, dto: UserInfoTicsUpdateDto): UserInfoTics? {
		val e = UserInfoTicsEntity.findById(id) ?: return null
		if (e.userId != dto.userId) return null
		val raw = UserInfoTics(
			id = e.id.value,
			userId = e.userId,
			preferredName = dto.preferredName,
			age = dto.age,
			stressLevel = dto.stressLevel,
			ticType = dto.ticType,
			ticFrequency = dto.ticFrequency,
			ticDuration = dto.ticDuration,
			goal = dto.goal,
			createdAt = e.createdAt,
			updatedAt = Instant.now()
		)
		e.setFrom(raw, MapMode.UPDATE)
		return e.toRaw()
	}
}
