package com.calmed.calmedfrontendtourettes.model.dto.response

import com.calmed.calmedfrontendtourettes.model.dto.response.UserDto
import com.calmed.calmedfrontendtourettes.model.raw.TickFrequency
import com.calmed.calmedfrontendtourettes.model.raw.TickType
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoTourettesDto(
	val id: String,
	val user: UserDto,
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
