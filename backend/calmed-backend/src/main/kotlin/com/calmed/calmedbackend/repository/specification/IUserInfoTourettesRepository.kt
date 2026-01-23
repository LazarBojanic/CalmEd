package com.calmed.calmedbackend.repository.specification

import com.calmed.calmedbackend.model.dto.request.UserInfoTourettesUpdateDto
import com.calmed.calmedbackend.model.raw.userinfo.tourettes.UserInfoTourettes
import java.util.UUID

interface IUserInfoTourettesRepository {
	suspend fun findAll(): List<UserInfoTourettes>
	suspend fun findById(id: UUID): UserInfoTourettes?
	suspend fun findByUserId(userId: UUID): UserInfoTourettes?
	suspend fun create(userInfoTourettes: UserInfoTourettes): UserInfoTourettes?
	suspend fun update(userInfoTourettes: UserInfoTourettes): UserInfoTourettes?
	suspend fun delete(id: UUID): Boolean
	suspend fun updateById(id: UUID, dto: UserInfoTourettesUpdateDto): UserInfoTourettes?

}