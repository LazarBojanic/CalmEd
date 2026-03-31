package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.UserInfoTicsUpdateDto
import com.calmed.calmedbackend.model.joined.UserInfoTicsJoined
import com.calmed.calmedbackend.model.raw.userinfo.tics.UserInfoTics
import java.util.UUID

interface IUserInfoTicsService {
	suspend fun getAll(): AppResult<List<UserInfoTicsJoined>>
	suspend fun getById(id: UUID): AppResult<UserInfoTicsJoined>
	suspend fun getByUserId(userId: UUID): AppResult<UserInfoTicsJoined>
	suspend fun create(userInfoTics: UserInfoTics): AppResult<UserInfoTicsJoined>
	suspend fun update(userInfoTics: UserInfoTics): AppResult<UserInfoTicsJoined>
	suspend fun delete(id: UUID): AppResult<Unit>
	suspend fun updateById(id: UUID, dto: UserInfoTicsUpdateDto): AppResult<UserInfoTicsJoined>

}