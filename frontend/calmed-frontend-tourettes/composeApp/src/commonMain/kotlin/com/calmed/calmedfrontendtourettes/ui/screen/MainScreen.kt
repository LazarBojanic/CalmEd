package com.calmed.calmedfrontendtourettes.ui.screen

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calmed.calmedfrontendtourettes.http.AppHttpClient
import com.calmed.calmedfrontendtourettes.model.dto.response.ProgramExerciseDto
import com.calmed.calmedfrontendtourettes.store.ITokenDataStore
import com.calmed.calmedfrontendtourettes.ui.component.PrimaryButton
import com.calmed.calmedfrontendtourettes.ui.component.ScreenScaffold
import com.calmed.calmedfrontendtourettes.viewmodel.SessionViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private enum class MainTab { Home, Exercises, Profile }

@Composable
fun MainScreen(
	onLogoutToLogin: () -> Unit,
	sessionViewModel: SessionViewModel = koinInject()
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

	val selectedExercise = remember { mutableStateOf<ProgramExerciseDto?>(null) }
	val showVideo = remember { mutableStateOf(false) }
	val selectedTab = remember { mutableStateOf(MainTab.Home) }

	LaunchedEffect(token?.access) {
		val access = token?.access
		if (!access.isNullOrBlank()) {
			sessionViewModel.loadSession()
			sessionViewModel.loadHome(year = 2026, month = 2)
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
			ScreenScaffold(title = "Onboarding") {
				Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
					Text("We couldn’t load your onboarding profile data.")
					if (error != null) Text("Error: $error")
					PrimaryButton(
						text = "Retry",
						onClick = { scope.launch { sessionViewModel.loadSession() } }
					)
					PrimaryButton(
						text = "Skip onboarding",
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
			if (!showVideo.value) {
				NavigationBar {
					NavigationBarItem(
						selected = selectedTab.value == MainTab.Home,
						onClick = { selectedTab.value = MainTab.Home },
						icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
						label = { Text("Home") }
					)
					NavigationBarItem(
						selected = selectedTab.value == MainTab.Exercises,
						onClick = { selectedTab.value = MainTab.Exercises },
						icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Exercises") },
						label = { Text("Exercises") }
					)
					NavigationBarItem(
						selected = selectedTab.value == MainTab.Profile,
						onClick = { selectedTab.value = MainTab.Profile },
						icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
						label = { Text("Profile") }
					)
				}
			}
		}
	) { innerPadding ->
		Box(modifier = Modifier.padding(innerPadding)) {

			if (!showVideo.value) {
				when (selectedTab.value) {
					MainTab.Home -> HomeScreen(sessionViewModel = sessionViewModel)

					MainTab.Profile -> ProfileScreen(
						user = u,
						userInfo = ui,
						onLogout = { onLogoutToLogin() }
					)

					MainTab.Exercises -> {
						val appHttpClient: AppHttpClient = koinInject()
						ExercisesScreen(
							currentWeek = home?.currentWeek ?: 1,
							exercises = allExercises,
							client = appHttpClient.client,
							onExerciseClick = { ex ->
								selectedExercise.value = ex
								showVideo.value = true
							}
						)
					}
				}
			} else {
				val ex = selectedExercise.value
				if (ex?.videoURL != null) {
					FullscreenVideoScreen(
						hlsUrl = ex.videoURL,
						onBack = { showVideo.value = false }
					)
				} else {
					showVideo.value = false
				}
			}
		}
	}
}