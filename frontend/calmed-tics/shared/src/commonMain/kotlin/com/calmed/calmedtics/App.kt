package com.calmed.calmedtics

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
import com.calmed.calmedtics.auth.getGoogleIdToken
import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.service.specification.IAuthService
import com.calmed.calmedtics.settings.AppSettings
import com.calmed.calmedtics.store.ITokenDataStore
import com.calmed.calmedtics.theme.AppTheme
import com.calmed.calmedtics.ui.screen.ForgotPasswordScreen
import com.calmed.calmedtics.ui.screen.VideoScreen
import com.calmed.calmedtics.ui.screen.HomeScreen
import com.calmed.calmedtics.ui.screen.LoginScreen
import com.calmed.calmedtics.ui.screen.MainScreen
import com.calmed.calmedtics.ui.screen.OfflineModeScreen
import com.calmed.calmedtics.ui.screen.PaymentScreen
import com.calmed.calmedtics.ui.screen.RegisterScreen
import com.calmed.calmedtics.ui.screen.SplashScreen
import com.calmed.calmedtics.ui.screen.WelcomeVideoScreen
import com.calmed.calmedtics.ui.screen.CourseOverviewScreen
import com.calmed.calmedtics.ui.screen.OnboardingScreen
import com.calmed.calmedtics.ui.screen.AgeConfirmScreen
import com.calmed.calmedtics.util.isBackendReachable
import com.calmed.calmedtics.viewmodel.AuthViewModel
import com.calmed.calmedtics.viewmodel.SessionViewModel
import com.calmed.calmedtics.auth.launchAppleSignIn
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.calmed.calmedtics.localization.AppLocaleProvider
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto

object Routes {
    const val Splash = "splash"
    const val Login = "auth/login"
    const val Register = "auth/register"
    const val ForgotPassword = "auth/forgot-password"
    const val Home = "home"
    const val WelcomeVideo = "welcome-video"
    const val CourseOverview = "course-overview"
    const val Video = "video"
    const val Main = "main"
    const val Onboarding = "onboarding"
    const val AgeConfirm = "age-confirm"
    const val Payment = "payment"
    const val Offline = "offline"
}

@Composable
fun App() {
    val httpClient: HttpClient = koinInject()
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory(httpClient))
            }
            .crossfade(true)
            .logger(DebugLogger())
            .build()
    }

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    var videoExercises by remember { mutableStateOf<List<ProgramExerciseDto>?>(null) }
    var videoIndex by remember { mutableStateOf(0) }
    var welcomeHandledUserId by rememberSaveable { mutableStateOf<String?>(null) }
    var courseOverviewHandledUserId by rememberSaveable { mutableStateOf<String?>(null) }

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

    LaunchedEffect(Unit) {
    }

    suspend fun resolveNextAuthenticatedRoute(): String? {
        val remoteUser = sessionViewModel.loadSession() ?: return null
        val isOnboarded = remoteUser.isOnboarded
        val isPaid = appApi.getPaymentStatus()?.hasAccess ?: false
        val showWelcomeVideo = appSettings.getShowWelcomeVideo(remoteUser.id)
        val shouldShowWelcomeVideo = showWelcomeVideo && welcomeHandledUserId != remoteUser.id
        val showCourseOverview = appSettings.getShowCourseOverview(remoteUser.id)
        val shouldShowCourseOverview =
            showCourseOverview && courseOverviewHandledUserId != remoteUser.id
        return when {
            !remoteUser.confirmOverEighteen -> Routes.AgeConfirm
            shouldShowWelcomeVideo -> Routes.WelcomeVideo
            shouldShowCourseOverview -> Routes.CourseOverview
            !isPaid -> Routes.Payment
            !isOnboarded -> Routes.Onboarding
            else -> Routes.Main
        }
    }

    fun openVideo(url: String, title: String? = null) {
        if (url.isBlank()) return
        videoExercises = listOf(
            ProgramExerciseDto(
                id = "",
                videoURL = url,
                title = title.orEmpty(),
                description = "",
                weekNumber = 1
            )
        )
        videoIndex = 0
        navController.navigate(Routes.Video) {
            launchSingleTop = true
        }
    }

    fun openVideoFromList(
        exercises: List<ProgramExerciseDto>,
        startIndex: Int,
    ) {
        videoExercises = exercises
        videoIndex = startIndex

        navController.navigate(Routes.Video) {
            launchSingleTop = true
        }
    }
    AppLocaleProvider {
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

                composable(Routes.AgeConfirm) {
                    AgeConfirmScreen(
                        loading = sessionLoading,
                        error = sessionError,
                        onConfirm = {
                            scope.launch {
                                val ok = sessionViewModel.confirmOverEighteen()
                                if (ok) {
                                    val nextRoute = resolveNextAuthenticatedRoute()
                                        ?: Routes.Login
                                    navController.navigate(nextRoute) {
                                        popUpTo(Routes.AgeConfirm) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        },
                        onDecline = {
                            scope.launch {
                                authService.logout()
                                navController.navigate(Routes.Login) {
                                    popUpTo(Routes.AgeConfirm) { inclusive = true }
                                    launchSingleTop = true
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
                        onOpenVideo = { url, title -> openVideo(url, title) }
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
                        },

                        onGoogleSignIn = {
                            scope.launch {
                                try {
                                    val googleToken = getGoogleIdToken()

                                    val ok = authViewModel.loginWithGoogle(
                                        googleToken
                                    )

                                    if (ok) {
                                        val nextRoute =
                                            resolveNextAuthenticatedRoute()
                                                ?: Routes.Login

                                        navController.navigate(nextRoute) {
                                            popUpTo(Routes.Register) {
                                                inclusive = true
                                            }
                                            launchSingleTop = true
                                        }
                                    }
                                } catch (t: Throwable) {
                                    println(
                                        "GoogleSignIn Register failed: ${t.message}"
                                    )
                                    t.printStackTrace()
                                }
                            }
                        },

                        onAppleSignIn = {
                            launchAppleSignIn()
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
                            welcomeHandledUserId = sessionViewModel.user.value?.id
                            navController.navigate(Routes.CourseOverview) {
                                popUpTo(Routes.WelcomeVideo) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onContinue = { dontShowAgain ->
                            welcomeHandledUserId = sessionViewModel.user.value?.id
                            if (dontShowAgain) settings.setShowWelcomeVideo(
                                sessionViewModel.user.value?.id,
                                false
                            )
                            navController.navigate(Routes.CourseOverview) {
                                popUpTo(Routes.WelcomeVideo) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(Routes.CourseOverview) {
                    val settings: AppSettings = koinInject()

                    CourseOverviewScreen(
                        onSkip = {
                            courseOverviewHandledUserId = sessionViewModel.user.value?.id
                            scope.launch {
                                val nextRoute = resolveNextAuthenticatedRoute() ?: Routes.Payment
                                navController.navigate(nextRoute) {
                                    popUpTo(Routes.CourseOverview) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        },
                        onContinue = { dontShowAgain ->
                            courseOverviewHandledUserId = sessionViewModel.user.value?.id
                            if (dontShowAgain) settings.setShowCourseOverview(
                                sessionViewModel.user.value?.id,
                                false
                            )
                            scope.launch {
                                val nextRoute = resolveNextAuthenticatedRoute() ?: Routes.Payment
                                navController.navigate(nextRoute) {
                                    popUpTo(Routes.CourseOverview) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
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
                        },
                        onLogout = {
                            navController.navigate(Routes.Login) {
                                popUpTo(Routes.Payment) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }


                composable(Routes.Video) {
                    val exercises = videoExercises

                    if (exercises == null || exercises.isEmpty()) {
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                        }
                        return@composable
                    }

                    VideoScreen(
                        exercises = exercises,
                        startIndex = videoIndex,
                        currentWeek = sessionViewModel.home.value?.currentWeek ?: 1,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
                    composable(Routes.Main) {
                        MainScreen(
                            onLogoutToLogin = {
                                welcomeHandledUserId = null
                                navController.navigate(Routes.Login) {
                                    popUpTo(Routes.Main) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onAccountDeleted = {
                                welcomeHandledUserId = null
                                courseOverviewHandledUserId = null
                                navController.navigate(Routes.Login) {
                                    popUpTo(Routes.Splash) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onOpenVideoFromList = { exercises, startIndex ->
                                openVideoFromList(exercises, startIndex)
                            }
                        )
                    }


                    composable(Routes.Home) {
                        HomeScreen(
                            sessionViewModel = koinInject(),
                            onExerciseClick = { exercise ->
                                val allExercises = sessionViewModel.allExercises.value
                                val currentWeek = sessionViewModel.home.value?.currentWeek ?: 1

                                val availableExercises = allExercises.filter {
                                    it.weekNumber in 1..currentWeek
                                }

                                val index = availableExercises.indexOfFirst {
                                    it.id == exercise.id
                                }

                                if (index != -1) {
                                    openVideoFromList(availableExercises, index)
                                } else {
                                    openVideoFromList(listOf(exercise), 0)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
