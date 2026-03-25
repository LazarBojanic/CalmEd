package com.calmed.calmedbackend.model

import com.calmed.calmedbackend.config.MuxConfig
import com.calmed.calmedbackend.model.dto.response.UserDto
import com.calmed.calmedbackend.model.dto.response.UserInfoTourettesDto
import com.calmed.calmedbackend.model.dto.response.ProgramExerciseDto
import com.calmed.calmedbackend.model.dto.response.UserProgramDto
import com.calmed.calmedbackend.model.dto.response.UserExerciseProgressDto
import com.calmed.calmedbackend.model.joined.AuthCredentialJoined
import com.calmed.calmedbackend.model.joined.RefreshTokenJoined
import com.calmed.calmedbackend.model.joined.UserInfoTourettesJoined
import com.calmed.calmedbackend.model.joined.UserJoined
import com.calmed.calmedbackend.model.joined.ProgramExerciseJoined
import com.calmed.calmedbackend.model.joined.UserProgramJoined
import com.calmed.calmedbackend.model.joined.UserExerciseProgressJoined
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredential
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialEntity
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshToken
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshTokenEntity
import com.calmed.calmedbackend.model.raw.user.User
import com.calmed.calmedbackend.model.raw.user.UserEntity
import com.calmed.calmedbackend.model.raw.userinfo.tourettes.UserInfoTourettes
import com.calmed.calmedbackend.model.raw.userinfo.tourettes.UserInfoTourettesEntity
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExercise
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExerciseEntity
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
		isEmailVerified = this.isEmailVerified,
		isOnboarded = this.isOnboarded,
		isPaid = this.isPaid,
		paymentType = this.paymentType,
		stripeCustomerId = this.stripeCustomerId,
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
		isOnboarded = this.isOnboarded,
		isPaid = this.isPaid,
		paymentType = this.paymentType,
		stripeCustomerId = this.stripeCustomerId,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun AuthCredential.join(userJoined: UserJoined): AuthCredentialJoined {
	return AuthCredentialJoined(
		id = this.id,
		userJoined = userJoined,
		type = this.type,
		passwordHash = this.passwordHash ?: "",
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
	isOnboarded = d.isOnboarded
	isPaid = d.isPaid
	paymentType = d.paymentType
	stripeCustomerId = d.stripeCustomerId
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

fun UserJoined.toDto(): UserDto {
	return UserDto(
		id = this.id,
		email = this.email,
		username = this.username,
		isEmailVerified = this.isEmailVerified,
		isOnboarded = this.isOnboarded,
		isPaid = this.isPaid,
		paymentType = this.paymentType,
		stripeCustomerId = this.stripeCustomerId,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun UserInfoTourettes.join(user: UserJoined): UserInfoTourettesJoined {
	return UserInfoTourettesJoined(
		id = this.id,
		user = user,
		preferredName = this.preferredName,
		age = this.age,
		stressLevel = this.stressLevel,
		tickType = this.tickType,
		tickFrequency = this.tickFrequency,
		goal = this.goal,
		followProgress = this.followProgress,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun UserInfoTourettesEntity.toRaw(): UserInfoTourettes {
	return UserInfoTourettes(
		id = this.id.value,
		userId = this.userId,
		preferredName = this.preferredName,
		age = this.age,
		stressLevel = this.stressLevel,
		tickType = this.tickType,
		tickFrequency = this.tickFrequency,
		goal = this.goal,
		followProgress = this.followProgress,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun UserInfoTourettesEntity.setFrom(d: UserInfoTourettes, mapMode: MapMode) {
	userId = d.userId
	preferredName = d.preferredName
	age = d.age
	stressLevel = d.stressLevel
	tickType = d.tickType
	tickFrequency = d.tickFrequency
	goal = d.goal
	followProgress = d.followProgress
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

fun UserInfoTourettesJoined.toDto(): UserInfoTourettesDto {
	return UserInfoTourettesDto(
		id = this.id,
		user = this.user.toDto(),
		preferredName = this.preferredName,
		age = this.age,
		stressLevel = this.stressLevel,
		tickType = this.tickType,
		tickFrequency = this.tickFrequency,
		goal = this.goal,
		followProgress = this.followProgress,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

// ProgramExercise mappings

fun ProgramExerciseEntity.toRaw(): ProgramExercise {
	return ProgramExercise(
		id = this.id.value,
		weekNumber = this.weekNumber,
		title = this.title,
		description = this.description,
		playbackId = this.playbackId,
		thumbnailURL = this.thumbnailURL,
		visibility = this.visibility,
		orderInWeek = this.orderInWeek,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun ProgramExerciseEntity.setFrom(d: ProgramExercise, mapMode: MapMode) {
	weekNumber = d.weekNumber
	title = d.title
	description = d.description
	playbackId = d.playbackId
	thumbnailURL = d.thumbnailURL
	visibility = d.visibility
	orderInWeek = d.orderInWeek
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
		title = this.title,
		description = this.description,
		playbackId = this.playbackId,
		thumbnailURL = this.thumbnailURL,
		visibility = this.visibility,
		orderInWeek = this.orderInWeek,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun ProgramExerciseJoined.toDto(muxConfig: MuxConfig): ProgramExerciseDto {
	val baseURL = "https://stream.mux.com/";
	var videoURL = ""
	if(muxConfig.signingKey.isNotBlank() && muxConfig.privateKey.isNotBlank() && !this.playbackId.isNullOrBlank()){
		if(this.visibility == Visibility.SIGNED){
			val token = MuxTokenGenerator.generatePlaybackToken(
				this.playbackId,
				muxConfig.signingKey,
				muxConfig.privateKey
			)
			videoURL = "$baseURL${this.playbackId}.m3u8?token=${token}"
		}
		else{
			videoURL = "$baseURL${this.playbackId}.m3u8"
		}
	}
	else{
		videoURL = "$baseURL${this.playbackId}.m3u8"
	}
	return ProgramExerciseDto(
		id = this.id,
		weekNumber = this.weekNumber,
		title = this.title,
		description = this.description,
		playbackId = this.playbackId,
		videoURL = videoURL,
		thumbnailURL = this.thumbnailURL,
		visibility = this.visibility,
		orderInWeek = this.orderInWeek,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

// UserProgram mappings

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

// UserExerciseProgress mappings

fun UserExerciseProgressEntity.toRaw(): UserExerciseProgress {
	return UserExerciseProgress(
		id = this.id.value,
		userId = this.userId,
		programExerciseId = this.programExerciseId,
		session = this.session,
		completedAt = this.completedAt,
		day = this.day,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun UserExerciseProgressEntity.setFrom(d: UserExerciseProgress, mapMode: MapMode) {
	userId = d.userId
	programExerciseId = d.programExerciseId
	session = d.session
	completedAt = d.completedAt
	day = d.day
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

fun UserExerciseProgress.join(user: UserJoined, programExercise: ProgramExerciseJoined): UserExerciseProgressJoined {
	return UserExerciseProgressJoined(
		id = this.id,
		user = user,
		programExercise = programExercise,
		session = this.session,
		completedAt = this.completedAt,
		day = this.day,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}

fun UserExerciseProgressJoined.toDto(muxConfig: MuxConfig): UserExerciseProgressDto {
	return UserExerciseProgressDto(
		id = this.id,
		user = this.user.toDto(),
		programExercise = this.programExercise.toDto(muxConfig),
		session = this.session,
		day = this.day,
		createdAt = this.createdAt,
		updatedAt = this.updatedAt
	)
}
