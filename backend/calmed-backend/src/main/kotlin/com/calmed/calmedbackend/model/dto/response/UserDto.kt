package com.calmed.calmedbackend.model.dto.response

import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UserDto(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val email: String,
	val username: String,
	val isEmailVerified: Boolean
)