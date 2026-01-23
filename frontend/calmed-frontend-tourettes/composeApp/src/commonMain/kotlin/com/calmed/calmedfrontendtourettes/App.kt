package com.calmed.calmedfrontendtourettes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.calmed.calmedfrontendtourettes.service.specification.IAuthService
import com.calmed.calmedfrontendtourettes.store.ITokenDataStore
import com.calmed.calmedfrontendtourettes.theme.AppTheme
import com.calmed.calmedfrontendtourettes.ui.screen.ForgotPasswordScreen
import com.calmed.calmedfrontendtourettes.ui.screen.HomeScreen
import com.calmed.calmedfrontendtourettes.ui.screen.LoginScreen
import com.calmed.calmedfrontendtourettes.ui.screen.RegisterScreen
import com.calmed.calmedfrontendtourettes.ui.screen.SplashScreen
import com.calmed.calmedfrontendtourettes.ui.screen.WelcomeVideoScreen
import org.koin.compose.koinInject
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.calmed.calmedfrontendtourettes.auth.getGoogleIdToken
import com.calmed.calmedfrontendtourettes.http.IAppApi
import com.calmed.calmedfrontendtourettes.viewmodel.AuthViewModel
import com.calmed.calmedfrontendtourettes.settings.AppSettings
import com.calmed.calmedfrontendtourettes.ui.screen.FullscreenVideoScreen
import org.koin.compose.koinInject


object Routes {
    const val Splash = "splash"
    const val Login = "auth/login"
    const val Register = "auth/register"
    const val ForgotPassword = "auth/forgot-password"
    const val Home = "home"
    const val WelcomeVideo = "welcome-video"
    const val FullscreenVideo = "video/fullscreen"
}

@Composable
fun App() {


    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val tokenStore: ITokenDataStore = koinInject()
    val authService: IAuthService = koinInject()
    val appApi: IAppApi = koinInject()

    val token by tokenStore.tokenDto.collectAsState()
    val authViewModel = remember { AuthViewModel(authService) }
    val appSettings: AppSettings = koinInject()
    val showWelcomeVideo = remember { appSettings.getShowWelcomeVideo() }


    AppTheme {
        NavHost(navController, startDestination = Routes.Splash) {
            composable(Routes.Splash) {
                SplashScreen()
                LaunchedEffect(token) {
                    val currentToken = tokenStore.tokenDto.value
                    if (currentToken != null) {
                        val access = currentToken.access
                        val refresh = currentToken.refresh
                        if (access != null && access.isNotBlank() && refresh != null && refresh.isNotBlank()) {
                            val refreshSuccess = authService.tryRefresh()
                            if (refreshSuccess) {
                                val nextRoute = if (showWelcomeVideo) {
                                    Routes.WelcomeVideo
                                } else {
                                    Routes.Home
                                }

                                navController.navigate(nextRoute) {
                                    popUpTo(Routes.Splash) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }else {
                                navController.navigate(Routes.Login) {
                                    popUpTo(Routes.Splash) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        } else {
                            navController.navigate(Routes.Login) {
                                popUpTo(Routes.Splash) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    } else {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Splash) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            }

            composable(Routes.Login) {
                LoginScreen(
                    onNavigateRegister = {
                        navController.navigate(Routes.Register)
                    },
                    onNavigateForgotPassword = {
                        navController.navigate(Routes.ForgotPassword)
                    },
                    onLoginSuccess = {
                        val nextRoute = if (showWelcomeVideo) Routes.WelcomeVideo else Routes.Home
                        navController.navigate(nextRoute) {
                            popUpTo(Routes.Login) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onGoogleSignIn = {
                        scope.launch {
                            try {
                                val token = getGoogleIdToken()
                                println("GOOGLE TOKEN LEN = ${token.length}")

                                println("GOOGLE BACKEND: calling loginWithGoogle...")
                                val ok = authViewModel.loginWithGoogle(token)
                                println("GOOGLE BACKEND OK = $ok")
                                println("UI: currentRoute=${navController.currentDestination?.route}")

                                if (ok) {
                                    val nextRoute = if (showWelcomeVideo) Routes.WelcomeVideo else Routes.Home
                                    println("UI: navigating now...")
                                    println("NAVIGATE -> HOME")
                                    navController.navigate(nextRoute) {
                                        popUpTo(Routes.Login) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    println("Google backend login returned false")
                                }
                            } catch (t: Throwable) {
                                println("Google sign-in failed: ${t::class.simpleName} - ${t.message}")
                                t.printStackTrace()
                            }
                        }
                    }
                )
            }

            composable(Routes.Register) {
                RegisterScreen(
                    onNavigateLogin = {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Register) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onRegisterSuccess = {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Register) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.ForgotPassword) {
                ForgotPasswordScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.WelcomeVideo) {
                val appSettings: AppSettings = koinInject()

                WelcomeVideoScreen(
                    onSkip = {
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.WelcomeVideo) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onContinue = { dontShowAgain ->
                        if (dontShowAgain) {
                            appSettings.setShowWelcomeVideo(false)
                        }
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.WelcomeVideo) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onFullscreen = {
                        navController.navigate(Routes.FullscreenVideo)
                    }
                )
            }


            composable(Routes.FullscreenVideo) {
                FullscreenVideoScreen(
                    hlsUrl = "https://bombona.rs/videos/testvideo/testvideo.m3u8",
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.Home) {
                HomeScreen(
                    onLogout = {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Home) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}