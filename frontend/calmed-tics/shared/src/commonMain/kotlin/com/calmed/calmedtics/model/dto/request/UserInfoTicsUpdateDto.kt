package com.calmed.calmedtics.model.dto.request

import com.calmed.calmedtics.model.raw.TicFrequency
import com.calmed.calmedtics.model.raw.TicType
import kotlinx.serialization.Serializable
import com.calmed.calmedtics.model.raw.TicDuration

@Serializable
data class UserInfoTicsUpdateDto(
	val userId: String,
	val preferredName: String?,
	val age: Int?,
	val stressLevel: Int?,
	val ticType: TicType?,
	val ticFrequency: TicFrequency?,
	val ticDuration: TicDuration?,
	val goal: String?
)