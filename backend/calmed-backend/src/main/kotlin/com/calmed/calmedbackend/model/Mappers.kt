package com.calmed.calmedbackend.model

import com.calmed.calmedbackend.model.dto.response.MessageDto
import com.calmed.calmedbackend.model.joined.AuthCredentialJoined
import com.calmed.calmedbackend.model.joined.MessageJoined
import com.calmed.calmedbackend.model.joined.UserJoined
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredential
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialEntity
import com.calmed.calmedbackend.model.raw.message.Message
import com.calmed.calmedbackend.model.raw.message.MessageEntity
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshToken
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshTokenEntity
import com.calmed.calmedbackend.model.raw.user.User
import com.calmed.calmedbackend.model.raw.user.UserEntity

enum class MapMode {
	CREATE, UPDATE
}

fun MessageEntity.toRaw(): Message = Message(
	id = this.id.value,
	text = this.text,
	createdAt = this.createdAt,
	updatedAt = this.updatedAt
)

fun MessageEntity.setFrom(d: Message, mode: MapMode) {
	text = d.text
	createdAt = d.createdAt
	updatedAt = d.updatedAt
	when (mode) {
		MapMode.CREATE -> {
			createdAt = d.createdAt
			updatedAt = d.updatedAt
		}

		MapMode.UPDATE -> {
			updatedAt = d.updatedAt
		}
	}
}

fun Message.join(): MessageJoined {
	return MessageJoined(
		id = this.id,
		text = this.text,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun MessageJoined.toDto(): MessageDto {
	return MessageDto(
		id = this.id,
		text = this.text,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun UserEntity.toRaw() : User {
	return User(
		id = this.id.value,
		email = this.email,
		username = this.username,
		isEmailVerified = this.isEmailVerified,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun AuthCredentialEntity.toRaw() : AuthCredential {
	return AuthCredential(
		id = this.id.value,
		userId = this.userId,
		type = this.type,
		passwordHash = this.passwordHash,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun RefreshTokenEntity.toRaw() : RefreshToken {
	return RefreshToken(
		id = this.id.value,
		userId = this.userId,
		tokenHash = this.tokenHash,
		issuedAt = this.issuedAt,
		expiresAt = this.expiresAt,
		revokedAt = this.revokedAt,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun User.join() : UserJoined {
	return UserJoined(
		id = this.id,
		email = this.email,
		username = this.username,
		isEmailVerified = this.isEmailVerified,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun AuthCredential.join(user: User): AuthCredentialJoined{
	return AuthCredentialJoined(
		id = this.id,
		userId = user.id,

	)
}