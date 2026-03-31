package com.calmed.calmedbackend.model.dto.response

import com.calmed.calmedbackend.model.raw.userinfo.tics.TickFrequency
import com.calmed.calmedbackend.model.raw.userinfo.tics.TickType
import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class UserInfoTicsDto(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val user: UserDto,
	val preferredName: String?,
	val age: Int?,
	val stressLevel: Int?,
	val tickType: TickType?,
	val tickFrequency:TickFrequency?,
	val goal: String?,
	val followProgress: Boolean?,
	@Serializable(with = InstantSerializer::class)
	val createdAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val updatedAt: Instant
)
