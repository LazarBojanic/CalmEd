package com.calmed.calmedfrontendtourettes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
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
import org.koin.compose.koinInject

object Routes {
    const val Splash = "splash"
    const val Login = "auth/login"
    const val Register = "auth/register"
    const val ForgotPassword = "auth/forgot-password"
    const val Home = "home"
}

@Composable
fun App() {
    val navController = rememberNavController()
    val tokenStore: ITokenDataStore = koinInject()
    val authService: IAuthService = koinInject()

    val token by tokenStore.tokenDto.collectAsState()

    AppTheme {
        NavHost(navController, startDestination = Routes.Splash) {
            composable(Routes.Splash) {
                SplashScreen()
                LaunchedEffect(token) {
                    // On app start: if we have tokens, try a refresh once (covers expired access)
                    if (token?.refresh?.isNotBlank() == true) {
                        authService.tryRefresh()
                    }

                    val nowToken = tokenStore.tokenDto.value
                    val target = if (nowToken?.access?.isNotBlank() == true && nowToken?.refresh?.isNotBlank() == true) {
                        Routes.Home
                    } else {
                        Routes.Login
                    }

                    navController.navigate(target) {
                        popUpTo(Routes.Splash) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            composable(Routes.Login) {
                LoginScreen(
                    onNavigateRegister = { navController.navigate(Routes.Register) },
                    onNavigateForgotPassword = { navController.navigate(Routes.ForgotPassword) },
                    onLoginSuccess = {
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.Login) { inclusive = true }
                            launchSingleTop = true
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
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.Register) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.ForgotPassword) {
                ForgotPasswordScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.Home) {
                HomeScreen(
                    onLogout = {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Home) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}