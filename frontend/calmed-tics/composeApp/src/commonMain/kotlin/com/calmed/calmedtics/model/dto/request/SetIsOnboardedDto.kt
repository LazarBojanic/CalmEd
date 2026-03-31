package com.calmed.calmedtics.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class SetIsOnboardedDto(
	val isOnboarded: Boolean,
)