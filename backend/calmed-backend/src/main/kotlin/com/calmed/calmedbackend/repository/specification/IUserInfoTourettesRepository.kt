package com.calmed.calmedbackend.repository.specification

import com.calmed.calmedbackend.model.dto.request.UserInfoTicsUpdateDto
import com.calmed.calmedbackend.model.raw.userinfo.tics.UserInfoTics
import java.util.UUID

interface IUserInfoTicsRepository {
	suspend fun findAll(): List<UserInfoTics>
	suspend fun findById(id: UUID): UserInfoTics?
	suspend fun findByUserId(userId: UUID): UserInfoTics?
	suspend fun create(userInfoTics: UserInfoTics): UserInfoTics?
	suspend fun update(userInfoTics: UserInfoTics): UserInfoTics?
	suspend fun delete(id: UUID): Boolean
	suspend fun deleteByUserId(userId: UUID): Boolean
	suspend fun updateById(id: UUID, dto: UserInfoTicsUpdateDto): UserInfoTics?

}