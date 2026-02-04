package com.calmed.calmedbackend.model.dto.response

import com.calmed.calmedbackend.util.LocalDateSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import com.calmed.calmedbackend.util.ZoneIdSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@Serializable
data class UserProgramDto(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val user: UserDto,
	@Serializable(with = LocalDateSerializer::class)
	val startDate: LocalDate,
	@Serializable(with = LocalDateSerializer::class)
	val endDate: LocalDate?,
	@Serializable(with = ZoneIdSerializer::class)
	val timezone: ZoneId?
)