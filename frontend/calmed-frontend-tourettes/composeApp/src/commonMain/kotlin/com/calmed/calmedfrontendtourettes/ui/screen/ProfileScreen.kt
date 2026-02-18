package com.calmed.calmedfrontendtourettes.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calmed.calmedfrontendtourettes.model.dto.request.UserInfoTourettesUpdateDto
import com.calmed.calmedfrontendtourettes.model.joined.UserInfoTourettesJoined
import com.calmed.calmedfrontendtourettes.model.joined.UserJoined
import com.calmed.calmedfrontendtourettes.model.raw.TickFrequency
import com.calmed.calmedfrontendtourettes.model.raw.TickType
import com.calmed.calmedfrontendtourettes.ui.component.PrimaryButton
import com.calmed.calmedfrontendtourettes.ui.component.ScreenScaffold
import com.calmed.calmedfrontendtourettes.ui.component.TextField
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
	val loading by sessionViewModel.loading.collectAsState()
	val error by sessionViewModel.error.collectAsState()

	var isEditing by remember { mutableStateOf(false) }

	val preferredName = remember { mutableStateOf(userInfo?.preferredName ?: "") }
	val age = remember { mutableIntStateOf(userInfo?.age ?: 18) }
	val stress = remember { mutableIntStateOf(userInfo?.stressLevel ?: 5) }
	val tickType = remember { mutableStateOf(userInfo?.tickType ?: TickType.BOTH) }
	val tickFrequency = remember { mutableStateOf(userInfo?.tickFrequency ?: TickFrequency.MODERATE) }
	val goal = remember { mutableStateOf(userInfo?.goal ?: "") }
	val followProgress = remember { mutableStateOf(userInfo?.followProgress ?: true) }
	val ageText = remember { mutableStateOf(age.intValue.toString()) }

	LaunchedEffect(userInfo?.id) {
		preferredName.value = userInfo?.preferredName ?: ""
		age.intValue = userInfo?.age ?: 18
		stress.intValue = userInfo?.stressLevel ?: 5
		tickType.value = userInfo?.tickType ?: TickType.BOTH
		tickFrequency.value = userInfo?.tickFrequency ?: TickFrequency.MODERATE
		goal.value = userInfo?.goal ?: ""
		followProgress.value = userInfo?.followProgress ?: true
		ageText.value = age.intValue.toString()
	}

	LaunchedEffect(age.intValue) {
		ageText.value = age.intValue.toString()
	}

	fun buildUpdate(u: UserJoined): UserInfoTourettesUpdateDto {
		return UserInfoTourettesUpdateDto(
			userId = u.id,
			preferredName = preferredName.value.trim(),
			age = age.intValue,
			stressLevel = stress.intValue,
			tickType = tickType.value,
			tickFrequency = tickFrequency.value,
			goal = goal.value.trim(),
			followProgress = followProgress.value
		)
	}

	@Composable
	fun RadioOptionRow(
		text: String,
		selected: Boolean,
		onClick: () -> Unit,
		modifier: Modifier = Modifier
	) {
		Surface(
			modifier = modifier.fillMaxWidth(),
			shape = MaterialTheme.shapes.medium,
			color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
			border = BorderStroke(
				width = 1.dp,
				color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
			)
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.selectable(
						selected = selected,
						onClick = onClick,
						role = androidx.compose.ui.semantics.Role.RadioButton
					)
					.padding(horizontal = 12.dp, vertical = 12.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				RadioButton(selected = selected, onClick = null)
				Spacer(Modifier.width(10.dp))
				Text(text = text, style = MaterialTheme.typography.bodyLarge)
			}
		}
	}

	ScreenScaffold(title = "Profile") {
		LazyColumn(
			modifier = Modifier.fillMaxWidth(),
			verticalArrangement = Arrangement.spacedBy(16.dp),
			contentPadding = PaddingValues(bottom = 24.dp)
		) {
			item {
				ProfileHeader(user, userInfo)
			}

			if (user != null && userInfo == null) {
				item {
					InfoSection(title = "Personal Details") {
						Text("We couldn’t load your profile details.")
						if (error != null) {
							Text("Error: $error")
						}
						PrimaryButton(
							text = if (loading) "Loading..." else "Retry",
							enabled = !loading,
							onClick = { scope.launch { sessionViewModel.loadSession() } }
						)
					}
				}
			}

			if (user != null && userInfo != null) {
				if (!isEditing) {
					item {
						PrimaryButton(
							text = "Edit profile",
							onClick = { isEditing = true }
						)
					}
				} else {
					item {
						InfoSection(title = "Edit Personal Details") {
							TextField(
								value = preferredName.value,
								onValueChange = { preferredName.value = it },
								label = "Preferred name",
								singleLine = true
							)

							Text("Age")
							TextField(
								value = ageText.value,
								onValueChange = { raw ->
									val digitsOnly = raw.filter { it.isDigit() }
									ageText.value = digitsOnly
									val parsed = digitsOnly.toIntOrNull()
									if (parsed != null) {
										age.intValue = parsed.coerceIn(5, 80)
									}
								},
								label = "Age",
								singleLine = true
							)
							Slider(
								value = age.intValue.toFloat(),
								onValueChange = { age.intValue = it.toInt().coerceIn(5, 80) },
								valueRange = 5f..80f,
								steps = 74
							)

							Text("Stress: ${stress.intValue} / 10")
							Slider(
								value = stress.intValue.toFloat(),
								onValueChange = { stress.intValue = it.toInt() },
								valueRange = 0f..10f,
								steps = 9
							)
						}
					}

					item {
						InfoSection(title = "Edit Condition Info") {
							Text("Tics type")
							Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
								RadioOptionRow(
									text = "Motor",
									selected = tickType.value == TickType.MOTOR,
									onClick = { tickType.value = TickType.MOTOR }
								)
								RadioOptionRow(
									text = "Vocal",
									selected = tickType.value == TickType.VOCAL,
									onClick = { tickType.value = TickType.VOCAL }
								)
								RadioOptionRow(
									text = "Both",
									selected = tickType.value == TickType.BOTH,
									onClick = { tickType.value = TickType.BOTH }
								)
							}

							Text("Tics frequency")
							Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
								RadioOptionRow(
									text = "Rare",
									selected = tickFrequency.value == TickFrequency.RARE,
									onClick = { tickFrequency.value = TickFrequency.RARE }
								)
								RadioOptionRow(
									text = "Moderate",
									selected = tickFrequency.value == TickFrequency.MODERATE,
									onClick = { tickFrequency.value = TickFrequency.MODERATE }
								)
								RadioOptionRow(
									text = "Daily",
									selected = tickFrequency.value == TickFrequency.DAILY,
									onClick = { tickFrequency.value = TickFrequency.DAILY }
								)
							}

							TextField(
								value = goal.value,
								onValueChange = { goal.value = it },
								label = "Your goal",
								singleLine = false
							)

							Row(
								modifier = Modifier.fillMaxWidth(),
								verticalAlignment = Alignment.CenterVertically
							) {
								Text("Follow progress")
								Spacer(modifier = Modifier.width(12.dp))
								Switch(
									checked = followProgress.value,
									onCheckedChange = { followProgress.value = it }
								)
							}
						}
					}

					item {
						PrimaryButton(
							text = if (loading) "Saving..." else "Save",
							enabled = !loading,
							onClick = {
								val u = user
								if (u != null) {
									scope.launch {
										val ok = sessionViewModel.updateProfileUserInfoTourettes(buildUpdate(u))
										if (ok) {
											isEditing = false
										}
									}
								}
							}
						)
					}
					item {
						PrimaryButton(
							text = "Cancel",
							enabled = !loading,
							onClick = {
								isEditing = false
							}
						)
					}
				}
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