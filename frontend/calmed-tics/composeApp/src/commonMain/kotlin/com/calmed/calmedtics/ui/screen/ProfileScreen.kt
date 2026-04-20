package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
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
import com.calmed.calmedtics.following_progress
import com.calmed.calmedtics.frequency_daily
import com.calmed.calmedtics.frequency_moderate
import com.calmed.calmedtics.frequency_rare
import com.calmed.calmedtics.goal
import com.calmed.calmedtics.goal_label
import com.calmed.calmedtics.help_support
import com.calmed.calmedtics.language_settings
import com.calmed.calmedtics.loading
import com.calmed.calmedtics.localization.customAppLocale
import com.calmed.calmedtics.logout
import com.calmed.calmedtics.model.dto.request.UserInfoTicsUpdateDto
import com.calmed.calmedtics.model.joined.UserInfoTicsJoined
import com.calmed.calmedtics.model.joined.UserJoined
import com.calmed.calmedtics.model.raw.TickFrequency
import com.calmed.calmedtics.model.raw.TickType
import com.calmed.calmedtics.morning_evening
import com.calmed.calmedtics.morning_reminder_label
import com.calmed.calmedtics.evening_reminder_label
import com.calmed.calmedtics.no
import com.calmed.calmedtics.personal_details
import com.calmed.calmedtics.preferred_name
import com.calmed.calmedtics.privacy_policy
import com.calmed.calmedtics.profile_error
import com.calmed.calmedtics.reminders
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
import com.calmed.calmedtics.tics_vocal
import com.calmed.calmedtics.ui.component.LanguageToggle
import com.calmed.calmedtics.ui.component.PrimaryButton
import com.calmed.calmedtics.ui.component.TextField
import com.calmed.calmedtics.ui.component.TimeSlider
import com.calmed.calmedtics.username
import com.calmed.calmedtics.yes
import com.calmed.calmedtics.viewmodel.SessionViewModel
import com.calmed.calmedtics.reminders.ReminderManager
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

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
	val reminderManager = remember { ReminderManager() }

	var remindersEnabled by remember { mutableStateOf(appSettings.isRemindersEnabled()) }

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

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(
				brush = Brush.verticalGradient(
					colors = listOf(
						Color(0xFF7B7DE5),
						Color(0xFFE5C8E8)
					)
				)
			)
	) {
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.statusBarsPadding()
				.navigationBarsPadding()
				.padding(horizontal = 16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp),
			contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
		) {
			item {
				ProfileHeader(user, userInfo)
			}

			if (user != null && userInfo == null) {
				item {
					InfoSection(title = stringResource(Res.string.personal_details)) {
						Text(
							text = stringResource(Res.string.profile_error),
							color = Color.White
						)

						if (error != null) {
							Text(
								text = stringResource(Res.string.error_prefix, error ?: ""),
								color = Color.White.copy(alpha = 0.85f)
							)
						}

						PrimaryButton(
							text = if (loading) {
								stringResource(Res.string.loading)
							} else {
								stringResource(Res.string.retry)
							},
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

							Text(
								text = stringResource(Res.string.age),
								color = Color.White
							)

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
								label = "",
								singleLine = true
							)

							Slider(
								value = age.intValue.toFloat(),
								onValueChange = { age.intValue = it.toInt().coerceIn(5, 80) },
								valueRange = 5f..80f,
								steps = 74,
								colors = SliderDefaults.colors(
									thumbColor = Color.White,
									activeTrackColor = Color.White,
									inactiveTrackColor = Color.White.copy(alpha = 0.45f)
								)
							)

							Text(
								text = stringResource(Res.string.stress_value, stress.intValue),
								color = Color.White
							)

							Slider(
								value = stress.intValue.toFloat(),
								onValueChange = { stress.intValue = it.toInt() },
								valueRange = 0f..10f,
								steps = 9,
								colors = SliderDefaults.colors(
									thumbColor = Color.White,
									activeTrackColor = Color.White,
									inactiveTrackColor = Color.White.copy(alpha = 0.45f)
								)
							)
						}
					}

					item {
						InfoSection(title = stringResource(Res.string.edit_condition_info)) {
							Text(
								text = stringResource(Res.string.tics_type),
								color = Color.White
							)

							Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
								RadioOptionRow(
									text = stringResource(Res.string.tics_motor),
									selected = tickType.value == TickType.MOTOR,
									onClick = { tickType.value = TickType.MOTOR }
								)
								RadioOptionRow(
									text = stringResource(Res.string.tics_vocal),
									selected = tickType.value == TickType.VOCAL,
									onClick = { tickType.value = TickType.VOCAL }
								)
								RadioOptionRow(
									text = stringResource(Res.string.tics_both),
									selected = tickType.value == TickType.BOTH,
									onClick = { tickType.value = TickType.BOTH }
								)
							}

							Text(
								text = stringResource(Res.string.tics_frequency),
								color = Color.White
							)

							Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
								RadioOptionRow(
									text = stringResource(Res.string.frequency_rare),
									selected = tickFrequency.value == TickFrequency.RARE,
									onClick = { tickFrequency.value = TickFrequency.RARE }
								)
								RadioOptionRow(
									text = stringResource(Res.string.frequency_moderate),
									selected = tickFrequency.value == TickFrequency.MODERATE,
									onClick = { tickFrequency.value = TickFrequency.MODERATE }
								)
								RadioOptionRow(
									text = stringResource(Res.string.frequency_daily),
									selected = tickFrequency.value == TickFrequency.DAILY,
									onClick = { tickFrequency.value = TickFrequency.DAILY }
								)
							}

							TextField(
								value = goal.value,
								onValueChange = { goal.value = it },
								label = stringResource(Res.string.goal_label),
								singleLine = false
							)

							Surface(
								modifier = Modifier.fillMaxWidth(),
								shape = RoundedCornerShape(20.dp),
								color = Color.White.copy(alpha = 0.14f),
								border = BorderStroke(
									width = 1.dp,
									color = Color.White.copy(alpha = 0.30f)
								)
							) {
								Row(
									modifier = Modifier
										.fillMaxWidth()
										.padding(horizontal = 16.dp, vertical = 16.dp),
									verticalAlignment = Alignment.CenterVertically
								) {
									Text(
										text = stringResource(Res.string.follow_progress),
										color = Color.White
									)
									Spacer(modifier = Modifier.weight(1f))
									Switch(
										checked = followProgress.value,
										onCheckedChange = { followProgress.value = it }
									)
								}
							}
						}
					}

					item {
						PrimaryButton(
							text = if (loading) {
								stringResource(Res.string.saving)
							} else {
								stringResource(Res.string.save)
							},
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
							text = stringResource(Res.string.cancel),
							enabled = !loading,
							onClick = { isEditing = false }
						)
					}
				}
			}

			if (user != null) {
				item {
					InfoSection(title = stringResource(Res.string.account_info)) {
						InfoRow(
							icon = Icons.Default.Email,
							label = stringResource(Res.string.email),
							value = user.email
						)
						InfoRow(
							icon = Icons.Default.Person,
							label = stringResource(Res.string.username),
							value = user.username
						)
					}
				}
			}

			if (userInfo != null) {
				item {
					InfoSection(title = stringResource(Res.string.personal_details)) {
						userInfo.age?.let {
							InfoRow(
								icon = Icons.Default.DateRange,
								label = stringResource(Res.string.age),
								value = it.toString()
							)
						}
						userInfo.stressLevel?.let {
							InfoRow(
								icon = Icons.Default.Warning,
								label = stringResource(Res.string.stress_level),
								value = "$it/10"
							)
						}
					}
				}

				item {
					InfoSection(title = stringResource(Res.string.condition_info)) {
						userInfo.tickType?.let {
							InfoRow(
								icon = Icons.Default.Info,
								label = stringResource(Res.string.tics_type),
								value = it.name
							)
						}
						userInfo.tickFrequency?.let {
							InfoRow(
								icon = Icons.Default.Refresh,
								label = stringResource(Res.string.tics_frequency),
								value = it.name
							)
						}
						userInfo.goal?.let {
							InfoRow(
								icon = Icons.Default.Star,
								label = stringResource(Res.string.goal),
								value = it
							)
						}
						userInfo.followProgress?.let {
							InfoRow(
								icon = Icons.Default.CheckCircle,
								label = stringResource(Res.string.following_progress),
								value = if (it) {
									stringResource(Res.string.yes)
								} else {
									stringResource(Res.string.no)
								}
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
					var morningTime by remember { mutableStateOf(appSettings.getMorningReminderTime()) }
					var eveningTime by remember { mutableStateOf(appSettings.getEveningReminderTime()) }

					Surface(
						modifier = Modifier.fillMaxWidth(),
						shape = RoundedCornerShape(20.dp),
						color = Color.White.copy(alpha = 0.14f),
						border = BorderStroke(
							width = 1.dp,
							color = Color.White.copy(alpha = 0.30f)
						)
					) {
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.padding(horizontal = 16.dp, vertical = 16.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							Text(
								text = stringResource(Res.string.morning_evening),
								color = Color.White
							)
							Spacer(Modifier.weight(1f))
							Switch(
								checked = remindersEnabled,
								onCheckedChange = { enabled ->
									remindersEnabled = enabled
									appSettings.setRemindersEnabled(enabled)

									if (enabled) {
										reminderManager.enableMorningAndEvening()
									} else {
										reminderManager.disableMorningAndEvening()
									}
								}
							)
						}
					}

					if (remindersEnabled) {
						Spacer(modifier = Modifier.height(8.dp))

						TimeSlider(
							label = stringResource(Res.string.morning_reminder_label),
							initialTime = morningTime,
							onTimeSelected = {
								morningTime = it
								appSettings.setMorningReminderTime(it)
								reminderManager.enableMorningAndEvening()
							}
						)

						Spacer(modifier = Modifier.height(8.dp))

						TimeSlider(
							label = stringResource(Res.string.evening_reminder_label),
							initialTime = eveningTime,
							onTimeSelected = {
								eveningTime = it
								appSettings.setEveningReminderTime(it)
								reminderManager.enableMorningAndEvening()
							}
						)
					}
				}
			}

			item {
				InfoSection(title = stringResource(Res.string.language_settings)) {
					var currentLanguage by remember { mutableStateOf(appSettings.getAppLanguage()) }

					LanguageToggle(
						selectedLanguage = currentLanguage,
						onLanguageSelected = { lang ->
							currentLanguage = lang
							customAppLocale = lang
							appSettings.setAppLanguage(lang)
						},
						modifier = Modifier.padding(top = 8.dp)
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
private fun RadioOptionRow(
	text: String,
	selected: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier.fillMaxWidth(),
		shape = RoundedCornerShape(20.dp),
		color = if (selected) {
			Color.White.copy(alpha = 0.28f)
		} else {
			Color.White.copy(alpha = 0.14f)
		},
		border = BorderStroke(
			width = if (selected) 2.dp else 1.dp,
			color = if (selected) {
				Color.White.copy(alpha = 0.95f)
			} else {
				Color.White.copy(alpha = 0.30f)
			}
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.selectable(
					selected = selected,
					onClick = onClick,
					role = Role.RadioButton
				)
				.padding(horizontal = 16.dp, vertical = 16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(
				modifier = Modifier
					.size(22.dp)
					.background(
						color = if (selected) Color.White else Color.Transparent,
						shape = CircleShape
					)
					.border(
						width = 2.dp,
						color = Color.White,
						shape = CircleShape
					)
			)

			Spacer(Modifier.width(12.dp))

			Text(
				text = text,
				style = MaterialTheme.typography.bodyLarge,
				color = Color.White
			)
		}
	}
}

@Composable
fun ProfileHeader(user: UserJoined?, userInfo: UserInfoTicsJoined?) {
	val displayName = userInfo?.preferredName
		?: user?.username
		?: stringResource(Res.string.default_user)

	val email = user?.email ?: ""

	Surface(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(28.dp),
		color = Color.White.copy(alpha = 0.18f),
		border = BorderStroke(
			1.dp,
			Color.White.copy(alpha = 0.28f)
		)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 20.dp, vertical = 28.dp),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Box(
				modifier = Modifier
					.size(96.dp)
					.clip(CircleShape)
					.background(Color.White.copy(alpha = 0.22f)),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = Icons.Default.Person,
					contentDescription = null,
					modifier = Modifier.size(52.dp),
					tint = Color.White
				)
			}

			Spacer(modifier = Modifier.height(16.dp))

			Text(
				text = displayName,
				style = MaterialTheme.typography.headlineSmall,
				fontWeight = FontWeight.Bold,
				color = Color.White
			)

			if (email.isNotEmpty()) {
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = email,
					style = MaterialTheme.typography.bodyMedium,
					color = Color.White.copy(alpha = 0.82f)
				)
			}
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
			color = Color.White,
			modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
		)

		Surface(
			modifier = Modifier.fillMaxWidth(),
			shape = RoundedCornerShape(24.dp),
			color = Color.White.copy(alpha = 0.16f),
			border = BorderStroke(
				1.dp,
				Color.White.copy(alpha = 0.24f)
			)
		) {
			Column(
				modifier = Modifier.padding(18.dp),
				verticalArrangement = Arrangement.spacedBy(14.dp)
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
		verticalAlignment = Alignment.Top
	) {
		Box(
			modifier = Modifier
				.size(36.dp)
				.clip(CircleShape)
				.background(Color.White.copy(alpha = 0.16f)),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				modifier = Modifier.size(18.dp),
				tint = Color.White
			)
		}

		Spacer(modifier = Modifier.width(12.dp))

		Column {
			Text(
				text = label,
				style = MaterialTheme.typography.labelMedium,
				color = Color.White.copy(alpha = 0.72f)
			)

			Spacer(modifier = Modifier.height(2.dp))

			Text(
				text = value,
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = FontWeight.Medium,
				color = Color.White
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
		color = Color.Transparent
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = 6.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(
				modifier = Modifier
					.size(36.dp)
					.clip(CircleShape)
					.background(Color.White.copy(alpha = 0.16f)),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					modifier = Modifier.size(18.dp),
					tint = Color.White
				)
			}

			Spacer(modifier = Modifier.width(12.dp))

			Text(
				text = label,
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = FontWeight.Medium,
				color = Color.White
			)

			Spacer(modifier = Modifier.weight(1f))

			Icon(
				imageVector = Icons.Default.KeyboardArrowRight,
				contentDescription = null,
				tint = Color.White.copy(alpha = 0.75f)
			)
		}
	}
}