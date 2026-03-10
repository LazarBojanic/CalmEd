package com.calmed.calmedfrontendtourettes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.calmed.calmedfrontendtourettes.auth.getGoogleIdToken
import com.calmed.calmedfrontendtourettes.http.IAppApi
import com.calmed.calmedfrontendtourettes.service.specification.IAuthService
import com.calmed.calmedfrontendtourettes.settings.AppSettings
import com.calmed.calmedfrontendtourettes.store.ITokenDataStore
import com.calmed.calmedfrontendtourettes.theme.AppTheme
import com.calmed.calmedfrontendtourettes.ui.screen.ForgotPasswordScreen
import com.calmed.calmedfrontendtourettes.ui.screen.FullscreenVideoScreen
import com.calmed.calmedfrontendtourettes.ui.screen.HomeScreen
import com.calmed.calmedfrontendtourettes.ui.screen.LoginScreen
import com.calmed.calmedfrontendtourettes.ui.screen.MainScreen
import com.calmed.calmedfrontendtourettes.ui.screen.OfflineModeScreen
import com.calmed.calmedfrontendtourettes.ui.screen.PaymentScreen
import com.calmed.calmedfrontendtourettes.ui.screen.RegisterScreen
import com.calmed.calmedfrontendtourettes.ui.screen.SplashScreen
import com.calmed.calmedfrontendtourettes.ui.screen.WelcomeVideoScreen
import com.calmed.calmedfrontendtourettes.ui.screen.OnboardingScreen
import com.calmed.calmedfrontendtourettes.util.isBackendReachable
import com.calmed.calmedfrontendtourettes.viewmodel.AuthViewModel
import com.calmed.calmedfrontendtourettes.viewmodel.SessionViewModel
import com.calmed.calmedfrontendtourettes.auth.launchAppleSignIn
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

object Routes {
    const val Splash = "splash"
    const val Login = "auth/login"
    const val Register = "auth/register"
    const val ForgotPassword = "auth/forgot-password"
    const val Home = "home"
    const val WelcomeVideo = "welcome-video"
    const val FullscreenVideo = "video/fullscreen"
    const val Main = "main"
    const val Onboarding = "onboarding"
    const val Payment = "payment"
    const val Offline = "offline"
}

@Composable
fun App() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    var fullscreenVideoUrl by rememberSaveable { mutableStateOf<String?>(null) }

    val tokenStore: ITokenDataStore = koinInject()
    val authService: IAuthService = koinInject()
    val appApi: IAppApi = koinInject()

    val authViewModel = remember { AuthViewModel(authService) }

    val appSettings: AppSettings = koinInject()
    val sessionViewModel: SessionViewModel = koinInject()
    val user by sessionViewModel.user.collectAsState()
    val userInfo by sessionViewModel.userInfo.collectAsState()
    val sessionLoading by sessionViewModel.loading.collectAsState()
    val sessionError by sessionViewModel.error.collectAsState()

    suspend fun resolveNextAuthenticatedRoute(): String? {
        val remoteUser = sessionViewModel.loadSession() ?: return null
        val isOnboarded = remoteUser.isOnboarded
        val isPaid = remoteUser.isPaid
        val showWelcomeVideo = appSettings.getShowWelcomeVideo(remoteUser.id)
        return when {
            !isPaid -> Routes.Payment
            showWelcomeVideo -> Routes.WelcomeVideo
            !isOnboarded -> Routes.Onboarding
            else -> Routes.Main
        }
    }

    fun openFullscreen(url: String) {
        if (url.isBlank()) return
        fullscreenVideoUrl = url
        navController.navigate(Routes.FullscreenVideo) {
            launchSingleTop = true
        }
    }

    AppTheme {
        NavHost(navController, startDestination = Routes.Splash) {

            composable(Routes.Splash) {
                SplashScreen()

                LaunchedEffect(Unit) {
                    val online = isBackendReachable(appApi)
                    if (!online) {
                        navController.navigate(Routes.Offline) {
                            popUpTo(Routes.Splash) { inclusive = true }
                            launchSingleTop = true
                        }
                        return@LaunchedEffect
                    }

                    val currentToken = tokenStore.getToken()
                    if (currentToken == null) {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Splash) { inclusive = true }
                            launchSingleTop = true
                        }
                        return@LaunchedEffect
                    }

                    val access = currentToken.access
                    val refresh = currentToken.refresh

                    if (access.isNullOrBlank() || refresh.isNullOrBlank()) {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Splash) { inclusive = true }
                            launchSingleTop = true
                        }
                        return@LaunchedEffect
                    }

                    val refreshSuccess = authService.tryRefresh()
                    if (!refreshSuccess) {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Splash) { inclusive = true }
                            launchSingleTop = true
                        }
                        return@LaunchedEffect
                    }

                    val nextRoute = resolveNextAuthenticatedRoute()
                    if (nextRoute == null) {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Splash) { inclusive = true }
                            launchSingleTop = true
                        }
                        return@LaunchedEffect
                    }
                    navController.navigate(nextRoute) {
                        popUpTo(Routes.Splash) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            composable(Routes.Login) {
                LoginScreen(
                    onNavigateRegister = { navController.navigate(Routes.Register) },
                    onNavigateForgotPassword = { navController.navigate(Routes.ForgotPassword) },
                    onNavigateOffline = {
                        navController.navigate(Routes.Offline) {
                            popUpTo(Routes.Login) { inclusive = true }
                            launchSingleTop = true
                        }
                    },

                    onLoginSuccess = {
                        scope.launch {
                            val nextRoute = resolveNextAuthenticatedRoute()
                            if (nextRoute == null) {
                                navController.navigate(Routes.Login) {
                                    popUpTo(Routes.Login) { inclusive = true }
                                    launchSingleTop = true
                                }
                                return@launch
                            }
                            navController.navigate(nextRoute) {
                                popUpTo(Routes.Login) { inclusive = true }
                                launchSingleTop = true
                            }
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
                                    val nextRoute = resolveNextAuthenticatedRoute()
                                    if (nextRoute == null) {
                                        navController.navigate(Routes.Login) {
                                            popUpTo(Routes.Login) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                        return@launch
                                    }
                                    navController.navigate(nextRoute) {
                                        popUpTo(Routes.Login) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            } catch (t: Throwable) {
                                println("GoogleSignIn failed: ${t.message}")
                                t.printStackTrace()
                            }
                        }
                    }
                )
            }

            composable(Routes.Offline) {
                OfflineModeScreen(
                    onTryOnline = {
                        scope.launch {
                            val online = isBackendReachable(appApi)
                            if (!online) {
                                return@launch
                            }

                            val currentToken = tokenStore.getToken()
                            val access = currentToken?.access
                            val refresh = currentToken?.refresh

                            if (access.isNullOrBlank() || refresh.isNullOrBlank()) {
                                navController.navigate(Routes.Login) {
                                    popUpTo(Routes.Offline) { inclusive = true }
                                    launchSingleTop = true
                                }
                                return@launch
                            }

                            val refreshSuccess = authService.tryRefresh()
                            if (!refreshSuccess) {
                                navController.navigate(Routes.Login) {
                                    popUpTo(Routes.Offline) { inclusive = true }
                                    launchSingleTop = true
                                }
                                return@launch
                            }

                            val nextRoute = resolveNextAuthenticatedRoute()
                            if (nextRoute == null) {
                                navController.navigate(Routes.Login) {
                                    popUpTo(Routes.Offline) { inclusive = true }
                                    launchSingleTop = true
                                }
                                return@launch
                            }
                            navController.navigate(nextRoute) {
                                popUpTo(Routes.Offline) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    onOpenFullscreen = { url -> openFullscreen(url) }
                )
            }

            composable(Routes.Register) {
                RegisterScreen(
                    onNavigateLogin = {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Register) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onRegisterSuccess = {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Register) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.ForgotPassword) {
                ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Routes.WelcomeVideo) {
                val settings: AppSettings = koinInject()

                WelcomeVideoScreen(
                    onSkip = {
                        val isPaid = sessionViewModel.user.value?.isPaid == true
                        val isOnboarded = sessionViewModel.user.value?.isOnboarded == true
                        val nextRoute = when {
                            !isPaid -> Routes.Payment
                            isOnboarded -> Routes.Main
                            else -> Routes.Onboarding
                        }
                        navController.navigate(nextRoute) {
                            popUpTo(Routes.WelcomeVideo) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onContinue = { dontShowAgain ->
                        if (dontShowAgain) settings.setShowWelcomeVideo(sessionViewModel.user.value?.id, false)
                        val isPaid = sessionViewModel.user.value?.isPaid == true
                        val isOnboarded = sessionViewModel.user.value?.isOnboarded == true
                        val nextRoute = when {
                            !isPaid -> Routes.Payment
                            isOnboarded -> Routes.Main
                            else -> Routes.Onboarding
                        }
                        navController.navigate(nextRoute) {
                            popUpTo(Routes.WelcomeVideo) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onOpenFullscreen = { url -> openFullscreen(url) }
                )
            }

            composable(Routes.Onboarding) {
                val u = user
                val info = userInfo

                if (u == null) {
                    LaunchedEffect(Unit) {
                        if (!sessionLoading && sessionError == null) {
                            sessionViewModel.loadSession()
                        }
                    }
                    Text(sessionError ?: "Loading...")
                    return@composable
                }

                if (info == null) {
                    Text("UserInfo is missing (backend doesn't return it).")
                    return@composable
                }

                OnboardingScreen(
                    user = u,
                    userInfo = info,
                    onSkip = {
                        scope.launch {
                            val ok = sessionViewModel.skipOnboarding()
                            if (ok) {
                                navController.navigate(Routes.Main) {
                                    popUpTo(Routes.Onboarding) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    },
                    onFinished = { dto ->
                        scope.launch {
                            val ok = sessionViewModel.completeOnboarding(dto)
                            if (ok) {
                                navController.navigate(Routes.Main) {
                                    popUpTo(Routes.Onboarding) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                )
            }

            composable(Routes.Payment) {
                PaymentScreen(
                    onPaid = {
                        scope.launch {
                            val nextRoute = resolveNextAuthenticatedRoute() ?: Routes.Login
                            navController.navigate(nextRoute) {
                                popUpTo(Routes.Payment) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }


            composable(Routes.FullscreenVideo) {
                val activeVideoUrl = fullscreenVideoUrl
                if (activeVideoUrl.isNullOrBlank()) {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                    return@composable
                }

                FullscreenVideoScreen(
                    hlsUrl = activeVideoUrl,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }


            composable(Routes.Main) {
                MainScreen(
                    onLogoutToLogin = {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Main) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onOpenFullscreen = { url -> openFullscreen(url) }
                )
            }


            composable(Routes.Home) {
                HomeScreen(
                    sessionViewModel = koinInject(),
                    onOpenFullscreen = { url -> openFullscreen(url) }
                )
            }
        }
    }
}
