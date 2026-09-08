package com.calmed.calmedbackend.model.dto.request

import com.calmed.calmedbackend.model.raw.userinfo.tics.TicFrequency
import com.calmed.calmedbackend.model.raw.userinfo.tics.TicType
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID
import com.calmed.calmedbackend.model.raw.userinfo.tics.TicDuration

@Serializable
data class UserInfoTicsUpdateDto(
	@Serializable(with = UUIDSerializer::class)
	val userId: UUID,
	val preferredName: String?,
	val age: Int?,
	val stressLevel: Int?,
	val ticType: TicType?,
	val ticFrequency: TicFrequency?,
	val ticDuration: TicDuration?,
	val goal: String?
)