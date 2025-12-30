package com.calmed.calmedfrontendtourettes

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.calmed.calmedfrontendtourettes.theme.AppTheme
import com.calmed.calmedfrontendtourettes.ui.screen.WelcomeScreen

object Routes {
	const val Welcome = "welcome"
	const val Login = "auth/login"
	const val Register = "auth/register"
	const val Home = "home"
}

@Composable
fun App() {
	val navController = rememberNavController()
	AppTheme{
		NavHost(navController, Routes.Welcome) {
			composable(Routes.Welcome) {
				WelcomeScreen()
			}
		}
	}

}