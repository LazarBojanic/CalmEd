package com.calmed.calmedbackend.repository.specification

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.raw.user.User
import com.calmed.calmedbackend.model.raw.userinfo.UserInfoTourettes
import java.util.UUID

interface IUserInfoTourettesRepository {
	suspend fun findAll(): List<UserInfoTourettes>
	suspend fun findById(id: UUID): UserInfoTourettes?
	suspend fun findByUserId(userId: UUID): UserInfoTourettes?
	suspend fun create(userInfoTourettes: UserInfoTourettes): UserInfoTourettes?
	suspend fun update(userInfoTourettes: UserInfoTourettes): UserInfoTourettes?
	suspend fun delete(id: UUID): Boolean
}