@file:OptIn(KoinExperimentalAPI::class)

package com.calmed.calmedtics.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.loading
import calmedtics.shared.generated.resources.no_internet_connection
import calmedtics.shared.generated.resources.user_info_missing
import com.calmed.calmedtics.auth.getGoogleIdToken
import com.calmed.calmedtics.auth.launchAppleSignIn
import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.logging.AppLog
import com.calmed.calmedtics.logging.LogTags
import com.calmed.calmedtics.model.dto.request.SupportMessageRequestDto
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.service.specification.IAuthService
import com.calmed.calmedtics.settings.AppSettings
import com.calmed.calmedtics.store.ITokenDataStore
import com.calmed.calmedtics.ui.component.ToastCenter
import com.calmed.calmedtics.ui.component.ToastKind
import com.calmed.calmedtics.ui.screen.AgeConfirmScreen
import com.calmed.calmedtics.ui.screen.CourseOverviewScreen
import com.calmed.calmedtics.ui.screen.ForgotPasswordScreen
import com.calmed.calmedtics.ui.screen.HelpSupportScreen
import com.calmed.calmedtics.ui.screen.LoginScreen
import com.calmed.calmedtics.ui.screen.MainScreen
import com.calmed.calmedtics.ui.screen.OfflineModeScreen
import com.calmed.calmedtics.ui.screen.OnboardingScreen
import com.calmed.calmedtics.ui.screen.PaymentScreen
import com.calmed.calmedtics.ui.screen.RegisterScreen
import com.calmed.calmedtics.ui.screen.SplashScreen
import com.calmed.calmedtics.ui.screen.VideoScreen
import com.calmed.calmedtics.ui.screen.WelcomeVideoScreen
import com.calmed.calmedtics.util.isBackendReachable
import com.calmed.calmedtics.viewmodel.AuthViewModel
import com.calmed.calmedtics.viewmodel.ExercisesViewModel
import com.calmed.calmedtics.viewmodel.HomeViewModel
import com.calmed.calmedtics.viewmodel.SessionRouting
import com.calmed.calmedtics.viewmodel.SessionViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

private val log = AppLog(LogTags.APP)

private fun downloadExercise(playbackId: String, title: String?): ProgramExerciseDto =
	ProgramExerciseDto(
		id = playbackId,
		weekNumber = 1,
		groupId = null,
		title = title.orEmpty(),
		description = "",
		token = "",
		previewToken = "",
		thumbnailToken = "",
		previewThumbnailToken = "",
		playbackId = playbackId,
		previewPlaybackId = "",
		url = "",
		previewURL = "",
		thumbnailURL = "",
		previewThumbnailURL = "",
		durationSeconds = null,
		visibility = "PUBLIC",
		createdAt = "",
		updatedAt = "",
	)

private fun SessionRouting.toRoute(): AppRoute = when {
	!confirmOverEighteen -> AppRoute.AgeConfirm
	shouldShowWelcomeVideo -> AppRoute.WelcomeVideo
	shouldShowCourseOverview -> AppRoute.CourseOverview
	!isPaid -> AppRoute.Payment
	!isOnboarded -> AppRoute.Onboarding
	else -> AppRoute.Main
}

private suspend fun SessionViewModel.nextRouteOrNull(): AppRoute? =
	resolveRouting()?.toRoute()

val appNavigationModule = module {
	single { AppNavigator() }

	navigation<AppRoute.Splash> {
		val appApi = get<IAppApi>()
		val tokenStore = get<ITokenDataStore>()
		val authService = get<IAuthService>()
		val session = koinAppViewModel<SessionViewModel>()
		val navigator = get<AppNavigator>()

		SplashScreen()

		LaunchedEffect(Unit) {
			val online = isBackendReachable(appApi)
			if (!online) {
				navigator.resetTo(AppRoute.Offline)
				return@LaunchedEffect
			}

			val currentToken = tokenStore.getToken()
			if (currentToken == null) {
				navigator.resetTo(AppRoute.Login)
				return@LaunchedEffect
			}

			val access = currentToken.access
			val refresh = currentToken.refresh
			if (access.isNullOrBlank() || refresh.isNullOrBlank()) {
				navigator.resetTo(AppRoute.Login)
				return@LaunchedEffect
			}

			if (!authService.tryRefresh()) {
				navigator.resetTo(AppRoute.Login)
				return@LaunchedEffect
			}

			val nextRoute = session.nextRouteOrNull()
			navigator.resetTo(nextRoute ?: AppRoute.Login)
		}
	}

	navigation<AppRoute.Login> {
		val navigator = get<AppNavigator>()
		val session = koinAppViewModel<SessionViewModel>()
		val authViewModel: AuthViewModel = koinViewModel()
		val scope = rememberCoroutineScope()

		LoginScreen(
			onNavigateRegister = { navigator.goTo(AppRoute.Register) },
			onNavigateForgotPassword = { navigator.goTo(AppRoute.ForgotPassword) },
			onNavigateOffline = { navigator.resetTo(AppRoute.Offline) },
			onLoginSuccess = {
				scope.launch {
					navigator.resetTo(session.nextRouteOrNull() ?: AppRoute.Login)
				}
			},
			onAppleSignIn = {
				launchAppleSignIn()
			},
			onGoogleSignIn = {
				scope.launch {
					try {
						val googleToken = getGoogleIdToken()
						val ok = authViewModel.loginWithGoogle(googleToken)
						if (ok) {
							navigator.resetTo(session.nextRouteOrNull() ?: AppRoute.Login)
						}
					} catch (t: Throwable) {
						log.error("GoogleSignIn failed", t)
					}
				}
			}
		)
	}

	navigation<AppRoute.Register> {
		val navigator = get<AppNavigator>()
		val session = koinAppViewModel<SessionViewModel>()
		val authViewModel: AuthViewModel = koinViewModel()
		val scope = rememberCoroutineScope()

		RegisterScreen(
			onNavigateLogin = { navigator.resetTo(AppRoute.Login) },
			onRegisterSuccess = { navigator.resetTo(AppRoute.Login) },
			onGoogleSignIn = {
				scope.launch {
					try {
						val googleToken = getGoogleIdToken()
						val ok = authViewModel.loginWithGoogle(googleToken)
						if (ok) {
							navigator.resetTo(session.nextRouteOrNull() ?: AppRoute.Login)
						}
					} catch (t: Throwable) {
						log.error("GoogleSignIn Register failed", t)
					}
				}
			},
			onAppleSignIn = {
				launchAppleSignIn()
			}
		)
	}

	navigation<AppRoute.ForgotPassword> {
		val navigator = get<AppNavigator>()
		ForgotPasswordScreen(onNavigateBack = { navigator.goBack() })
	}

	navigation<AppRoute.AgeConfirm> {
		val session = koinAppViewModel<SessionViewModel>()
		val navigator = get<AppNavigator>()
		val scope = rememberCoroutineScope()
		val loading by session.loading.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
		val error by session.error.collectAsStateWithLifecycle(LocalLifecycleOwner.current)

		AgeConfirmScreen(
			loading = loading,
			error = error,
			onConfirm = {
				scope.launch {
					if (session.confirmOverEighteen()) {
						navigator.resetTo(session.nextRouteOrNull() ?: AppRoute.Login)
					}
				}
			},
			onDecline = {
				scope.launch {
					session.logout()
					navigator.resetTo(AppRoute.Login)
				}
			}
		)
	}

	navigation<AppRoute.Offline> {
		val appApi = get<IAppApi>()
		val tokenStore = get<ITokenDataStore>()
		val authService = get<IAuthService>()
		val session = koinAppViewModel<SessionViewModel>()
		val navigator = get<AppNavigator>()
		val scope = rememberCoroutineScope()
		val noInternetMessage = stringResource(Res.string.no_internet_connection)

		OfflineModeScreen(
			onTryOnline = {
				scope.launch {
					val online = isBackendReachable(appApi)
					if (!online) {
						ToastCenter.show(noInternetMessage, ToastKind.Error)
						return@launch
					}

					val currentToken = tokenStore.getToken()
					val access = currentToken?.access
					val refresh = currentToken?.refresh
					if (access.isNullOrBlank() || refresh.isNullOrBlank()) {
						navigator.resetTo(AppRoute.Login)
						return@launch
					}

					if (!authService.tryRefresh()) {
						navigator.resetTo(AppRoute.Login)
						return@launch
					}

					navigator.resetTo(session.nextRouteOrNull() ?: AppRoute.Login)
				}
			},
			onOpenDownload = { playbackId, title ->
				if (playbackId.isNotBlank()) {
					navigator.goTo(
						AppRoute.Video(listOf(downloadExercise(playbackId, title)), 0, 1)
					)
				}
			}
		)
	}

	navigation<AppRoute.WelcomeVideo> {
		val settings = get<AppSettings>()
		val session = koinAppViewModel<SessionViewModel>()
		val navigator = get<AppNavigator>()

		WelcomeVideoScreen(
			onSkip = {
				session.onWelcomeHandled(session.user.value?.id)
				navigator.resetTo(AppRoute.CourseOverview)
			},
			onContinue = { dontShowAgain ->
				session.onWelcomeHandled(session.user.value?.id)
				if (dontShowAgain) {
					settings.setShowWelcomeVideo(session.user.value?.id, false)
				}
				navigator.resetTo(AppRoute.CourseOverview)
			}
		)
	}

	navigation<AppRoute.CourseOverview> {
		val settings = get<AppSettings>()
		val session = koinAppViewModel<SessionViewModel>()
		val navigator = get<AppNavigator>()
		val scope = rememberCoroutineScope()

		CourseOverviewScreen(
			onSkip = {
				session.onCourseOverviewHandled(session.user.value?.id)
				scope.launch {
					navigator.resetTo(session.nextRouteOrNull() ?: AppRoute.Payment)
				}
			},
			onContinue = { dontShowAgain ->
				session.onCourseOverviewHandled(session.user.value?.id)
				if (dontShowAgain) {
					settings.setShowCourseOverview(session.user.value?.id, false)
				}
				scope.launch {
					navigator.resetTo(session.nextRouteOrNull() ?: AppRoute.Payment)
				}
			}
		)
	}

	navigation<AppRoute.Onboarding> {
		val session = koinAppViewModel<SessionViewModel>()
		val navigator = get<AppNavigator>()
		val scope = rememberCoroutineScope()
		val user by session.user.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
		val userInfo by session.userInfo.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
		val loading by session.loading.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
		val error by session.error.collectAsStateWithLifecycle(LocalLifecycleOwner.current)

		val currentUser = user
		if (currentUser == null) {
			LaunchedEffect(Unit) {
				if (!loading && error == null) {
					session.loadSession()
				}
			}
			Text(error ?: stringResource(Res.string.loading))
			return@navigation
		}

		val info = userInfo
		if (info == null) {
			Text(stringResource(Res.string.user_info_missing))
			return@navigation
		}

		OnboardingScreen(
			user = currentUser,
			userInfo = info,
			onSkip = {
				scope.launch {
					if (session.skipOnboarding()) navigator.resetTo(AppRoute.Main)
				}
			},
			onFinished = { dto ->
				scope.launch {
					if (session.completeOnboarding(dto)) navigator.resetTo(AppRoute.Main)
				}
			}
		)
	}

	navigation<AppRoute.Payment> {
		val session = koinAppViewModel<SessionViewModel>()
		val navigator = get<AppNavigator>()
		val scope = rememberCoroutineScope()

		PaymentScreen(
			onPaid = {
				scope.launch {
					navigator.resetTo(session.nextRouteOrNull() ?: AppRoute.Login)
				}
			},
			onLogout = {
				scope.launch {
					session.logout()
					navigator.resetTo(AppRoute.Login)
				}
			}
		)
	}

	navigation<AppRoute.Video> { route ->
		val navigator = get<AppNavigator>()
		val exercises = route.exercises

		if (exercises.isEmpty()) {
			LaunchedEffect(Unit) { navigator.goBack() }
			return@navigation
		}

		VideoScreen(
			exercises = exercises,
			startIndex = route.startIndex,
			currentWeek = route.currentWeek,
			onBack = { navigator.goBack() }
		)
	}

	navigation<AppRoute.HelpSupport> {
		val navigator = get<AppNavigator>()
		val authService = get<IAuthService>()

		HelpSupportScreen(
			onBack = { navigator.goBack() },
			onSendMessage = { subject, message ->
				try {
					authService.sendSupportMessage(
						SupportMessageRequestDto(subject = subject, message = message)
					)
				} catch (t: Throwable) {
					false
				}
			}
		)
	}

	navigation<AppRoute.Main> {
		val navigator = get<AppNavigator>()
		val session = koinAppViewModel<SessionViewModel>()
		val homeViewModel: HomeViewModel = koinViewModel()
		val exercisesViewModel: ExercisesViewModel = koinViewModel()

		MainScreen(
			sessionViewModel = session,
			homeViewModel = homeViewModel,
			exercisesViewModel = exercisesViewModel,
			onLogoutToLogin = {
				session.resetRoutingFlags()
				navigator.resetTo(AppRoute.Login)
			},
			onAccountDeleted = {
				session.resetRoutingFlags()
				navigator.resetTo(AppRoute.Login)
			},
			onOpenVideoFromList = { exercises, startIndex ->
				navigator.goTo(
					AppRoute.Video(
						exercises = exercises,
						startIndex = startIndex,
						currentWeek = homeViewModel.home.value?.currentWeek ?: 1
					)
				)
			},
			onOpenHelpSupport = {
				navigator.goTo(AppRoute.HelpSupport)
			}
		)
	}
}
