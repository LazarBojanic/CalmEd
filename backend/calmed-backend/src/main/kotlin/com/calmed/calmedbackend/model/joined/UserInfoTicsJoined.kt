package com.calmed.calmedbackend.model.joined

import com.calmed.calmedbackend.model.raw.userinfo.tics.TicFrequency
import com.calmed.calmedbackend.model.raw.userinfo.tics.TicType
import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID
import com.calmed.calmedbackend.model.raw.userinfo.tics.TicDuration

@Serializable
data class UserInfoTicsJoined(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val user: UserJoined,
	val preferredName: String?,
	val age: Int?,
	val stressLevel: Int?,
	val ticType: TicType?,
	val ticFrequency: TicFrequency?,
	val ticDuration: TicDuration?,
	val goal: String?,
	@Serializable(with = InstantSerializer::class)
	val createdAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val updatedAt: Instant,
) {
	companion object {
		fun createNew(
			user: UserJoined,
			preferredName: String?,
			age: Int?,
			stressLevel: Int?,
			ticType: TicType?,
			ticFrequency: TicFrequency?,
			ticDuration: TicDuration?,
			goal: String?,
			createdAt: Instant? = null,
			updatedAt: Instant? = null,
		): UserInfoTicsJoined {
			val now = Instant.now()
			val cat = createdAt ?: now
			val uat = updatedAt ?: now
			return UserInfoTicsJoined(
				id = UUID.randomUUID(),
				user = user,
				preferredName = preferredName,
				age = age,
				stressLevel = stressLevel,
				ticType = ticType,
				ticFrequency = ticFrequency,
				ticDuration = ticDuration,
				goal = goal,
				createdAt = cat,
				updatedAt = uat
			)
		}
	}
}