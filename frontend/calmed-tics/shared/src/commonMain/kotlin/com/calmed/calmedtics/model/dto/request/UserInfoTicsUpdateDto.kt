package com.calmed.calmedtics.model.dto.request

import com.calmed.calmedtics.model.raw.TickFrequency
import com.calmed.calmedtics.model.raw.TickType
import kotlinx.serialization.Serializable
import com.calmed.calmedtics.model.raw.TicDuration

@Serializable
data class UserInfoTicsUpdateDto(
	val userId: String,
	val preferredName: String?,
	val age: Int?,
	val stressLevel: Int?,
	val tickType: TickType?,
	val tickFrequency: TickFrequency?,
	val ticDuration: TicDuration?,
	val goal: String?,
	val followProgress: Boolean?
)