package com.calmed.calmedbackend.service.specification

import java.util.UUID

interface IJwtService{
	fun generateAccessToken(userId: UUID, email: String): String
	fun verifier()
}