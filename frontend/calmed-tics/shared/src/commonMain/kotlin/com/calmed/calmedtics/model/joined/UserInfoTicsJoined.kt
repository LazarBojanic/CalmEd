package com.calmed.calmedtics.model.joined

import com.calmed.calmedtics.model.raw.TicFrequency
import com.calmed.calmedtics.model.raw.TicType
import kotlinx.serialization.Serializable
import com.calmed.calmedtics.model.raw.TicDuration

@Serializable
data class UserInfoTicsJoined(
	val id: String,
	val user: UserJoined,
	val preferredName: String?,
	val age: Int?,
	val stressLevel: Int?,
	val ticType: TicType?,
	val ticFrequency: TicFrequency?,
	val ticDuration: TicDuration?,
	val goal: String?,
	val createdAt: String,
	val updatedAt: String
)
