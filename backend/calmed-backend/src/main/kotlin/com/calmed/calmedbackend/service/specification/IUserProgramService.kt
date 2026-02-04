package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.joined.UserProgramJoined
import com.calmed.calmedbackend.model.raw.userprogram.UserProgram
import java.util.UUID

interface IUserProgramService {
	suspend fun getAll(): AppResult<List<UserProgramJoined>>
	suspend fun getById(id: UUID): AppResult<UserProgramJoined>
	suspend fun getByUserId(userId: UUID): AppResult<UserProgramJoined>
	suspend fun create(userProgram: UserProgram): AppResult<UserProgramJoined>
	suspend fun update(userProgram: UserProgram): AppResult<UserProgramJoined>
	suspend fun delete(id: UUID): AppResult<Unit>
}