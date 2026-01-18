package com.calmed.calmedfrontendtourettes.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class SetIsOnboardedDto(
	val isOnboarded: Boolean,
)