package com.calmed.calmedtics.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed

@Serializable
sealed interface AppRoute : NavKey {
	@Serializable
	data object Splash : AppRoute

	@Serializable
	data object Login : AppRoute

	@Serializable
	data object Register : AppRoute

	@Serializable
	data object ForgotPassword : AppRoute

	@Serializable
	data object WelcomeVideo : AppRoute

	@Serializable
	data object CourseOverview : AppRoute

	@Serializable
	data class Video(
		val exercises: List<ProgramExerciseDto>,
		val startIndex: Int,
		val currentWeek: Int,
	) : AppRoute

	@Serializable
	data object Main : AppRoute

	@Serializable
	data object Onboarding : AppRoute

	@Serializable
	data object AgeConfirm : AppRoute

	@Serializable
	data object Payment : AppRoute

	@Serializable
	data object Offline : AppRoute

	@Serializable
	data object HelpSupport : AppRoute
}

@OptIn(ExperimentalSerializationApi::class)
val appRouteConfig = SavedStateConfiguration {
	serializersModule = SerializersModule {
		polymorphic(NavKey::class) {
			subclassesOfSealed<AppRoute>()
		}
	}
}
