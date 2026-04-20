package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.calmed.calmedtics.Res
import com.calmed.calmedtics.age_title
import com.calmed.calmedtics.back
import com.calmed.calmedtics.can_change_later
import com.calmed.calmedtics.finish
import com.calmed.calmedtics.follow_progress_question
import com.calmed.calmedtics.follow_progress_title
import com.calmed.calmedtics.frequency_daily
import com.calmed.calmedtics.frequency_moderate
import com.calmed.calmedtics.frequency_rare
import com.calmed.calmedtics.goal_example
import com.calmed.calmedtics.goal_input_label
import com.calmed.calmedtics.goal_question
import com.calmed.calmedtics.goal_title
import com.calmed.calmedtics.model.dto.request.UserInfoTicsUpdateDto
import com.calmed.calmedtics.model.joined.UserInfoTicsJoined
import com.calmed.calmedtics.model.joined.UserJoined
import com.calmed.calmedtics.model.raw.TickFrequency
import com.calmed.calmedtics.model.raw.TickType
import com.calmed.calmedtics.next
import com.calmed.calmedtics.onboarding_questions_intro
import com.calmed.calmedtics.personalize_experience
import com.calmed.calmedtics.preferred_name_description
import com.calmed.calmedtics.preferred_name_title
import com.calmed.calmedtics.skip
import com.calmed.calmedtics.start
import com.calmed.calmedtics.tailor_app_experience
import com.calmed.calmedtics.tics_both
import com.calmed.calmedtics.tics_both_description
import com.calmed.calmedtics.tics_frequency_daily_description
import com.calmed.calmedtics.tics_frequency_moderate_description
import com.calmed.calmedtics.stress_level_question
import com.calmed.calmedtics.tics_frequency_question
import com.calmed.calmedtics.tics_frequency_rare_description
import com.calmed.calmedtics.tics_frequency_title
import com.calmed.calmedtics.tics_motor
import com.calmed.calmedtics.tics_motor_description
import com.calmed.calmedtics.tics_type_title
import com.calmed.calmedtics.tics_vocal
import com.calmed.calmedtics.tics_vocal_description
import com.calmed.calmedtics.track_progress_description
import com.calmed.calmedtics.track_progress_option
import com.calmed.calmedtics.ui.component.PrimaryButton
import com.calmed.calmedtics.ui.component.TextField
import com.calmed.calmedtics.welcome_user
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material.icons.outlined.SentimentVeryDissatisfied

@OptIn(ExperimentalMaterial3Api::class)
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
	val tickFrequency =
		remember { mutableStateOf(userInfo.tickFrequency ?: TickFrequency.MODERATE) }
	val goal = remember { mutableStateOf(userInfo.goal ?: "") }
	val followProgress = remember { mutableStateOf(userInfo.followProgress ?: true) }

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
			color = if (selected) {
				MaterialTheme.colorScheme.primaryContainer
			} else {
				MaterialTheme.colorScheme.surface
			},
			border = BorderStroke(
				width = 1.dp,
				color = if (selected) {
					MaterialTheme.colorScheme.primary
				} else {
					MaterialTheme.colorScheme.outline
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
					.padding(horizontal = 12.dp, vertical = 12.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				RadioButton(selected = selected, onClick = null)
				Spacer(Modifier.width(10.dp))
				Text(text = text, style = MaterialTheme.typography.bodyLarge)
			}
		}
	}

	Box(modifier = Modifier.fillMaxSize()) {
		when (step.intValue) {
			0 -> {
				Column(
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
						.padding(24.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.SpaceBetween
				) {
					Column(
						modifier = Modifier.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.End
						) {
							Surface(
								shape = MaterialTheme.shapes.large,
								color = Color.White.copy(alpha = 0.28f)
							) {
								TextButton(onClick = onSkip) {
									Text(
										text = stringResource(Res.string.skip),
										color = Color.White
									)
								}
							}
						}

						Spacer(modifier = Modifier.height(48.dp))

						Text(
							text = stringResource(Res.string.welcome_user, user.username),
							style = MaterialTheme.typography.headlineMedium.copy(
								color = Color.White
							)
						)

						Spacer(modifier = Modifier.height(16.dp))

						Text(
							text = stringResource(Res.string.personalize_experience),
							style = MaterialTheme.typography.bodyLarge.copy(
								color = Color.White.copy(alpha = 0.9f)
							)
						)

						Spacer(modifier = Modifier.height(48.dp))

						Surface(
							shape = MaterialTheme.shapes.extraLarge,
							color = Color.White.copy(alpha = 0.22f)
						) {
							Text(
								text = stringResource(Res.string.onboarding_questions_intro),
								modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
								style = MaterialTheme.typography.bodyMedium.copy(
									color = Color.White
								)
							)
						}
					}

					Column(
						modifier = Modifier.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						Row(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							repeat(5) { index ->
								Surface(
									modifier = Modifier
										.width(if (index == 0) 18.dp else 8.dp)
										.height(8.dp),
									shape = MaterialTheme.shapes.large,
									color = if (index == 0) {
										Color(0xFF2F327D)
									} else {
										Color.White.copy(alpha = 0.5f)
									}
								) {}
							}
						}

						Spacer(modifier = Modifier.height(24.dp))

						Surface(
							modifier = Modifier.fillMaxWidth(),
							shape = MaterialTheme.shapes.extraLarge,
							color = Color.White.copy(alpha = 0.28f)
						) {
							TextButton(
								onClick = { step.intValue = 1 },
								modifier = Modifier.fillMaxWidth()
							) {
								Text(
									text = stringResource(Res.string.start),
									style = MaterialTheme.typography.titleMedium,
									color = Color.White
								)
							}
						}
					}
				}
			}

			1 -> {
				Column(
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
						.padding(24.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.SpaceBetween
				) {
					Column(
						modifier = Modifier.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween,
							verticalAlignment = Alignment.CenterVertically
						) {
							TextButton(
								onClick = { step.intValue = 0 }
							) {
								Text(
									text = "Back",
									color = Color.White
								)
							}

							Surface(
								shape = MaterialTheme.shapes.large,
								color = Color.White.copy(alpha = 0.28f)
							) {
								TextButton(onClick = onSkip) {
									Text(
										text = stringResource(Res.string.skip),
										color = Color.White
									)
								}
							}
						}

						Spacer(modifier = Modifier.height(48.dp))

						Text(
							text = stringResource(Res.string.preferred_name_title),
							style = MaterialTheme.typography.headlineMedium.copy(
								color = Color.White
							)
						)

						Spacer(modifier = Modifier.height(12.dp))

						Text(
							text = stringResource(Res.string.preferred_name_description),
							style = MaterialTheme.typography.bodyLarge.copy(
								color = Color.White.copy(alpha = 0.9f)
							)
						)

						Spacer(modifier = Modifier.height(40.dp))

						Surface(
							modifier = Modifier.fillMaxWidth(),
							shape = MaterialTheme.shapes.extraLarge,
							color = Color.White.copy(alpha = 0.22f)
						) {
							Column(
								modifier = Modifier.padding(20.dp)
							) {


								TextField(
									value = preferredName.value,
									onValueChange = { preferredName.value = it },
									label = "",
									singleLine = true
								)
							}
						}
					}

					Column(
						modifier = Modifier.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						Row(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							repeat(5) { index ->
								Surface(
									modifier = Modifier
										.width(if (index == 1) 18.dp else 8.dp)
										.height(8.dp),
									shape = MaterialTheme.shapes.large,
									color = if (index == 1) {
										Color(0xFF2F327D)
									} else {
										Color.White.copy(alpha = 0.5f)
									}
								) {}
							}
						}

						Spacer(modifier = Modifier.height(24.dp))

						Surface(
							modifier = Modifier.fillMaxWidth(),
							shape = MaterialTheme.shapes.extraLarge,
							color = Color.White.copy(alpha = 0.28f)
						) {
							TextButton(
								onClick = { step.intValue = 2 },
								modifier = Modifier.fillMaxWidth()
							) {
								Text(
									text = stringResource(Res.string.next),
									style = MaterialTheme.typography.titleMedium,
									color = Color.White
								)
							}
						}
					}
				}
			}

			2 -> {
				Column(
					modifier = Modifier
						.fillMaxSize()
						.background(
							brush = Brush.verticalGradient(
								colors = listOf(
									Color(0xFF7B7DE5),
									Color(0xFFE5C6E8)
								)
							)
						)
						.padding(24.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.SpaceBetween
				) {
					Column(
						modifier = Modifier.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween,
							verticalAlignment = Alignment.CenterVertically
						) {
							TextButton(
								onClick = { step.intValue = 1 }
							) {
								Text(
									text = stringResource(Res.string.back),
									color = Color.White
								)
							}

							Surface(
								shape = MaterialTheme.shapes.large,
								color = Color.White.copy(alpha = 0.28f)
							) {
								TextButton(
									onClick = { step.intValue = 3 }
								) {
									Text(
										text = stringResource(Res.string.next),
										color = Color.White
									)
								}
							}
						}

						Spacer(modifier = Modifier.height(24.dp))

						Text(
							text = stringResource(Res.string.age_title),
							style = MaterialTheme.typography.titleLarge.copy(
								color = Color(0xFF2F327D)
							)
						)

						Spacer(modifier = Modifier.height(40.dp))

						Box(
							modifier = Modifier.height(180.dp),
							contentAlignment = Alignment.Center
						) {
							val ages = (5..80).toList()
							val initialIndex = (age.intValue - 6).coerceIn(0, ages.lastIndex)
							val listState = rememberLazyListState(
								initialFirstVisibleItemIndex = initialIndex
							)

							LaunchedEffect(listState) {
								snapshotFlow { listState.firstVisibleItemIndex }
									.collect { index ->
										val middleIndex = (index + 1).coerceIn(0, ages.lastIndex)
										age.intValue = ages[middleIndex]
									}
							}

							Surface(
								modifier = Modifier
									.fillMaxWidth(0.45f)
									.height(64.dp),
								shape = MaterialTheme.shapes.large,
								color = Color.White.copy(alpha = 0.35f)
							) {}

							LazyColumn(
								state = listState,
								modifier = Modifier.height(180.dp),
								horizontalAlignment = Alignment.CenterHorizontally
							) {
								items(ages.size) { index ->
									val value = ages[index]
									val selected = value == age.intValue

									Box(
										modifier = Modifier.height(60.dp),
										contentAlignment = Alignment.Center
									) {
										Text(
											text = value.toString(),
											style = if (selected) {
												MaterialTheme.typography.displayLarge.copy(
													color = Color(0xFF2F327D)
												)
											} else {
												MaterialTheme.typography.headlineMedium.copy(
													color = Color(0xFF8C8FEF)
												)
											}
										)
									}
								}
							}
						}
					}

					Column(
						modifier = Modifier.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						Row(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							repeat(5) { index ->
								Surface(
									modifier = Modifier
										.width(if (index == 1) 18.dp else 8.dp)
										.height(8.dp),
									shape = MaterialTheme.shapes.large,
									color = if (index == 1) {
										Color(0xFF2F327D)
									} else {
										Color.White.copy(alpha = 0.5f)
									}
								) {}
							}
						}

						Spacer(modifier = Modifier.height(24.dp))

						Surface(
							modifier = Modifier.fillMaxWidth(),
							shape = MaterialTheme.shapes.extraLarge,
							color = Color.White.copy(alpha = 0.22f)
						) {
							Text(
								text = stringResource(Res.string.tailor_app_experience),
								modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
								style = MaterialTheme.typography.bodyMedium.copy(
									color = Color.White
								)
							)
						}
					}
				}
			}

			3 -> {
				Column(
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
						.padding(24.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.SpaceBetween
				) {

					Column(
						modifier = Modifier.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally
					) {

						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween,
							verticalAlignment = Alignment.CenterVertically
						) {
							TextButton(onClick = { step.intValue = 2 }) {
								Text(
									text = stringResource(Res.string.back),
									color = Color.White
								)
							}

							Surface(
								shape = MaterialTheme.shapes.large,
								color = Color.White.copy(alpha = 0.28f)
							) {
								TextButton(onClick = { step.intValue = 4 }) {
									Text(
										text = stringResource(Res.string.next),
										color = Color.White
									)
								}
							}
						}

						Spacer(modifier = Modifier.height(48.dp))

						Text(
							text = stringResource(Res.string.stress_level_question),
							style = MaterialTheme.typography.headlineMedium.copy(
								color = Color(0xFF2F327D)
							)
						)

						Spacer(modifier = Modifier.height(56.dp))

						Column(
							modifier = Modifier.fillMaxWidth(),
							horizontalAlignment = Alignment.CenterHorizontally
						) {

							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.SpaceBetween,
								verticalAlignment = Alignment.CenterVertically
							) {
								Icon(
									imageVector = Icons.Outlined.SentimentSatisfiedAlt,
									contentDescription = null,
									tint = Color(0xFF8C8FEF).copy(alpha = 0.85f),
									modifier = Modifier.size(28.dp)
								)

								Icon(
									imageVector = Icons.Outlined.SentimentVeryDissatisfied,
									contentDescription = null,
									tint = Color(0xFF8C8FEF).copy(alpha = 0.85f),
									modifier = Modifier.size(28.dp)
								)
							}

							Spacer(modifier = Modifier.height(14.dp))

							Box(
								modifier = Modifier
									.fillMaxWidth()
									.padding(horizontal = 6.dp)
							) {
								Box(
									modifier = Modifier
										.fillMaxWidth()
										.height(4.dp)
										.align(Alignment.Center)
										.background(
											color = Color.White.copy(alpha = 0.75f),
											shape = CircleShape
										)
								)

								Row(
									modifier = Modifier.fillMaxWidth(),
									horizontalArrangement = Arrangement.SpaceBetween,
									verticalAlignment = Alignment.CenterVertically
								) {
									(0..10).forEach { value ->
										val selected = stress.intValue == value

										Box(
											modifier = Modifier
												.size(if (selected) 22.dp else 14.dp)
												.shadow(
													elevation = if (selected) 12.dp else 0.dp,
													shape = CircleShape,
													ambientColor = Color(0xFF8D83FF),
													spotColor = Color(0xFF8D83FF)
												)
												.background(
													color = if (selected) Color.White else Color(
														0xFFE8E4FF
													),
													shape = CircleShape
												)
												.border(
													width = if (selected) 4.dp else 2.dp,
													color = Color(0xFF7E78E8),
													shape = CircleShape
												)
												.clickable { stress.intValue = value }
										)
									}
								}
							}

							Spacer(modifier = Modifier.height(12.dp))

							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.SpaceBetween
							) {
								(0..10).forEach { value ->
									Text(
										text = value.toString(),
										style = MaterialTheme.typography.bodySmall.copy(
											color = Color(0xFF2F327D)
										)
									)
								}
							}
						}
					}

					Column(
						modifier = Modifier.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally
					) {

						Row(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							repeat(5) { index ->
								Surface(
									modifier = Modifier
										.width(if (index == 2) 18.dp else 8.dp)
										.height(8.dp),
									shape = MaterialTheme.shapes.large,
									color = if (index == 2) {
										Color(0xFF2F327D)
									} else {
										Color.White.copy(alpha = 0.5f)
									}
								) {}
							}
						}

						Spacer(modifier = Modifier.height(24.dp))

						Surface(
							modifier = Modifier.fillMaxWidth(),
							shape = MaterialTheme.shapes.extraLarge,
							color = Color.White.copy(alpha = 0.22f)
						) {
							Text(
								text = stringResource(Res.string.tailor_app_experience),
								modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
								style = MaterialTheme.typography.bodyMedium.copy(
									color = Color.White
								)
							)
						}
					}
				}
			}

			4 -> {
				Column(
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
						.padding(24.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.SpaceBetween
				) {

					Column(
						modifier = Modifier.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally
					) {

						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween,
							verticalAlignment = Alignment.CenterVertically
						) {
							TextButton(onClick = { step.intValue = 3 }) {
								Text(
									text = stringResource(Res.string.back),
									color = Color.White
								)
							}

							Surface(
								shape = MaterialTheme.shapes.large,
								color = Color.White.copy(alpha = 0.28f)
							) {
								TextButton(onClick = { step.intValue = 5 }) {
									Text(
										text = stringResource(Res.string.next),
										color = Color.White
									)
								}
							}
						}

						Spacer(modifier = Modifier.height(40.dp))

						Text(
							text = stringResource(Res.string.tics_type_title),
							style = MaterialTheme.typography.headlineMedium.copy(
								color = Color(0xFF2F327D)
							)
						)

						Spacer(modifier = Modifier.height(32.dp))

						Column(
							modifier = Modifier.fillMaxWidth(),
							verticalArrangement = Arrangement.spacedBy(14.dp)
						) {
							Surface(
								modifier = Modifier
									.fillMaxWidth()
									.clickable { tickType.value = TickType.MOTOR },
								shape = MaterialTheme.shapes.extraLarge,
								color = if (tickType.value == TickType.MOTOR) {
									Color.White.copy(alpha = 0.30f)
								} else {
									Color.White.copy(alpha = 0.14f)
								},
								border = BorderStroke(
									width = if (tickType.value == TickType.MOTOR) 2.dp else 1.dp,
									color = if (tickType.value == TickType.MOTOR) {
										Color.White.copy(alpha = 0.95f)
									} else {
										Color.White.copy(alpha = 0.35f)
									}
								)
							) {
								Row(
									modifier = Modifier
										.fillMaxWidth()
										.padding(horizontal = 18.dp, vertical = 18.dp),
									verticalAlignment = Alignment.CenterVertically,
									horizontalArrangement = Arrangement.SpaceBetween
								) {
									Column {
										Text(
											text = stringResource(Res.string.tics_motor),
											style = MaterialTheme.typography.titleMedium.copy(
												color = Color.White
											)
										)
										Spacer(modifier = Modifier.height(4.dp))
										Text(
											text = stringResource(Res.string.tics_motor_description),
											style = MaterialTheme.typography.bodyMedium.copy(
												color = Color.White.copy(alpha = 0.8f)
											)
										)
									}

									Box(
										modifier = Modifier
											.size(24.dp)
											.background(
												color = if (tickType.value == TickType.MOTOR) {
													Color.White
												} else {
													Color.Transparent
												},
												shape = CircleShape
											)
											.border(
												width = 2.dp,
												color = Color.White,
												shape = CircleShape
											)
									)
								}
							}

							Surface(
								modifier = Modifier
									.fillMaxWidth()
									.clickable { tickType.value = TickType.VOCAL },
								shape = MaterialTheme.shapes.extraLarge,
								color = if (tickType.value == TickType.VOCAL) {
									Color.White.copy(alpha = 0.30f)
								} else {
									Color.White.copy(alpha = 0.14f)
								},
								border = BorderStroke(
									width = if (tickType.value == TickType.VOCAL) 2.dp else 1.dp,
									color = if (tickType.value == TickType.VOCAL) {
										Color.White.copy(alpha = 0.95f)
									} else {
										Color.White.copy(alpha = 0.35f)
									}
								)
							) {
								Row(
									modifier = Modifier
										.fillMaxWidth()
										.padding(horizontal = 18.dp, vertical = 18.dp),
									verticalAlignment = Alignment.CenterVertically,
									horizontalArrangement = Arrangement.SpaceBetween
								) {
									Column {
										Text(
											text = stringResource(Res.string.tics_vocal),
											style = MaterialTheme.typography.titleMedium.copy(
												color = Color.White
											)
										)
										Spacer(modifier = Modifier.height(4.dp))
										Text(
											text = stringResource(Res.string.tics_vocal_description),
											style = MaterialTheme.typography.bodyMedium.copy(
												color = Color.White.copy(alpha = 0.8f)
											)
										)
									}

									Box(
										modifier = Modifier
											.size(24.dp)
											.background(
												color = if (tickType.value == TickType.VOCAL) {
													Color.White
												} else {
													Color.Transparent
												},
												shape = CircleShape
											)
											.border(
												width = 2.dp,
												color = Color.White,
												shape = CircleShape
											)
									)
								}
							}

							Surface(
								modifier = Modifier
									.fillMaxWidth()
									.clickable { tickType.value = TickType.BOTH },
								shape = MaterialTheme.shapes.extraLarge,
								color = if (tickType.value == TickType.BOTH) {
									Color.White.copy(alpha = 0.30f)
								} else {
									Color.White.copy(alpha = 0.14f)
								},
								border = BorderStroke(
									width = if (tickType.value == TickType.BOTH) 2.dp else 1.dp,
									color = if (tickType.value == TickType.BOTH) {
										Color.White.copy(alpha = 0.95f)
									} else {
										Color.White.copy(alpha = 0.35f)
									}
								)
							) {
								Row(
									modifier = Modifier
										.fillMaxWidth()
										.padding(horizontal = 18.dp, vertical = 18.dp),
									verticalAlignment = Alignment.CenterVertically,
									horizontalArrangement = Arrangement.SpaceBetween
								) {
									Column {
										Text(
											text = stringResource(Res.string.tics_both),
											style = MaterialTheme.typography.titleMedium.copy(
												color = Color.White
											)
										)
										Spacer(modifier = Modifier.height(4.dp))
										Text(
											text = stringResource(Res.string.tics_both_description),
											style = MaterialTheme.typography.bodyMedium.copy(
												color = Color.White.copy(alpha = 0.8f)
											)
										)
									}

									Box(
										modifier = Modifier
											.size(24.dp)
											.background(
												color = if (tickType.value == TickType.BOTH) {
													Color.White
												} else {
													Color.Transparent
												},
												shape = CircleShape
											)
											.border(
												width = 2.dp,
												color = Color.White,
												shape = CircleShape
											)
									)
								}
							}
						}
					}


					Column(
						modifier = Modifier.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally
					) {

						Row(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							repeat(5) { index ->
								Surface(
									modifier = Modifier
										.width(if (index == 3) 18.dp else 8.dp)
										.height(8.dp),
									shape = MaterialTheme.shapes.large,
									color = if (index == 3) {
										Color(0xFF2F327D)
									} else {
										Color.White.copy(alpha = 0.5f)
									}
								) {}
							}
						}

						Spacer(modifier = Modifier.height(24.dp))

						Surface(
							modifier = Modifier.fillMaxWidth(),
							shape = MaterialTheme.shapes.extraLarge,
							color = Color.White.copy(alpha = 0.22f)
						) {
							Text(
								text = stringResource(Res.string.tailor_app_experience),
								modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
								style = MaterialTheme.typography.bodyMedium.copy(
									color = Color.White
								)
							)
						}
					}
				}
			}

			5 -> {
				Column(
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
						.padding(24.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.SpaceBetween
				) {

					// TOP
					Column(
						modifier = Modifier.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally
					) {

						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween,
							verticalAlignment = Alignment.CenterVertically
						) {
							TextButton(onClick = { step.intValue = 4 }) {
								Text(
									text = stringResource(Res.string.back),
									color = Color.White
								)
							}

							Surface(
								shape = MaterialTheme.shapes.large,
								color = Color.White.copy(alpha = 0.28f)
							) {
								TextButton(onClick = { step.intValue = 6 }) {
									Text(
										text = stringResource(Res.string.next),
										color = Color.White
									)
								}
							}
						}

						Spacer(modifier = Modifier.height(40.dp))

						Text(
							text = stringResource(Res.string.tics_frequency_title),
							style = MaterialTheme.typography.headlineMedium.copy(
								color = Color(0xFF2F327D)
							)
						)

						Spacer(modifier = Modifier.height(16.dp))

						Text(
							text = stringResource(Res.string.tics_frequency_question),
							style = MaterialTheme.typography.bodyLarge.copy(
								color = Color.White.copy(alpha = 0.9f)
							)
						)

						Spacer(modifier = Modifier.height(32.dp))

						Column(
							modifier = Modifier.fillMaxWidth(),
							verticalArrangement = Arrangement.spacedBy(14.dp)
						) {
							Surface(
								modifier = Modifier
									.fillMaxWidth()
									.clickable { tickFrequency.value = TickFrequency.RARE },
								shape = MaterialTheme.shapes.extraLarge,
								color = if (tickFrequency.value == TickFrequency.RARE) {
									Color.White.copy(alpha = 0.30f)
								} else {
									Color.White.copy(alpha = 0.14f)
								},
								border = BorderStroke(
									width = if (tickFrequency.value == TickFrequency.RARE) 2.dp else 1.dp,
									color = if (tickFrequency.value == TickFrequency.RARE) {
										Color.White.copy(alpha = 0.95f)
									} else {
										Color.White.copy(alpha = 0.35f)
									}
								)
							) {
								Row(
									modifier = Modifier
										.fillMaxWidth()
										.padding(horizontal = 18.dp, vertical = 18.dp),
									verticalAlignment = Alignment.CenterVertically,
									horizontalArrangement = Arrangement.SpaceBetween
								) {
									Column {
										Text(
											text = stringResource(Res.string.frequency_rare),
											style = MaterialTheme.typography.titleMedium.copy(
												color = Color.White
											)
										)
										Spacer(modifier = Modifier.height(4.dp))
										Text(
											text = stringResource(Res.string.tics_frequency_rare_description),
											style = MaterialTheme.typography.bodyMedium.copy(
												color = Color.White.copy(alpha = 0.8f)
											)
										)
									}

									Box(
										modifier = Modifier
											.size(24.dp)
											.background(
												color = if (tickFrequency.value == TickFrequency.RARE) {
													Color.White
												} else {
													Color.Transparent
												},
												shape = CircleShape
											)
											.border(
												width = 2.dp,
												color = Color.White,
												shape = CircleShape
											)
									)
								}
							}

							Surface(
								modifier = Modifier
									.fillMaxWidth()
									.clickable { tickFrequency.value = TickFrequency.MODERATE },
								shape = MaterialTheme.shapes.extraLarge,
								color = if (tickFrequency.value == TickFrequency.MODERATE) {
									Color.White.copy(alpha = 0.30f)
								} else {
									Color.White.copy(alpha = 0.14f)
								},
								border = BorderStroke(
									width = if (tickFrequency.value == TickFrequency.MODERATE) 2.dp else 1.dp,
									color = if (tickFrequency.value == TickFrequency.MODERATE) {
										Color.White.copy(alpha = 0.95f)
									} else {
										Color.White.copy(alpha = 0.35f)
									}
								)
							) {
								Row(
									modifier = Modifier
										.fillMaxWidth()
										.padding(horizontal = 18.dp, vertical = 18.dp),
									verticalAlignment = Alignment.CenterVertically,
									horizontalArrangement = Arrangement.SpaceBetween
								) {
									Column {
										Text(
											text = stringResource(Res.string.frequency_moderate),
											style = MaterialTheme.typography.titleMedium.copy(
												color = Color.White
											)
										)
										Spacer(modifier = Modifier.height(4.dp))
										Text(
											text = stringResource(Res.string.tics_frequency_moderate_description),
											style = MaterialTheme.typography.bodyMedium.copy(
												color = Color.White.copy(alpha = 0.8f)
											)
										)
									}

									Box(
										modifier = Modifier
											.size(24.dp)
											.background(
												color = if (tickFrequency.value == TickFrequency.MODERATE) {
													Color.White
												} else {
													Color.Transparent
												},
												shape = CircleShape
											)
											.border(
												width = 2.dp,
												color = Color.White,
												shape = CircleShape
											)
									)
								}
							}

							Surface(
								modifier = Modifier
									.fillMaxWidth()
									.clickable { tickFrequency.value = TickFrequency.DAILY },
								shape = MaterialTheme.shapes.extraLarge,
								color = if (tickFrequency.value == TickFrequency.DAILY) {
									Color.White.copy(alpha = 0.30f)
								} else {
									Color.White.copy(alpha = 0.14f)
								},
								border = BorderStroke(
									width = if (tickFrequency.value == TickFrequency.DAILY) 2.dp else 1.dp,
									color = if (tickFrequency.value == TickFrequency.DAILY) {
										Color.White.copy(alpha = 0.95f)
									} else {
										Color.White.copy(alpha = 0.35f)
									}
								)
							) {
								Row(
									modifier = Modifier
										.fillMaxWidth()
										.padding(horizontal = 18.dp, vertical = 18.dp),
									verticalAlignment = Alignment.CenterVertically,
									horizontalArrangement = Arrangement.SpaceBetween
								) {
									Column {
										Text(
											text = stringResource(Res.string.frequency_daily),
											style = MaterialTheme.typography.titleMedium.copy(
												color = Color.White
											)
										)
										Spacer(modifier = Modifier.height(4.dp))
										Text(
											text = stringResource(Res.string.tics_frequency_daily_description),
											style = MaterialTheme.typography.bodyMedium.copy(
												color = Color.White.copy(alpha = 0.8f)
											)
										)
									}

									Box(
										modifier = Modifier
											.size(24.dp)
											.background(
												color = if (tickFrequency.value == TickFrequency.DAILY) {
													Color.White
												} else {
													Color.Transparent
												},
												shape = CircleShape
											)
											.border(
												width = 2.dp,
												color = Color.White,
												shape = CircleShape
											)
									)
								}
							}
						}
					}

					// BOTTOM
					Column(
						modifier = Modifier.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally
					) {

						Row(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							repeat(5) { index ->
								Surface(
									modifier = Modifier
										.width(if (index == 4) 18.dp else 8.dp)
										.height(8.dp),
									shape = MaterialTheme.shapes.large,
									color = if (index == 4) {
										Color(0xFF2F327D)
									} else {
										Color.White.copy(alpha = 0.5f)
									}
								) {}
							}
						}

						Spacer(modifier = Modifier.height(24.dp))

						Surface(
							modifier = Modifier.fillMaxWidth(),
							shape = MaterialTheme.shapes.extraLarge,
							color = Color.White.copy(alpha = 0.22f)
						) {
							Text(
								text = stringResource(Res.string.tailor_app_experience),
								modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
								style = MaterialTheme.typography.bodyMedium.copy(
									color = Color.White
								)
							)
						}
					}
				}
			}

			6 -> {
				Column(
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
						.padding(24.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.SpaceBetween
				) {

					Column(
						modifier = Modifier.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally
					) {

						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween,
							verticalAlignment = Alignment.CenterVertically
						) {
							TextButton(onClick = { step.intValue = 5 }) {
								Text(
									text = stringResource(Res.string.back),
									color = Color.White
								)
							}

							Surface(
								shape = MaterialTheme.shapes.large,
								color = Color.White.copy(alpha = 0.28f)
							) {
								TextButton(onClick = { step.intValue = 7 }) {
									Text(
										text = stringResource(Res.string.next),
										color = Color.White
									)
								}
							}
						}

						Spacer(modifier = Modifier.height(40.dp))

						Text(
							text = stringResource(Res.string.goal_title),
							style = MaterialTheme.typography.headlineMedium.copy(
								color = Color(0xFF2F327D)
							)
						)

						Spacer(modifier = Modifier.height(16.dp))

						Text(
							text = stringResource(Res.string.goal_question),
							style = MaterialTheme.typography.bodyLarge.copy(
								color = Color.White.copy(alpha = 0.9f)
							)
						)

						Spacer(modifier = Modifier.height(32.dp))

						Surface(
							modifier = Modifier.fillMaxWidth(),
							shape = MaterialTheme.shapes.extraLarge,
							color = Color.White.copy(alpha = 0.18f),
							border = BorderStroke(
								width = 1.dp,
								color = Color.White.copy(alpha = 0.35f)
							)
						) {
							Column(
								modifier = Modifier.padding(18.dp)
							) {
								Text(
									text = stringResource(Res.string.goal_input_label),
									style = MaterialTheme.typography.labelLarge.copy(
										color = Color.White.copy(alpha = 0.8f)
									)
								)

								Spacer(modifier = Modifier.height(10.dp))

								TextField(
									value = goal.value,
									onValueChange = { goal.value = it },
									label = "",
									singleLine = false,
									modifier = Modifier.height(180.dp)
								)
							}
						}

						Spacer(modifier = Modifier.height(16.dp))

						Surface(
							modifier = Modifier.fillMaxWidth(),
							shape = MaterialTheme.shapes.extraLarge,
							color = Color.White.copy(alpha = 0.12f)
						) {
							Text(
								text = stringResource(Res.string.goal_example),
								modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
								style = MaterialTheme.typography.bodyMedium.copy(
									color = Color.White.copy(alpha = 0.75f)
								)
							)
						}
					}


					Column(
						modifier = Modifier.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally
					) {

						Row(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							repeat(5) { index ->
								Surface(
									modifier = Modifier
										.width(if (index == 4) 18.dp else 8.dp)
										.height(8.dp),
									shape = MaterialTheme.shapes.large,
									color = if (index == 4) {
										Color(0xFF2F327D)
									} else {
										Color.White.copy(alpha = 0.5f)
									}
								) {}
							}
						}

						Spacer(modifier = Modifier.height(24.dp))

						Surface(
							modifier = Modifier.fillMaxWidth(),
							shape = MaterialTheme.shapes.extraLarge,
							color = Color.White.copy(alpha = 0.22f)
						) {
							Text(
								text = stringResource(Res.string.tailor_app_experience),
								modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
								style = MaterialTheme.typography.bodyMedium.copy(
									color = Color.White
								)
							)
						}
					}
				}
			}

			7 -> {
				Column(
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
						.padding(24.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.SpaceBetween
				) {

					Column(
						modifier = Modifier.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally
					) {

						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween,
							verticalAlignment = Alignment.CenterVertically
						) {
							TextButton(onClick = { step.intValue = 6 }) {
								Text(
									text = stringResource(Res.string.back),
									color = Color.White
								)
							}

							TextButton(onClick = onSkip) {
								Text(
									text = stringResource(Res.string.skip),
									color = Color.White
								)
							}
						}

						Spacer(modifier = Modifier.height(40.dp))

						Text(
							text = stringResource(Res.string.follow_progress_title),
							style = MaterialTheme.typography.headlineMedium.copy(
								color = Color(0xFF2F327D)
							)
						)

						Spacer(modifier = Modifier.height(16.dp))

						Text(
							text = stringResource(Res.string.follow_progress_question),
							style = MaterialTheme.typography.bodyLarge.copy(
								color = Color.White.copy(alpha = 0.9f)
							)
						)

						Spacer(modifier = Modifier.height(32.dp))

						Surface(
							modifier = Modifier
								.fillMaxWidth()
								.clickable {
									followProgress.value = !followProgress.value
								},
							shape = MaterialTheme.shapes.extraLarge,
							color = Color.White.copy(alpha = 0.20f),
							border = BorderStroke(
								width = if (followProgress.value) 2.dp else 1.dp,
								color = if (followProgress.value) {
									Color.White
								} else {
									Color.White.copy(alpha = 0.35f)
								}
							)
						) {
							Row(
								modifier = Modifier
									.fillMaxWidth()
									.padding(horizontal = 18.dp, vertical = 20.dp),
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.SpaceBetween
							) {

								Column {
									Text(
										text = stringResource(Res.string.track_progress_option),
										style = MaterialTheme.typography.titleMedium.copy(
											color = Color.White
										)
									)

									Spacer(modifier = Modifier.height(4.dp))

									Text(
										text = stringResource(Res.string.track_progress_description),
										style = MaterialTheme.typography.bodyMedium.copy(
											color = Color.White.copy(alpha = 0.75f)
										)
									)
								}

								Box(
									modifier = Modifier
										.size(26.dp)
										.background(
											color = if (followProgress.value) Color.White else Color.Transparent,
											shape = CircleShape
										)
										.border(
											width = 2.dp,
											color = Color.White,
											shape = CircleShape
										)
								)
							}
						}

						Spacer(modifier = Modifier.height(20.dp))

						Surface(
							modifier = Modifier.fillMaxWidth(),
							shape = MaterialTheme.shapes.extraLarge,
							color = Color.White.copy(alpha = 0.12f)
						) {
							Text(
								text = stringResource(Res.string.can_change_later),
								modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
								style = MaterialTheme.typography.bodySmall.copy(
									color = Color.White.copy(alpha = 0.7f)
								)
							)
						}
					}

					Column(
						modifier = Modifier.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally
					) {

						Row(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							repeat(5) { index ->
								Surface(
									modifier = Modifier
										.width(if (index == 4) 18.dp else 8.dp)
										.height(8.dp),
									shape = MaterialTheme.shapes.large,
									color = if (index == 4) {
										Color(0xFF2F327D)
									} else {
										Color.White.copy(alpha = 0.5f)
									}
								) {}
							}
						}

						Spacer(modifier = Modifier.height(24.dp))

						Surface(
							modifier = Modifier.fillMaxWidth(),
							shape = MaterialTheme.shapes.extraLarge,
							color = Color.White.copy(alpha = 0.28f)
						) {
							TextButton(
								onClick = { onFinished(buildUpdate()) },
								modifier = Modifier.fillMaxWidth()
							) {
								Text(
									text = stringResource(Res.string.finish),
									style = MaterialTheme.typography.titleMedium,
									color = Color.White
								)
							}
						}
					}
				}
			}
		}
	}
}
