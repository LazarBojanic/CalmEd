package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calmed.calmedtics.Res
import com.calmed.calmedtics.error_prefix
import com.calmed.calmedtics.model.dto.request.SupportMessageRequestDto
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.onboarding_error
import com.calmed.calmedtics.onboarding_title
import com.calmed.calmedtics.retry
import com.calmed.calmedtics.service.specification.IAuthService
import com.calmed.calmedtics.settings.AppSettings
import com.calmed.calmedtics.skip_onboarding
import com.calmed.calmedtics.store.ITokenDataStore
import com.calmed.calmedtics.tab_exercises
import com.calmed.calmedtics.tab_home
import com.calmed.calmedtics.tab_profile
import com.calmed.calmedtics.ui.component.PrimaryButton
import com.calmed.calmedtics.ui.component.ScreenScaffold
import com.calmed.calmedtics.util.currentYmd
import com.calmed.calmedtics.viewmodel.SessionViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private enum class MainTab { Home, Exercises, Profile, HelpSupport }

@Composable
fun MainScreen(
	onLogoutToLogin: () -> Unit,
	onOpenFullscreen: (String) -> Unit,
	onOpenFullscreenFromList: (List<ProgramExerciseDto>, Int) -> Unit,
	sessionViewModel: SessionViewModel = koinInject(),
	authService: IAuthService = koinInject()
) {
	val scope = rememberCoroutineScope()

	val tokenStore: ITokenDataStore = koinInject()
	val token by tokenStore.tokenDto.collectAsState()

	val loading by sessionViewModel.loading.collectAsState()
	val error by sessionViewModel.error.collectAsState()
	val user by sessionViewModel.user.collectAsState()
	val userInfo by sessionViewModel.userInfo.collectAsState()
	val home by sessionViewModel.home.collectAsState()
	val allExercises by sessionViewModel.allExercises.collectAsState()

	val selectedTab = rememberSaveable { mutableStateOf(MainTab.Home) }
	val appSettings = koinInject<AppSettings>()

	LaunchedEffect(token?.access) {
		val access = token?.access
		if (!access.isNullOrBlank()) {
			val ymd = currentYmd()
			sessionViewModel.loadSession()
			sessionViewModel.loadHome(year = ymd.year, month = ymd.month)
			sessionViewModel.loadAllExercises()
		}
	}

	if (loading && user == null) {
		Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
			CircularProgressIndicator()
		}
		return
	}

	val u = user
	val ui = userInfo

	if (u != null && !u.isOnboarded) {
		if (loading && ui == null) {
			Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
				CircularProgressIndicator()
			}
			return
		}

		if (ui == null) {
			ScreenScaffold(title = stringResource(Res.string.onboarding_title)) {
				Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
					Text(stringResource(Res.string.onboarding_error))
					if (error != null) {
						Text(stringResource(Res.string.error_prefix, error ?: ""))
					}
					PrimaryButton(
						text = stringResource(Res.string.retry),
						onClick = { scope.launch { sessionViewModel.loadSession() } }
					)
					PrimaryButton(
						text = stringResource(Res.string.skip_onboarding),
						onClick = { scope.launch { sessionViewModel.skipOnboarding() } }
					)
				}
			}
			return
		}

		OnboardingScreen(
			user = u,
			userInfo = ui,
			onSkip = { scope.launch { sessionViewModel.skipOnboarding() } },
			onFinished = { update ->
				scope.launch {
					val ok = sessionViewModel.completeOnboarding(update)
					if (ok) selectedTab.value = MainTab.Home
				}
			}
		)
		return
	}

	Scaffold(
		bottomBar = {
			NavigationBar {
				NavigationBarItem(
					selected = selectedTab.value == MainTab.Home,
					onClick = { selectedTab.value = MainTab.Home },
					icon = { Icon(Icons.Default.Home, contentDescription = stringResource(Res.string.tab_home)) },
					label = { Text(stringResource(Res.string.tab_home)) }
				)
				NavigationBarItem(
					selected = selectedTab.value == MainTab.Exercises,
					onClick = { selectedTab.value = MainTab.Exercises },
					icon = { Icon(Icons.Default.PlayArrow, contentDescription = stringResource(Res.string.tab_exercises)) },
					label = { Text(stringResource(Res.string.tab_exercises)) }
				)
				NavigationBarItem(
					selected = selectedTab.value == MainTab.Profile,
					onClick = { selectedTab.value = MainTab.Profile },
					icon = { Icon(Icons.Default.Person, contentDescription = stringResource(Res.string.tab_profile)) },
					label = { Text(stringResource(Res.string.tab_profile)) }
				)
			}
		}
	) { innerPadding ->
		Box(modifier = Modifier.padding(innerPadding)) {
			when (selectedTab.value) {
				MainTab.Home -> HomeScreen(
					sessionViewModel = sessionViewModel,
					onOpenFullscreen = onOpenFullscreen
				)

				MainTab.Profile -> ProfileScreen(
					user = u,
					userInfo = ui,
					onLogout = { onLogoutToLogin() },
					onHelpSupportClick = {
						selectedTab.value = MainTab.HelpSupport
					}
				)

				MainTab.HelpSupport -> HelpSupportScreen(
					onBack = {
						selectedTab.value = MainTab.Profile
					},
					onSendMessage = { subject, message ->
						scope.launch {
							authService.sendSupportMessage(
								SupportMessageRequestDto(
									subject = subject,
									message = message,
									userEmail = u?.email ?: ""
								)
							)
						}
					}
				)

				MainTab.Exercises -> {
					ExercisesScreen(
						currentWeek = home?.currentWeek ?: 1,
						exercises = allExercises,
						onExerciseClick = { ex ->
							val index = allExercises.indexOf(ex)
							if (index != -1) {
								onOpenFullscreenFromList(allExercises, index)
							}
						}
					)
				}
			}
		}
	}
}