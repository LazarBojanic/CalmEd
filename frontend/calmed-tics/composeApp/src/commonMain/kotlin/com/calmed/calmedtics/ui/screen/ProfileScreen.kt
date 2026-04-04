package com.calmed.calmedtics.ui.screen

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
import com.calmed.calmedtics.Res
import com.calmed.calmedtics.account_info
import com.calmed.calmedtics.age
import com.calmed.calmedtics.cancel
import com.calmed.calmedtics.condition_info
import com.calmed.calmedtics.default_user
import com.calmed.calmedtics.edit_condition_info
import com.calmed.calmedtics.edit_personal_details
import com.calmed.calmedtics.edit_profile
import com.calmed.calmedtics.email
import com.calmed.calmedtics.error_prefix
import com.calmed.calmedtics.follow_progress
import com.calmed.calmedtics.follow_progress_title
import com.calmed.calmedtics.following_progress
import com.calmed.calmedtics.frequency_daily
import com.calmed.calmedtics.frequency_moderate
import com.calmed.calmedtics.frequency_rare
import com.calmed.calmedtics.goal
import com.calmed.calmedtics.goal_label
import com.calmed.calmedtics.help_support
import com.calmed.calmedtics.language_english
import com.calmed.calmedtics.language_settings
import com.calmed.calmedtics.language_spanish
import com.calmed.calmedtics.loading
import com.calmed.calmedtics.localization.customAppLocale
import com.calmed.calmedtics.logout
import com.calmed.calmedtics.model.dto.request.UserInfoTicsUpdateDto
import com.calmed.calmedtics.model.joined.UserInfoTicsJoined
import com.calmed.calmedtics.model.joined.UserJoined
import com.calmed.calmedtics.model.raw.TickFrequency
import com.calmed.calmedtics.model.raw.TickType
import com.calmed.calmedtics.morning_evening
import com.calmed.calmedtics.no
import com.calmed.calmedtics.personal_details
import com.calmed.calmedtics.preferred_name
import com.calmed.calmedtics.privacy_policy
import com.calmed.calmedtics.profile_error
import com.calmed.calmedtics.profile_title
import com.calmed.calmedtics.reminders
import com.calmed.calmedtics.ui.component.PrimaryButton
import com.calmed.calmedtics.ui.component.ScreenScaffold
import com.calmed.calmedtics.ui.component.TextField
import com.calmed.calmedtics.viewmodel.SessionViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.calmed.calmedtics.reminders.ReminderManager
import com.calmed.calmedtics.retry
import com.calmed.calmedtics.save
import com.calmed.calmedtics.saving
import com.calmed.calmedtics.stress_level
import com.calmed.calmedtics.stress_value
import com.calmed.calmedtics.support
import com.calmed.calmedtics.terms
import com.calmed.calmedtics.tics_both
import com.calmed.calmedtics.tics_frequency
import com.calmed.calmedtics.tics_motor
import com.calmed.calmedtics.tics_type
import com.calmed.calmedtics.tics_type_title
import com.calmed.calmedtics.use_system_language
import com.calmed.calmedtics.username
import com.calmed.calmedtics.yes
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileScreen(
	user: UserJoined?,
	userInfo: UserInfoTicsJoined?,
	onLogout: () -> Unit,
	onHelpSupportClick: () -> Unit = {},
	appSettings: com.calmed.calmedtics.settings.AppSettings = koinInject(),
	sessionViewModel: SessionViewModel = koinInject()
) {
	val scope = rememberCoroutineScope()
	var remindersEnabled by remember { mutableStateOf(appSettings.isRemindersEnabled()) }
	val reminderManager = ReminderManager()
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

	LaunchedEffect(Unit) {
		if (remindersEnabled) {
			reminderManager.enableMorningAndEvening()
		}
	}
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

	fun buildUpdate(u: UserJoined): UserInfoTicsUpdateDto {
		return UserInfoTicsUpdateDto(
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

	ScreenScaffold(title = stringResource(Res.string.profile_title)){
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
					InfoSection(title = stringResource(Res.string.personal_details)) {
						Text(stringResource(Res.string.profile_error))
						if (error != null) {
							Text(stringResource(Res.string.error_prefix, error ?: ""))
						}
						PrimaryButton(
							text = if (loading) stringResource(Res.string.loading) else stringResource(Res.string.retry),
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
							text = stringResource(Res.string.edit_profile),
							onClick = { isEditing = true }
						)
					}
				} else {
					item {
						InfoSection(title = stringResource(Res.string.edit_personal_details)) {
							TextField(
								value = preferredName.value,
								onValueChange = { preferredName.value = it },
								label = stringResource(Res.string.preferred_name),
								singleLine = true
							)

							Text(stringResource(Res.string.age))
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
								label = stringResource(Res.string.age),
								singleLine = true
							)
							Slider(
								value = age.intValue.toFloat(),
								onValueChange = { age.intValue = it.toInt().coerceIn(5, 80) },
								valueRange = 5f..80f,
								steps = 74
							)

							Text(stringResource(Res.string.stress_value, stress.intValue))
							Slider(
								value = stress.intValue.toFloat(),
								onValueChange = { stress.intValue = it.toInt() },
								valueRange = 0f..10f,
								steps = 9
							)
						}
					}

					item {
						InfoSection(title = stringResource(Res.string.edit_condition_info)) {
							Text( stringResource(Res.string.tics_type))
							Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
								RadioOptionRow(
									text =  stringResource(Res.string.tics_motor),
									selected = tickType.value == TickType.MOTOR,
									onClick = { tickType.value = TickType.MOTOR }
								)
								RadioOptionRow(
									text =  stringResource(Res.string.tics_motor),
									selected = tickType.value == TickType.VOCAL,
									onClick = { tickType.value = TickType.VOCAL }
								)
								RadioOptionRow(
									text =  stringResource(Res.string.tics_both),
									selected = tickType.value == TickType.BOTH,
									onClick = { tickType.value = TickType.BOTH }
								)
							}

							Text( stringResource(Res.string.tics_frequency))
							Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
								RadioOptionRow(
									text =  stringResource(Res.string.frequency_rare),
									selected = tickFrequency.value == TickFrequency.RARE,
									onClick = { tickFrequency.value = TickFrequency.RARE }
								)
								RadioOptionRow(
									text =  stringResource(Res.string.frequency_moderate),
									selected = tickFrequency.value == TickFrequency.MODERATE,
									onClick = { tickFrequency.value = TickFrequency.MODERATE }
								)
								RadioOptionRow(
									text =  stringResource(Res.string.frequency_daily),
									selected = tickFrequency.value == TickFrequency.DAILY,
									onClick = { tickFrequency.value = TickFrequency.DAILY }
								)
							}

							TextField(
								value = goal.value,
								onValueChange = { goal.value = it },
								label =  stringResource(Res.string.goal_label),
								singleLine = false
							)

							Row(
								modifier = Modifier.fillMaxWidth(),
								verticalAlignment = Alignment.CenterVertically
							) {
								Text( stringResource(Res.string.follow_progress))
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
							text = if (loading)  stringResource(Res.string.saving) else  stringResource(Res.string.save),
							enabled = !loading,
							onClick = {
								val u = user
								if (u != null) {
									scope.launch {
										val ok = sessionViewModel.updateProfileUserInfoTics(buildUpdate(u))
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
							text =  stringResource(Res.string.cancel),
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
					InfoSection(title =  stringResource(Res.string.account_info)) {
						InfoRow(icon = Icons.Default.Email, label =  stringResource(Res.string.email), value = user.email)
						InfoRow(icon = Icons.Default.Person, label =  stringResource(Res.string.username), value = user.username)
					}
				}
			}

			if (userInfo != null) {
				item {
					InfoSection(title =  stringResource(Res.string.personal_details)) {
						userInfo.age?.let {
							InfoRow(icon = Icons.Default.DateRange, label =  stringResource(Res.string.age), value = it.toString())
						}
						userInfo.stressLevel?.let {
							InfoRow(icon = Icons.Default.Warning, label =  stringResource(Res.string.stress_level), value = "$it/10")
						}
					}
				}

				item {
					InfoSection(title = stringResource(Res.string.condition_info)) {
						userInfo.tickType?.let {
							InfoRow(icon = Icons.Default.Info, label = stringResource(Res.string.tics_type), value = it.name)
						}
						userInfo.tickFrequency?.let {
							InfoRow(icon = Icons.Default.Refresh, label = stringResource(Res.string.tics_frequency), value = it.name)
						}
						userInfo.goal?.let {
							InfoRow(icon = Icons.Default.Star, label = stringResource(Res.string.goal), value = it)
						}
						userInfo.followProgress?.let {
							InfoRow(
								icon = Icons.Default.CheckCircle,
								label = stringResource(Res.string.following_progress),
								value = if (it) stringResource(Res.string.yes) else stringResource(Res.string.no)
							)
						}
					}
				}
			}
			item {
				InfoSection(title = stringResource(Res.string.support)) {
					SettingsRow(
						icon = Icons.Default.Help,
						label = stringResource(Res.string.help_support),
						onClick = onHelpSupportClick
					)
					SettingsRow(
						icon = Icons.Default.Lock,
						label = stringResource(Res.string.privacy_policy),
						onClick = { }
					)
					SettingsRow(
						icon = Icons.Default.Description,
						label = stringResource(Res.string.terms),
						onClick = { }
					)
				}
			}
			item {
				InfoSection(title = stringResource(Res.string.reminders)) {
					Row(
						modifier = Modifier.fillMaxWidth(),
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(stringResource(Res.string.morning_evening))
						Spacer(Modifier.weight(1f))
						Switch(
							checked = remindersEnabled,
							onCheckedChange = { enabled ->
								remindersEnabled = enabled
								appSettings.setRemindersEnabled(enabled)

								if (enabled) reminderManager.enableMorningAndEvening()
								else reminderManager.disableMorningAndEvening()
							}
						)
					}
				}
				InfoSection(title = stringResource(Res.string.language_settings)) {

					PrimaryButton(
						text = stringResource(Res.string.language_english),
						onClick = { customAppLocale = "en" }
					)

					PrimaryButton(
						text = stringResource(Res.string.language_spanish),
						onClick = { customAppLocale = "es" }
					)

					PrimaryButton(
						text = stringResource(Res.string.use_system_language),
						onClick = { customAppLocale = null }
					)
				}
			}


			item {
				Spacer(modifier = Modifier.height(8.dp))
				PrimaryButton(
					text = stringResource(Res.string.logout),
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
fun ProfileHeader(user: UserJoined?, userInfo: UserInfoTicsJoined?) {
	val displayName = userInfo?.preferredName
		?: user?.username
		?: stringResource(Res.string.default_user)
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
@Composable
fun SettingsRow(
	icon: ImageVector,
	label: String,
	onClick: () -> Unit
) {
	Surface(
		onClick = onClick,
		color = androidx.compose.ui.graphics.Color.Transparent
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = 4.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				modifier = Modifier.size(20.dp),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Spacer(modifier = Modifier.width(12.dp))
			Text(
				text = label,
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = FontWeight.Medium
			)
			Spacer(modifier = Modifier.weight(1f))
			Icon(
				imageVector = Icons.Default.KeyboardArrowRight,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}