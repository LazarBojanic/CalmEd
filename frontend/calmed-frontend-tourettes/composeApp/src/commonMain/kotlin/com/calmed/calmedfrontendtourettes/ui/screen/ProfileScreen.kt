package com.calmed.calmedfrontendtourettes.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			verticalArrangement = Arrangement.spacedBy(16.dp),
			contentPadding = PaddingValues(bottom = 24.dp)
		) {
			item {
				ProfileHeader(user, userInfo)
			}

			if (user != null) {
				item {
					InfoSection(title = "Account Information") {
						InfoRow(icon = Icons.Default.Email, label = "Email", value = user.email)
						InfoRow(icon = Icons.Default.Person, label = "Username", value = user.username)
					}
				}
			}

			if (userInfo != null) {
				item {
					InfoSection(title = "Personal Details") {
						userInfo.age?.let {
							InfoRow(icon = Icons.Default.DateRange, label = "Age", value = it.toString())
						}
						userInfo.stressLevel?.let {
							InfoRow(icon = Icons.Default.Warning, label = "Stress Level", value = "$it/10")
						}
					}
				}

				item {
					InfoSection(title = "Condition Info") {
						userInfo.tickType?.let {
							InfoRow(icon = Icons.Default.Info, label = "Tick Type", value = it.name)
						}
						userInfo.tickFrequency?.let {
							InfoRow(icon = Icons.Default.Refresh, label = "Frequency", value = it.name)
						}
						userInfo.goal?.let {
							InfoRow(icon = Icons.Default.Star, label = "Goal", value = it)
						}
						userInfo.followProgress?.let {
							InfoRow(
								icon = Icons.Default.CheckCircle,
								label = "Following Progress",
								value = if (it) "Yes" else "No"
							)
						}
					}
				}
			}

			item {
				Spacer(modifier = Modifier.height(8.dp))
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
}

@Composable
fun ProfileHeader(user: UserJoined?, userInfo: UserInfoTourettesJoined?) {
	val displayName = userInfo?.preferredName ?: user?.username ?: "User"
	val email = user?.email ?: ""

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 24.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Box(
			modifier = Modifier
				.size(100.dp)
				.clip(CircleShape)
				.background(MaterialTheme.colorScheme.primaryContainer),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = Icons.Default.Person,
				contentDescription = null,
				modifier = Modifier.size(64.dp),
				tint = MaterialTheme.colorScheme.onPrimaryContainer
			)
		}

		Spacer(modifier = Modifier.height(16.dp))

		Text(
			text = displayName,
			style = MaterialTheme.typography.headlineMedium,
			fontWeight = FontWeight.Bold
		)

		if (email.isNotEmpty()) {
			Text(
				text = email,
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

@Composable
fun InfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.SemiBold,
			color = MaterialTheme.colorScheme.primary,
			modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
		)
		Card(
			modifier = Modifier.fillMaxWidth(),
			shape = RoundedCornerShape(12.dp),
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
			)
		) {
			Column(
				modifier = Modifier.padding(16.dp),
				verticalArrangement = Arrangement.spacedBy(12.dp)
			) {
				content()
			}
		}
	}
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			modifier = Modifier.size(20.dp),
			tint = MaterialTheme.colorScheme.onSurfaceVariant
		)
		Spacer(modifier = Modifier.width(12.dp))
		Column {
			Text(
				text = label,
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Text(
				text = value,
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = FontWeight.Medium
			)
		}
	}
}