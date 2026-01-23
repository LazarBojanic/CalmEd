package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.UserInfoTourettesUpdateDto
import com.calmed.calmedbackend.model.joined.UserInfoTourettesJoined
import com.calmed.calmedbackend.model.raw.userinfo.tourettes.UserInfoTourettes
import java.util.UUID

interface IUserInfoTourettesService {
	suspend fun getAll(): AppResult<List<UserInfoTourettesJoined>>
	suspend fun getById(id: UUID): AppResult<UserInfoTourettesJoined>
	suspend fun getByUserId(userId: UUID): AppResult<UserInfoTourettesJoined>
	suspend fun create(userInfoTourettes: UserInfoTourettes): AppResult<UserInfoTourettesJoined>
	suspend fun update(userInfoTourettes: UserInfoTourettes): AppResult<UserInfoTourettesJoined>
	suspend fun delete(id: UUID): AppResult<Unit>
	suspend fun updateById(id: UUID, dto: UserInfoTourettesUpdateDto): AppResult<UserInfoTourettesJoined>

}