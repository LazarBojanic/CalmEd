package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.database.withTransaction
import com.calmed.calmedbackend.model.MapMode
import com.calmed.calmedbackend.model.raw.userinfo.UserInfoTourettes
import com.calmed.calmedbackend.model.raw.userinfo.UserInfoTourettesEntity
import com.calmed.calmedbackend.model.raw.userinfo.UserInfoTourettesTable
import com.calmed.calmedbackend.model.setFrom
import com.calmed.calmedbackend.model.toRaw
import com.calmed.calmedbackend.repository.specification.IUserInfoTourettesRepository
import org.jetbrains.exposed.v1.core.eq
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
			val e = UserInfoTourettesEntity
				.find { UserInfoTourettesTable.userId eq userId }
				.firstOrNull()
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
}