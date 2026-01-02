package com.calmed.calmedbackend.model.raw.authcredential

import kotlinx.serialization.Serializable

@Serializable
enum class AuthCredentialType {
	BASIC, GOOGLE, APPLE
}