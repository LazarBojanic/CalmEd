package com.calmed.calmedbackend.model.dto.response

import com.calmed.calmedbackend.model.raw.userinfo.tics.TicFrequency
import com.calmed.calmedbackend.model.raw.userinfo.tics.TicType
import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID
import com.calmed.calmedbackend.model.raw.userinfo.tics.TicDuration

@Serializable
data class UserInfoTicsDto(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val user: UserDto,
	val preferredName: String?,
	val age: Int?,
	val stressLevel: Int?,
	val ticType: TicType?,
	val ticFrequency:TicFrequency?,
	val ticDuration: TicDuration?,
	val goal: String?,
	@Serializable(with = InstantSerializer::class)
	val createdAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val updatedAt: Instant
)
