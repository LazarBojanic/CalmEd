package com.calmed.calmedbackend.auth.apple

import kotlinx.serialization.Serializable

@Serializable
data class AppleClaims(
	val iss: String,
	val aud: String,
	val sub: String,
	val email: String?,
	val emailVerified: Boolean?
)