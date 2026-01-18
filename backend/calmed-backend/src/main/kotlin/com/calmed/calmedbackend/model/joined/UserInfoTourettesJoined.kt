package com.calmed.calmedbackend.model.joined

import com.calmed.calmedbackend.model.raw.userinfo.TickFrequency
import com.calmed.calmedbackend.model.raw.userinfo.TickType
import com.calmed.calmedbackend.model.raw.userinfo.UserInfoTourettes
import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class UserInfoTourettesJoined(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val user: UserJoined,
	val preferredName: String?,
	val age: Int?,
	val stressLevel: Int?,
	val tickType: TickType?,
	val tickFrequency: TickFrequency?,
	val goal: String?,
	val followProgress: Boolean?,
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
			tickType: TickType?,
			tickFrequency: TickFrequency?,
			goal: String?,
			followProgress: Boolean?,
			createdAt: Instant? = null,
			updatedAt: Instant? = null,
		): UserInfoTourettesJoined {
			val now = Instant.now()
			val cat = createdAt ?: now
			val uat = updatedAt ?: now
			return UserInfoTourettesJoined(
				id = UUID.randomUUID(),
				user = user,
				preferredName = preferredName,
				age = age,
				stressLevel = stressLevel,
				tickType = tickType,
				tickFrequency = tickFrequency,
				goal = goal,
				followProgress = followProgress,
				createdAt = cat,
				updatedAt = uat
			)
		}
	}
}