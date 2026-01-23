package com.calmed.calmedfrontendtourettes.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import com.calmed.calmedfrontendtourettes.model.joined.UserInfoTourettesJoined
import com.calmed.calmedfrontendtourettes.model.joined.UserJoined
import com.calmed.calmedfrontendtourettes.ui.component.PrimaryButton
import com.calmed.calmedfrontendtourettes.ui.component.ScreenScaffold
import com.calmed.calmedfrontendtourettes.viewmodel.SessionViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ProfileScreen(
	user: UserJoined?,
	userInfo: UserInfoTourettesJoined?,
	onLogout: () -> Unit,
	sessionViewModel: SessionViewModel = koinInject()
) {
	val scope = rememberCoroutineScope()

	ScreenScaffold(title = "Profile") {
		Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
			Text("User", style = MaterialTheme.typography.titleLarge)

			if (user == null) {
				Text("No user loaded.")
			} else {
				Text("ID: ${user.id}")
				Text("Email: ${user.email}")
				Text("Username: ${user.username}")
				Text("Email verified: ${user.isEmailVerified}")
				Text("Onboarded: ${user.isOnboarded}")
			}

			Text("UserInfoTourettes", style = MaterialTheme.typography.titleLarge)

			if (userInfo == null) {
				Text("No user info loaded.")
			} else {
				Text("Preferred name: ${userInfo.preferredName ?: "-"}")
				Text("Age: ${userInfo.age?.toString() ?: "-"}")
				Text("Stress level: ${userInfo.stressLevel?.toString() ?: "-"}")
				Text("Tick type: ${userInfo.tickType?.name ?: "-"}")
				Text("Tick frequency: ${userInfo.tickFrequency?.name ?: "-"}")
				Text("Goal: ${userInfo.goal ?: "-"}")
				Text("Follow progress: ${userInfo.followProgress?.toString() ?: "-"}")
			}

			PrimaryButton(
				text = "Logout",
				onClick = {
					scope.launch {
						sessionViewModel.logout()
						onLogout()
					}
				}
			)
		}
	}
}