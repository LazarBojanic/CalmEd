package com.calmed.calmedbackend.model

import com.calmed.calmedbackend.config.MuxConfig
import com.calmed.calmedbackend.model.dto.response.UserDto
import com.calmed.calmedbackend.model.dto.response.UserInfoTicsDto
import com.calmed.calmedbackend.model.dto.response.ProgramExerciseDto
import com.calmed.calmedbackend.model.dto.response.ExerciseGroupDto
import com.calmed.calmedbackend.model.dto.response.UserProgramDto
import com.calmed.calmedbackend.model.dto.response.UserExerciseProgressDto
import com.calmed.calmedbackend.model.joined.AuthCredentialJoined
import com.calmed.calmedbackend.model.joined.RefreshTokenJoined
import com.calmed.calmedbackend.model.joined.UserInfoTicsJoined
import com.calmed.calmedbackend.model.joined.UserJoined
import com.calmed.calmedbackend.model.joined.ProgramExerciseJoined
import com.calmed.calmedbackend.model.joined.UserProgramJoined
import com.calmed.calmedbackend.model.joined.UserExerciseProgressJoined
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredential
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialEntity
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshToken
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshTokenEntity
import com.calmed.calmedbackend.model.raw.payment.Payment
import com.calmed.calmedbackend.model.raw.payment.PaymentEntity
import com.calmed.calmedbackend.model.raw.storeentitlement.StoreEntitlement
import com.calmed.calmedbackend.model.raw.storeentitlement.StoreEntitlementEntity
import com.calmed.calmedbackend.model.raw.user.User
import com.calmed.calmedbackend.model.raw.user.UserEntity
import com.calmed.calmedbackend.model.raw.userinfo.tics.UserInfoTics
import com.calmed.calmedbackend.model.raw.userinfo.tics.UserInfoTicsEntity
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExercise
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExerciseEntity
import com.calmed.calmedbackend.model.raw.exercisegroup.ExerciseGroup
import com.calmed.calmedbackend.model.raw.exercisegroup.ExerciseGroupEntity
import com.calmed.calmedbackend.model.raw.programexercise.Visibility
import com.calmed.calmedbackend.model.raw.userprogram.UserProgram
import com.calmed.calmedbackend.model.raw.userprogram.UserProgramEntity
import com.calmed.calmedbackend.model.raw.userexerciseprogress.UserExerciseProgress
import com.calmed.calmedbackend.model.raw.userexerciseprogress.UserExerciseProgressEntity
import com.calmed.calmedbackend.util.MuxTokenGenerator
import java.time.ZoneId

enum class MapMode {
	CREATE, UPDATE
}

fun UserEntity.toRaw(): User {
	return User(
		id = this.id.value,
		email = this.email,
		username = this.username,
		profileImageUrl = this.profileImageUrl,
		isEmailVerified = this.isEmailVerified,
		isOnboarded = this.isOnboarded,
		confirmOverEighteen = this.confirmOverEighteen,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun PaymentEntity.toRaw(): Payment {
	return Payment(
		id = this.id.value,
		userId = this.userId,
		provider = this.provider,
		googlePurchaseToken = this.googlePurchaseToken,
		googleOrderId = this.googleOrderId,
		appleTransactionId = this.appleTransactionId,
		appleOriginalTransactionId = this.appleOriginalTransactionId,
		stripePaymentIntentId = this.stripePaymentIntentId,
		stripeCheckoutSessionId = this.stripeCheckoutSessionId,
		paypalOrderId = this.paypalOrderId,
		paypalCaptureId = this.paypalCaptureId,
		status = this.status,
		refundedAt = this.refundedAt,
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
		providerUserId = this.providerUserId,
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
		profileImageUrl = this.profileImageUrl,
		isEmailVerified = this.isEmailVerified,
		isOnboarded = this.isOnboarded,
		confirmOverEighteen = this.confirmOverEighteen,
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
		providerUserId = this.providerUserId,
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
	profileImageUrl = d.profileImageUrl
	isEmailVerified = d.isEmailVerified
	isOnboarded = d.isOnboarded
	confirmOverEighteen = d.confirmOverEighteen
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
	providerUserId = d.providerUserId
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

fun PaymentEntity.setFrom(d: Payment, mapMode: MapMode) {
	userId = d.userId
	provider = d.provider
	googlePurchaseToken = d.googlePurchaseToken
	googleOrderId = d.googleOrderId
	appleTransactionId = d.appleTransactionId
	appleOriginalTransactionId = d.appleOriginalTransactionId
	stripePaymentIntentId = d.stripePaymentIntentId
	stripeCheckoutSessionId = d.stripeCheckoutSessionId
	paypalOrderId = d.paypalOrderId
	paypalCaptureId = d.paypalCaptureId
	status = d.status
	refundedAt = d.refundedAt
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

fun StoreEntitlementEntity.toRaw(): StoreEntitlement {
	return StoreEntitlement(
		id = this.id.value,
		store = this.store,
		storeTransactionId = this.storeTransactionId,
		userId = this.userId,
		productId = this.productId,
		obfuscatedAccountId = this.obfuscatedAccountId,
		environment = this.environment,
		revokedAt = this.revokedAt,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun StoreEntitlementEntity.setFrom(d: StoreEntitlement, mapMode: MapMode) {
	store = d.store
	storeTransactionId = d.storeTransactionId
	userId = d.userId
	productId = d.productId
	obfuscatedAccountId = d.obfuscatedAccountId
	environment = d.environment
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

fun UserJoined.toDto(): UserDto {
	return UserDto(
		id = this.id,
		email = this.email,
		username = this.username,
		profileImageUrl = this.profileImageUrl,
		isEmailVerified = this.isEmailVerified,
		isOnboarded = this.isOnboarded,
		confirmOverEighteen = this.confirmOverEighteen,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun UserInfoTics.join(user: UserJoined): UserInfoTicsJoined {
	return UserInfoTicsJoined(
		id = this.id,
		user = user,
		preferredName = this.preferredName,
		age = this.age,
		stressLevel = this.stressLevel,
		ticType = this.ticType,
		ticFrequency = this.ticFrequency,
		ticDuration = this.ticDuration,
		goal = this.goal,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun UserInfoTicsEntity.toRaw(): UserInfoTics {
	return UserInfoTics(
		id = this.id.value,
		userId = this.userId,
		preferredName = this.preferredName,
		age = this.age,
		stressLevel = this.stressLevel,
		ticType = this.ticType,
		ticFrequency = this.ticFrequency,
		ticDuration = this.ticDuration,
		goal = this.goal,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun UserInfoTicsEntity.setFrom(d: UserInfoTics, mapMode: MapMode) {
	userId = d.userId
	preferredName = d.preferredName
	age = d.age
	stressLevel = d.stressLevel
	ticType = d.ticType
	ticFrequency = d.ticFrequency
	ticDuration = d.ticDuration
	goal = d.goal
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

fun UserInfoTicsJoined.toDto(): UserInfoTicsDto {
	return UserInfoTicsDto(
		id = this.id,
		user = this.user.toDto(),
		preferredName = this.preferredName,
		age = this.age,
		stressLevel = this.stressLevel,
		ticType = this.ticType,
		ticFrequency = this.ticFrequency,
		ticDuration = this.ticDuration,
		goal = this.goal,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun ExerciseGroupEntity.toRaw(): ExerciseGroup {
	return ExerciseGroup(
		id = this.id.value,
		name = this.name,
		description = this.description
	)
}

fun ExerciseGroup.toDto(): ExerciseGroupDto {
	return ExerciseGroupDto(
		id = this.id,
		name = this.name,
		description = this.description
	)
}

fun ProgramExerciseEntity.toRaw(): ProgramExercise {
	return ProgramExercise(
		id = this.id.value,
		weekNumber = this.weekNumber,
		groupId = this.groupId,
		title = this.title,
		description = this.description,
		playbackId = this.playbackId,
		previewPlaybackId = this.previewPlaybackId,
		durationSeconds = this.durationSeconds,
		visibility = this.visibility,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun ProgramExerciseEntity.setFrom(d: ProgramExercise, mapMode: MapMode) {
	weekNumber = d.weekNumber
	groupId = d.groupId
	title = d.title
	description = d.description
	playbackId = d.playbackId
	previewPlaybackId = d.previewPlaybackId
	durationSeconds = d.durationSeconds
	visibility = d.visibility
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

fun ProgramExercise.join(): ProgramExerciseJoined {
	return ProgramExerciseJoined(
		id = this.id,
		weekNumber = this.weekNumber,
		groupId = this.groupId,
		title = this.title,
		description = this.description,
		playbackId = this.playbackId,
		previewPlaybackId = this.previewPlaybackId,
		durationSeconds = this.durationSeconds,
		visibility = this.visibility,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun ProgramExerciseJoined.toDto(muxConfig: MuxConfig, hasAccess: Boolean): ProgramExerciseDto {
	val thumbnailBaseURL = "https://image.mux.com/"
	val isSigned = this.visibility == Visibility.SIGNED
	val canPlayFull = !isSigned || hasAccess

	var token480 = ""
	var token720 = ""
	var token1080 = ""
	var previewToken = ""
	var thumbnailToken = ""
	var previewThumbnailToken = ""
	var thumbnailURL = "$thumbnailBaseURL${this.playbackId}/thumbnail.jpg"
	var previewThumbnailURL = "$thumbnailBaseURL${this.previewPlaybackId}/thumbnail.jpg"

	if (isSigned) {
		token480 = MuxTokenGenerator.generatePlaybackToken(
			this.playbackId,
			muxConfig.signingKey,
			muxConfig.privateKey,
			maxResolution = "480p"
		)
		token720 = MuxTokenGenerator.generatePlaybackToken(
			this.playbackId,
			muxConfig.signingKey,
			muxConfig.privateKey,
			maxResolution = "720p"
		)
		token1080 = MuxTokenGenerator.generatePlaybackToken(
			this.playbackId,
			muxConfig.signingKey,
			muxConfig.privateKey,
			maxResolution = "1080p"
		)
		previewToken = MuxTokenGenerator.generatePlaybackToken(
			this.previewPlaybackId,
			muxConfig.signingKey,
			muxConfig.privateKey,
			maxResolution = "720p"
		)
		thumbnailToken = MuxTokenGenerator.generateThumbnailToken(
			this.playbackId,
			muxConfig.signingKey,
			muxConfig.privateKey
		)
		previewThumbnailToken = MuxTokenGenerator.generateThumbnailToken(
			this.previewPlaybackId,
			muxConfig.signingKey,
			muxConfig.privateKey
		)
		thumbnailURL = "$thumbnailURL?token=$thumbnailToken"
		previewThumbnailURL = "$previewThumbnailURL?token=$previewThumbnailToken"
	}

	if (!canPlayFull) {
		token480 = ""
		token720 = ""
		token1080 = ""
		previewToken = ""
		thumbnailToken = ""
		previewThumbnailToken = ""
		thumbnailURL = ""
		previewThumbnailURL = ""
	}

	return ProgramExerciseDto(
		id = this.id,
		weekNumber = this.weekNumber,
		groupId = this.groupId,
		title = this.title,
		description = this.description,
		playbackId = this.playbackId,
		previewPlaybackId = this.previewPlaybackId,
		token480 = token480,
		token720 = token720,
		token1080 = token1080,
		previewToken = previewToken,
		thumbnailToken = thumbnailToken,
		previewThumbnailToken = previewThumbnailToken,
		thumbnailURL = thumbnailURL,
		previewThumbnailURL = previewThumbnailURL,
		durationSeconds = this.durationSeconds,
		visibility = this.visibility,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun UserProgramEntity.toRaw(): UserProgram {
	return UserProgram(
		id = this.id.value,
		userId = this.userId,
		startDate = this.startDate,
		endDate = this.endDate,
		timezone = this.timezone?.let { ZoneId.of(it) },
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun UserProgramEntity.setFrom(d: UserProgram, mapMode: MapMode) {
	userId = d.userId
	startDate = d.startDate
	endDate = d.endDate
	timezone = d.timezone?.id
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

fun UserProgram.join(user: UserJoined): UserProgramJoined {
	return UserProgramJoined(
		id = this.id,
		user = user,
		startDate = this.startDate,
		endDate = this.endDate,
		timezone = this.timezone,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun UserProgramJoined.toDto(): UserProgramDto {
	return UserProgramDto(
		id = this.id,
		user = this.user.toDto(),
		startDate = this.startDate,
		endDate = this.endDate,
		timezone = this.timezone,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun UserExerciseProgressEntity.toRaw(): UserExerciseProgress {
	return UserExerciseProgress(
		id = this.id.value,
		userId = this.userId,
		week = this.week,
		day = this.day,
		exerciseSession = this.exerciseSession,
		completedAt = this.completedAt,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun UserExerciseProgressEntity.setFrom(d: UserExerciseProgress, mapMode: MapMode) {
	userId = d.userId
	week = d.week
	day = d.day
	exerciseSession = d.exerciseSession
	completedAt = d.completedAt
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

fun UserExerciseProgress.join(user: UserJoined): UserExerciseProgressJoined {
	return UserExerciseProgressJoined(
		id = this.id,
		user = user,
		week = this.week,
		day = this.day,
		exerciseSession = this.exerciseSession,
		completedAt = this.completedAt,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun UserExerciseProgressJoined.toDto(): UserExerciseProgressDto {
	return UserExerciseProgressDto(
		id = this.id,
		user = this.user.toDto(),
		week = this.week,
		day = this.day,
		exerciseSession = this.exerciseSession,
		completedAt = this.completedAt,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

