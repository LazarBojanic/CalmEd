package com.calmed.calmedtics.model.joined

import com.calmed.calmedtics.model.raw.TickFrequency
import com.calmed.calmedtics.model.raw.TickType
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoTicsJoined(
	val id: String,
	val user: UserJoined,
	val preferredName: String?,
	val age: Int?,
	val stressLevel: Int?,
	val tickType: TickType?,
	val tickFrequency: TickFrequency?,
	val goal: String?,
	val followProgress: Boolean?,
	val createdAt: String,
	val updatedAt: String
)
