package com.calmed.calmedbackend.model.dto.request

import com.calmed.calmedbackend.model.raw.userinfo.tics.TickFrequency
import com.calmed.calmedbackend.model.raw.userinfo.tics.TickType
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UserInfoTicsUpdateDto(
	@Serializable(with = UUIDSerializer::class)
	val userId: UUID,
	val preferredName: String?,
	val age: Int?,
	val stressLevel: Int?,
	val tickType: TickType?,
	val tickFrequency: TickFrequency?,
	val goal: String?,
	val followProgress: Boolean?
)