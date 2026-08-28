package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.calmed.calmedtics.theme.appBackgroundGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calmed.calmedtics.*
import com.calmed.calmedtics.model.dto.request.UserInfoTicsUpdateDto
import com.calmed.calmedtics.model.joined.UserInfoTicsJoined
import com.calmed.calmedtics.model.joined.UserJoined
import com.calmed.calmedtics.model.raw.TickFrequency
import com.calmed.calmedtics.model.raw.TickType
import com.calmed.calmedtics.reminders.ReminderManager
import com.calmed.calmedtics.ui.component.PrimaryButton
import com.calmed.calmedtics.ui.component.TextField
import com.calmed.calmedtics.ui.component.ThumbnailImage
import com.calmed.calmedtics.ui.component.TimeSlider
import com.calmed.calmedtics.util.createImagePicker
import com.calmed.calmedtics.util.decodeImage
import com.calmed.calmedtics.viewmodel.SessionViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.account_info
import calmedtics.shared.generated.resources.age
import calmedtics.shared.generated.resources.cancel
import calmedtics.shared.generated.resources.condition_info
import calmedtics.shared.generated.resources.default_user
import calmedtics.shared.generated.resources.edit_condition_info
import calmedtics.shared.generated.resources.edit_personal_details
import calmedtics.shared.generated.resources.edit_profile
import calmedtics.shared.generated.resources.email
import calmedtics.shared.generated.resources.error_prefix
import calmedtics.shared.generated.resources.evening_reminder_label
import calmedtics.shared.generated.resources.frequency_daily
import calmedtics.shared.generated.resources.frequency_moderate
import calmedtics.shared.generated.resources.frequency_rare
import calmedtics.shared.generated.resources.goal
import calmedtics.shared.generated.resources.goal_label
import calmedtics.shared.generated.resources.help_support
import calmedtics.shared.generated.resources.loading
import calmedtics.shared.generated.resources.logout
import calmedtics.shared.generated.resources.morning_evening
import calmedtics.shared.generated.resources.morning_reminder_label
import calmedtics.shared.generated.resources.no
import calmedtics.shared.generated.resources.personal_details
import calmedtics.shared.generated.resources.preferred_name
import calmedtics.shared.generated.resources.privacy_policy
import calmedtics.shared.generated.resources.profile_error
import calmedtics.shared.generated.resources.reminders
import calmedtics.shared.generated.resources.retry
import calmedtics.shared.generated.resources.save
import calmedtics.shared.generated.resources.saving
import calmedtics.shared.generated.resources.stress_level
import calmedtics.shared.generated.resources.stress_value
import calmedtics.shared.generated.resources.support
import calmedtics.shared.generated.resources.terms
import calmedtics.shared.generated.resources.tics_both
import calmedtics.shared.generated.resources.tics_frequency
import calmedtics.shared.generated.resources.tics_motor
import calmedtics.shared.generated.resources.tics_type
import calmedtics.shared.generated.resources.tics_vocal
import calmedtics.shared.generated.resources.username
import calmedtics.shared.generated.resources.yes
import com.calmed.calmedtics.model.raw.TicDuration

@Composable
fun ProfileScreen(
	user: UserJoined?,
	userInfo: UserInfoTicsJoined?,
	onLogout: () -> Unit,
	onAccountDeleted: () -> Unit = {},
	onHelpSupportClick: () -> Unit = {},
	onPrivacyPolicyClick: () -> Unit = {},
	onTermsClick: () -> Unit = {},
	onRefundPolicyClick: () -> Unit = {},
	appSettings: com.calmed.calmedtics.settings.AppSettings = koinInject(),
	sessionViewModel: SessionViewModel = koinInject()
){
	val scope = rememberCoroutineScope()
	val reminderManager = remember { ReminderManager() }

	var remindersEnabled by remember { mutableStateOf(appSettings.isRemindersEnabled()) }

	val loading by sessionViewModel.loading.collectAsState()
	val error by sessionViewModel.error.collectAsState()

	var isEditing by remember { mutableStateOf(false) }
	var showDeleteDialog by remember { mutableStateOf(false) }
	var isDeleting by remember { mutableStateOf(false) }
	var deleteError by remember { mutableStateOf<String?>(null) }
	var deleteConfirmText by remember { mutableStateOf("") }
	val imagePicker = remember { createImagePicker() }

	var profileImageBytes by remember {
		mutableStateOf<ByteArray?>(null)
	}
	val profileImageBitmap =
		remember(profileImageBytes) {
			profileImageBytes?.let { decodeImage(it) }
		}
	val preferredName = remember { mutableStateOf(userInfo?.preferredName ?: "") }
	val age = remember { mutableIntStateOf(userInfo?.age ?: 18) }
	val stress = remember { mutableIntStateOf(userInfo?.stressLevel ?: 5) }
	val tickType = remember { mutableStateOf(userInfo?.tickType ?: TickType.BOTH) }
	val tickFrequency = remember { mutableStateOf(userInfo?.tickFrequency ?: TickFrequency.MODERATE) }
	val ticDuration = remember { mutableStateOf(userInfo?.ticDuration ?: TicDuration.ZERO_TO_ONE_YEAR) }
	val goal = remember { mutableStateOf(userInfo?.goal ?: "") }
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
		ticDuration.value = userInfo?.ticDuration ?: TicDuration.ZERO_TO_ONE_YEAR
		goal.value = userInfo?.goal ?: ""
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
			ticDuration = ticDuration.value,
			goal = goal.value.trim()
		)
	}

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(appBackgroundGradient())
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
							color = MaterialTheme.colorScheme.error
						)

						if (error != null) {
							Text(
								text = stringResource(Res.string.error_prefix, error ?: ""),
								color = MaterialTheme.colorScheme.onSurfaceVariant
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
							Column(
								modifier = Modifier.fillMaxWidth(),
								horizontalAlignment = Alignment.CenterHorizontally
							) {
								val profileImageUrl =
									user?.profileImageUrl?.let { url ->
										if (url.startsWith("http")) {
											url
										} else {
											"https://api.calm-ed.com$url"
										}
									}

								Box(
									modifier = Modifier
										.size(96.dp)
										.clip(CircleShape)
										.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f))
										.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape),
									contentAlignment = Alignment.Center
								) {
									if (profileImageBitmap != null) {
										Image(
											bitmap = profileImageBitmap,
											contentDescription = "Selected profile photo",
											modifier = Modifier
												.fillMaxSize()
												.clip(CircleShape)
										)
									} else {
										Icon(
											imageVector = Icons.Default.Person,
											contentDescription = null,
											modifier = Modifier.size(52.dp),
											tint = MaterialTheme.colorScheme.onSurface
										)
									}
								}

								Spacer(
									modifier = Modifier.height(10.dp)
								)

								Surface(
									onClick = {
										imagePicker.pickImage { bytes ->
											if (bytes != null) {
												profileImageBytes = bytes
											}
										}
									},
									shape = RoundedCornerShape(16.dp),
									color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
									border = BorderStroke(
										1.dp,
										MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
									)
								) {
									Text(
										text = "Choose profile photo",
										modifier = Modifier.padding(
											horizontal = 16.dp,
											vertical = 10.dp
										),
										color = MaterialTheme.colorScheme.onSurface,
										fontWeight = FontWeight.Medium
									)
								}
							}

							Spacer(
								modifier = Modifier.height(8.dp)
							)
							TextField(
								value = preferredName.value,
								onValueChange = { preferredName.value = it },
								label = stringResource(Res.string.preferred_name),
								singleLine = true
							)

							Text(
								text = stringResource(Res.string.age),
								color = MaterialTheme.colorScheme.onSurface
							)

							TextField(
								value = ageText.value,
								onValueChange = { raw ->
									val digitsOnly = raw.take(3).filter { it.isDigit() }
									ageText.value = digitsOnly
									val parsed = digitsOnly.toIntOrNull()
									if (parsed != null) {
										val coerced = parsed.coerceIn(18, 125)
										age.intValue = coerced
										if (parsed > 125 || (digitsOnly.length >= 2 && parsed < 18) || (digitsOnly.length == 1 && parsed == 0)) {
											ageText.value = coerced.toString()
										}
									}
								},
								label = "",
								singleLine = true
							)

							Slider(
								value = age.intValue.toFloat(),
								onValueChange = { age.intValue = it.toInt().coerceIn(18, 125) },
								valueRange = 18f..125f,
								steps = 62,
							)

							Text(
								text = stringResource(Res.string.stress_value, stress.intValue),
								color = MaterialTheme.colorScheme.onSurface
							)

							Slider(
								value = stress.intValue.toFloat(),
								onValueChange = { stress.intValue = it.toInt() },
								valueRange = 0f..10f,
								steps = 9,
							)
						}
					}

					item {
						InfoSection(title = stringResource(Res.string.edit_condition_info)) {
							Text(
								text = stringResource(Res.string.tics_type),
								color = MaterialTheme.colorScheme.onSurface
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
								color = MaterialTheme.colorScheme.onSurface
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

							Text(
								text = "How long have you had tics?",
								color = MaterialTheme.colorScheme.onSurface
							)

							Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
								RadioOptionRow(
									text = "0–1 year",
									selected = ticDuration.value == TicDuration.ZERO_TO_ONE_YEAR,
									onClick = { ticDuration.value = TicDuration.ZERO_TO_ONE_YEAR }
								)
								RadioOptionRow(
									text = "1–3 years",
									selected = ticDuration.value == TicDuration.ONE_TO_THREE_YEARS,
									onClick = { ticDuration.value = TicDuration.ONE_TO_THREE_YEARS }
								)
								RadioOptionRow(
									text = "3+ years",
									selected = ticDuration.value == TicDuration.THREE_PLUS_YEARS,
									onClick = { ticDuration.value = TicDuration.THREE_PLUS_YEARS }
								)
							}

							TextField(
								value = goal.value,
								onValueChange = { goal.value = it },
								label = stringResource(Res.string.goal_label),
								singleLine = false
							)
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

										val profileOk =
											sessionViewModel.updateProfileUserInfoTics(
												buildUpdate(u)
											)

										if (!profileOk) {
											return@launch
										}

										val imageOk =
											profileImageBytes?.let { bytes ->
												sessionViewModel.uploadProfileImage(bytes)
											} ?: true

										if (imageOk) {
											profileImageBytes = null
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
						userInfo.ticDuration?.let {
							InfoRow(
								icon = Icons.Default.DateRange,
								label = "How long have you had tics?",
								value = when (it) {
									TicDuration.ZERO_TO_ONE_YEAR -> "0–1 year"
									TicDuration.ONE_TO_THREE_YEARS -> "1–3 years"
									TicDuration.THREE_PLUS_YEARS -> "3+ years"
								}
							)
						}
					}
				}
			}

			item {
				InfoSection(title = stringResource(Res.string.support)) {
					SettingsRow(
						icon = Icons.AutoMirrored.Filled.Help,
						label = stringResource(Res.string.help_support),
						onClick = onHelpSupportClick
					)
					SettingsRow(
						icon = Icons.Default.Lock,
						label = stringResource(Res.string.privacy_policy),
						onClick = onPrivacyPolicyClick
					)

					SettingsRow(
						icon = Icons.Default.Description,
						label = stringResource(Res.string.terms),
						onClick = onTermsClick
					)

					SettingsRow(
						icon = Icons.Default.Refresh,
						label = "Refund Policy",
						onClick = onRefundPolicyClick
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
						color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
						border = BorderStroke(
							width = 1.dp,
							color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
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
								color = MaterialTheme.colorScheme.onSurface
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

			item {
				InfoSection(title = "Danger zone") {
					SettingsRow(
						icon = Icons.Default.Delete,
						label = "Delete Account",
						onClick = {
							deleteError = null
							deleteConfirmText = ""
							showDeleteDialog = true
						}
					)
					if (deleteError != null) {
						Text(
							text = deleteError ?: "",
							color = MaterialTheme.colorScheme.error
						)
					}
				}
			}
		}
	}

	if (showDeleteDialog) {
		AlertDialog(
			onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
			title = { Text("Delete Account") },
			text = {
				Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
					Text(
						"This will permanently delete your account and all of your " +
							"personal data. "
					)
					Text(
						"Purchases made through the App Store or Google Play can be " +
							"restored if you sign in again with the same account.",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
					Text(
						"This action cannot be undone.",
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.error
					)
					Spacer(modifier = Modifier.height(6.dp))
					TextField(
						value = deleteConfirmText,
						onValueChange = { deleteConfirmText = it },
						label = "Type DELETE to confirm",
						singleLine = true
					)
				}
			},
			confirmButton = {
				TextButton(
					enabled = !isDeleting && deleteConfirmText == "DELETE",
					onClick = {
						scope.launch {
							isDeleting = true
							deleteError = null
							val ok = sessionViewModel.deleteAccount()
							isDeleting = false
							if (ok) {
								showDeleteDialog = false
								onAccountDeleted()
							} else {
								deleteError = sessionViewModel.error.value ?: "Could not delete the account."
							}
						}
					}
				) {
					Text(if (isDeleting) "Deleting..." else "Delete")
				}
			},
			dismissButton = {
				TextButton(
					enabled = !isDeleting,
					onClick = { showDeleteDialog = false }
				) {
					Text("Cancel")
				}
			}
		)
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
			MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
		} else {
			MaterialTheme.colorScheme.surface.copy(alpha = 0.25f)
		},
		border = BorderStroke(
			width = if (selected) 2.dp else 1.dp,
			color = if (selected) {
				MaterialTheme.colorScheme.primary
			} else {
				MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
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
						color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
						shape = CircleShape
					)
					.border(
						width = 2.dp,
						color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
						shape = CircleShape
					)
			)

			Spacer(Modifier.width(12.dp))

			Text(
				text = text,
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface
			)
		}
	}
}

@Composable
fun ProfileHeader(
	user: UserJoined?,
	userInfo: UserInfoTicsJoined?
) {
	val displayName = userInfo?.preferredName
		?: user?.username
		?: stringResource(Res.string.default_user)

	val email = user?.email ?: ""

	val profileImageUrl =
		user?.profileImageUrl?.let { url ->
			if (url.startsWith("http")) {
				url
			} else {
				"https://api.calm-ed.com$url"
			}
		}

	Surface(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(28.dp),
		color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
		border = BorderStroke(
			1.dp,
			MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
		)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(
					horizontal = 20.dp,
					vertical = 28.dp
				),
			horizontalAlignment = Alignment.CenterHorizontally
		) {

			Box(
				modifier = Modifier
					.size(96.dp)
					.clip(CircleShape)
					.background(
						MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
					)
					.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape),
				contentAlignment = Alignment.Center
			) {

				if (!profileImageUrl.isNullOrBlank()) {

					ThumbnailImage(
						url = profileImageUrl,
						contentDescription = "Profile photo",
						modifier = Modifier
							.fillMaxSize()
							.clip(CircleShape)
					)

				} else {

					Icon(
						imageVector = Icons.Default.Person,
						contentDescription = null,
						modifier = Modifier.size(52.dp),
						tint = MaterialTheme.colorScheme.onSurface
					)
				}
			}

			Spacer(
				modifier = Modifier.height(16.dp)
			)

			Text(
				text = displayName,
				style = MaterialTheme.typography.headlineSmall,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onSurface
			)

			if (email.isNotEmpty()) {
				Spacer(
					modifier = Modifier.height(4.dp)
				)

				Text(
					text = email,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
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
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
		)

		Surface(
			modifier = Modifier.fillMaxWidth(),
			shape = RoundedCornerShape(24.dp),
			color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
			border = BorderStroke(
				1.dp,
				MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
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
				.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f))
				.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				modifier = Modifier.size(18.dp),
				tint = MaterialTheme.colorScheme.onSurface
			)
		}

		Spacer(modifier = Modifier.width(12.dp))

		Column {
			Text(
				text = label,
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)

			Spacer(modifier = Modifier.height(2.dp))

			Text(
				text = value,
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = FontWeight.Medium,
				color = MaterialTheme.colorScheme.onSurface
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
					.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f))
					.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					modifier = Modifier.size(18.dp),
					tint = MaterialTheme.colorScheme.onSurface
				)
			}

			Spacer(modifier = Modifier.width(12.dp))

			Text(
				text = label,
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = FontWeight.Medium,
				color = MaterialTheme.colorScheme.onSurface
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