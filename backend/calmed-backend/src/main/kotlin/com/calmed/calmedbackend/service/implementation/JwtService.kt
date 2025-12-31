package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.service.specification.IJwtService
import java.util.UUID

class JwtService : IJwtService {
	override fun generateAccessToken(userId: UUID, email: String): String {
		TODO("Not yet implemented")
	}

	override fun verifier() {
		TODO("Not yet implemented")
	}
}