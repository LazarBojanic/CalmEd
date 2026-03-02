package com.calmed.calmedfrontendtourettes.model.joined

import com.calmed.calmedfrontendtourettes.model.raw.TickFrequency
import com.calmed.calmedfrontendtourettes.model.raw.TickType
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoTourettesJoined(
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
