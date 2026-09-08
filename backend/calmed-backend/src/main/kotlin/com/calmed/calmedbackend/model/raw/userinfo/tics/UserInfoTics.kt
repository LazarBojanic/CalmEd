package com.calmed.calmedbackend.model.raw.userinfo.tics

import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class UserInfoTics(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	@Serializable(with = UUIDSerializer::class)
	val userId: UUID,
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
	val updatedAt: Instant
) {
	companion object {
		fun createNew(
			userId: UUID,
			preferredName: String?,
			age: Int?,
			stressLevel: Int?,
			ticType: TicType?,
			ticFrequency: TicFrequency?,
			ticDuration: TicDuration?,
			goal: String?,
			createdAt: Instant? = null,
			updatedAt: Instant? = null,
		): UserInfoTics {
			val now = Instant.now()
			val cat = createdAt ?: now
			val uat = updatedAt ?: now
			return UserInfoTics(
				id = UUID.randomUUID(),
				userId = userId,
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