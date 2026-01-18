package com.calmed.calmedfrontendtourettes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.calmed.calmedfrontendtourettes.service.specification.IAuthService
import com.calmed.calmedfrontendtourettes.store.ITokenDataStore
import com.calmed.calmedfrontendtourettes.theme.AppTheme
import com.calmed.calmedfrontendtourettes.ui.screen.ForgotPasswordScreen
import com.calmed.calmedfrontendtourettes.ui.screen.LoginScreen
import com.calmed.calmedfrontendtourettes.ui.screen.MainScreen
import com.calmed.calmedfrontendtourettes.ui.screen.RegisterScreen
import com.calmed.calmedfrontendtourettes.ui.screen.SplashScreen
import com.calmed.calmedfrontendtourettes.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.calmed.calmedfrontendtourettes.auth.getGoogleIdToken

object Routes {
    const val Splash = "splash"
    const val Login = "auth/login"
    const val Register = "auth/register"
    const val ForgotPassword = "auth/forgot-password"
    const val Main = "main"
}

@Composable
fun App() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val tokenStore: ITokenDataStore = koinInject()
    val authService: IAuthService = koinInject()

    val token by tokenStore.tokenDto.collectAsState()
    val authViewModel = AuthViewModel(authService)

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
                                navController.navigate(Routes.Main) {
                                    popUpTo(Routes.Splash) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                navController.navigate(Routes.Login) {
                                    popUpTo(Routes.Splash) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        } else {
                            navController.navigate(Routes.Login) {
                                popUpTo(Routes.Splash) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    } else {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Splash) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }

            composable(Routes.Login) {
                LoginScreen(
                    onNavigateRegister = { navController.navigate(Routes.Register) },
                    onNavigateForgotPassword = { navController.navigate(Routes.ForgotPassword) },
                    onLoginSuccess = {
                        navController.navigate(Routes.Main) {
                            popUpTo(Routes.Login) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onGoogleSignIn = {
                        scope.launch {
                            try {
                                val googleToken = getGoogleIdToken()
                                val ok = authViewModel.loginWithGoogle(googleToken)
                                if (ok) {
                                    navController.navigate(Routes.Main) {
                                        popUpTo(Routes.Login) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            } catch (_: Throwable) {
                            }
                        }
                    }
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

            composable(Routes.Main) {
                MainScreen(
                    onLogoutToLogin = {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Main) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}