package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.calmed.calmedtics.model.dto.request.UserInfoTicsUpdateDto
import com.calmed.calmedtics.model.joined.UserInfoTicsJoined
import com.calmed.calmedtics.model.joined.UserJoined
import com.calmed.calmedtics.model.raw.TickFrequency
import com.calmed.calmedtics.model.raw.TickType
import com.calmed.calmedtics.ui.component.PrimaryButton
import com.calmed.calmedtics.ui.component.ScreenScaffold
import com.calmed.calmedtics.ui.component.TextField

@Composable
fun OnboardingScreen(
	user: UserJoined,
	userInfo: UserInfoTicsJoined,
	onSkip: () -> Unit,
	onFinished: (UserInfoTicsUpdateDto) -> Unit
) {
	val step = remember { mutableIntStateOf(0) }
	val preferredName = remember { mutableStateOf(userInfo.preferredName ?: "") }
	val age = remember { mutableIntStateOf(userInfo.age ?: 18) }
	val stress = remember { mutableIntStateOf(userInfo.stressLevel ?: 5) }
	val tickType = remember { mutableStateOf(userInfo.tickType ?: TickType.BOTH) }
	val tickFrequency = remember { mutableStateOf(userInfo.tickFrequency ?: TickFrequency.MODERATE) }
	val goal = remember { mutableStateOf(userInfo.goal ?: "") }
	val followProgress = remember { mutableStateOf(userInfo.followProgress ?: true) }
	val ageText = remember { mutableStateOf(age.intValue.toString()) }

	LaunchedEffect(age.intValue) {
		ageText.value = age.intValue.toString()
	}

	fun buildUpdate(): UserInfoTicsUpdateDto {
		return UserInfoTicsUpdateDto(
			userId = user.id,
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
						role = Role.RadioButton
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

	ScreenScaffold(
		title = "Onboarding",
		onBack = if (step.intValue > 0) ({ step.intValue -= 1 }) else null
	) {
		Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
			TextButton(onClick = onSkip) {
				Text("Skip")
			}
		}

		Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
			when (step.intValue) {
				0 -> {
					Text("Welcome ${user.username}", style = MaterialTheme.typography.headlineSmall)
					Text("Let’s personalize your experience.")
					PrimaryButton(text = "Start", onClick = { step.intValue = 1 })
				}

				1 -> {
					Text("Preferred name", style = MaterialTheme.typography.titleLarge)
					TextField(
						value = preferredName.value,
						onValueChange = { preferredName.value = it },
						label = "Preferred name",
						singleLine = true
					)
					PrimaryButton(text = "Next", onClick = { step.intValue = 2 })
				}

				2 -> {
					Text("Age", style = MaterialTheme.typography.titleLarge)
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
					PrimaryButton(text = "Next", onClick = { step.intValue = 3 })
				}

				3 -> {
					Text("Stress level", style = MaterialTheme.typography.titleLarge)
					Text("Stress: ${stress.intValue} / 10")
					Slider(
						value = stress.intValue.toFloat(),
						onValueChange = { stress.intValue = it.toInt() },
						valueRange = 0f..10f,
						steps = 9
					)
					PrimaryButton(text = "Next", onClick = { step.intValue = 4 })
				}

				4 -> {
					Text("Tics type", style = MaterialTheme.typography.titleLarge)

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

					PrimaryButton(text = "Next", onClick = { step.intValue = 5 })
				}

				5 -> {
					Text("Tics frequency", style = MaterialTheme.typography.titleLarge)

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

					PrimaryButton(text = "Next", onClick = { step.intValue = 6 })
				}

				6 -> {
					Text("Goal", style = MaterialTheme.typography.titleLarge)
					TextField(
						value = goal.value,
						onValueChange = { goal.value = it },
						label = "Your goal",
						singleLine = false
					)
					PrimaryButton(text = "Next", onClick = { step.intValue = 7 })
				}

				7 -> {
					Text("Follow progress", style = MaterialTheme.typography.titleLarge)
					Text("Would you like to follow your progress over time?")
					Switch(checked = followProgress.value, onCheckedChange = { followProgress.value = it })
					PrimaryButton(text = "Finish", onClick = { onFinished(buildUpdate()) })
				}
			}
		}
	}
}