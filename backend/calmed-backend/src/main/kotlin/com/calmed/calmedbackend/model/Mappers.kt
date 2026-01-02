package com.calmed.calmedbackend.model

import com.calmed.calmedbackend.model.dto.response.UserDto
import com.calmed.calmedbackend.model.joined.AuthCredentialJoined
import com.calmed.calmedbackend.model.joined.MessageJoined
import com.calmed.calmedbackend.model.joined.RefreshTokenJoined
import com.calmed.calmedbackend.model.joined.UserJoined
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredential
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialEntity
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshToken
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshTokenEntity
import com.calmed.calmedbackend.model.raw.user.User
import com.calmed.calmedbackend.model.raw.user.UserEntity

enum class MapMode {
	CREATE, UPDATE
}

fun UserEntity.toRaw(): User {
	return User(
		id = this.id.value,
		email = this.email,
		username = this.username,
		isEmailVerified = this.isEmailVerified,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun AuthCredentialEntity.toRaw(): AuthCredential {
	return AuthCredential(
		id = this.id.value,
		userId = this.userId,
		type = this.type,
		passwordHash = this.passwordHash,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun RefreshTokenEntity.toRaw(): RefreshToken {
	return RefreshToken(
		id = this.id.value,
		replacedBy = this.replacedBy,
		userId = this.userId,
		tokenHash = this.tokenHash,
		issuedAt = this.issuedAt,
		expiresAt = this.expiresAt,
		revokedAt = this.revokedAt,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun User.join(): UserJoined {
	return UserJoined(
		id = this.id,
		email = this.email,
		username = this.username,
		isEmailVerified = this.isEmailVerified,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun AuthCredential.join(userJoined: UserJoined): AuthCredentialJoined {
	return AuthCredentialJoined(
		id = this.id,
		userJoined = userJoined,
		type = this.type,
		passwordHash = this.passwordHash,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun RefreshToken.join(userJoined: UserJoined): RefreshTokenJoined {
	return RefreshTokenJoined(
		id = this.id,
		replacedBy = this.replacedBy,
		userJoined = userJoined,
		tokenHash = this.tokenHash,
		issuedAt = this.issuedAt,
		expiresAt = this.expiresAt,
		revokedAt = this.revokedAt,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun RefreshTokenJoined.toRaw(): RefreshToken {
	return RefreshToken(
		id = this.id,
		replacedBy = this.replacedBy,
		userId = userJoined.id,
		tokenHash = this.tokenHash,
		issuedAt = this.issuedAt,
		expiresAt = this.expiresAt,
		revokedAt = this.revokedAt,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun UserEntity.setFrom(d: User, mapMode: MapMode) {
	email = d.email
	username = d.username
	isEmailVerified = d.isEmailVerified
	when (mapMode) {
		MapMode.CREATE -> {
			createdAt = d.createdAt
			updatedAt = d.updatedAt
		}

		MapMode.UPDATE -> {
			updatedAt = d.updatedAt
		}
	}
}

fun AuthCredentialEntity.setFrom(d: AuthCredential, mapMode: MapMode) {
	userId = d.userId
	type = d.type
	passwordHash = d.passwordHash
	when (mapMode) {
		MapMode.CREATE -> {
			createdAt = d.createdAt
			updatedAt = d.updatedAt
		}

		MapMode.UPDATE -> {
			updatedAt = d.updatedAt
		}
	}
}

fun RefreshTokenEntity.setFrom(d: RefreshToken, mapMode: MapMode) {
	userId = d.userId
	tokenHash = d.tokenHash
	issuedAt = d.issuedAt
	expiresAt = d.expiresAt
	revokedAt = d.revokedAt
	when (mapMode) {
		MapMode.CREATE -> {
			createdAt = d.createdAt
			updatedAt = d.updatedAt
		}

		MapMode.UPDATE -> {
			updatedAt = d.updatedAt
		}
	}

}

fun UserJoined.toDto(): UserDto{
	return UserDto(
		id = this.id,
		email = this.email,
		username = this.username,
		isEmailVerified = this.isEmailVerified
	)
}