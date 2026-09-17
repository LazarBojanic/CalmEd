package com.calmed.calmedtics.ui.screen

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.close
import calmedtics.shared.generated.resources.exercise_counter
import calmedtics.shared.generated.resources.mobile_rotate
import calmedtics.shared.generated.resources.mute
import calmedtics.shared.generated.resources.no_exercises_available
import calmedtics.shared.generated.resources.resolution
import calmedtics.shared.generated.resources.turn_your_phone
import calmedtics.shared.generated.resources.unmute
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.settings.AppSettings
import com.calmed.calmedtics.theme.appBackgroundGradient
import com.calmed.calmedtics.ui.component.AppToastHost
import com.calmed.calmedtics.ui.component.BackButton
import com.calmed.calmedtics.ui.component.CastButton
import com.calmed.calmedtics.ui.component.KeepScreenAwake
import com.calmed.calmedtics.ui.component.PlatformBackHandler
import com.calmed.calmedtics.ui.component.VideoItem
import com.calmed.calmedtics.ui.component.VideoOverlayButton
import com.calmed.calmedtics.ui.component.VideoPlayer
import com.calmed.calmedtics.ui.component.VideoPlayerDownloadButton
import com.calmed.calmedtics.video.VideoQuality
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun VideoScreen(
	exercises: List<ProgramExerciseDto>,
	startIndex: Int = 0,
	currentWeek: Int,
	onBack: () -> Unit,
) {
	val appSettings: AppSettings = koinInject()
	KeepScreenAwake(enabled = appSettings.isKeepScreenAwake())

	val playable = remember(exercises, currentWeek) {
		exercises.filter { it.weekNumber <= currentWeek }
	}

	if (playable.isEmpty()) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(appBackgroundGradient())
				.statusBarsPadding(),
			contentAlignment = Alignment.Center,
		) {
			BackButton(
				onClick = onBack,
				modifier = Modifier
					.align(Alignment.TopStart)
					.padding(16.dp),
			)
			Text(
				text = stringResource(Res.string.no_exercises_available),
				color = MaterialTheme.colorScheme.onSurface,
				fontSize = 18.sp,
			)
		}
		return
	}

	val safeStartIndex = remember(playable, exercises, startIndex) {
		val requestedId = exercises.getOrNull(startIndex)?.id
		playable.indexOfFirst { it.id == requestedId }.takeIf { it >= 0 } ?: 0
	}

	var currentIndex by remember(playable) { mutableStateOf(safeStartIndex) }
	var muted by rememberSaveable { mutableStateOf(false) }
	var quality by rememberSaveable(stateSaver = VideoQualitySaver) {
		mutableStateOf(appSettings.getDownloadResolution())
	}
	var isFullscreen by remember { mutableStateOf(false) }
	var showQualityPicker by remember { mutableStateOf(false) }
	var isPlaying by remember { mutableStateOf(false) }
	var controlsVisible by remember { mutableStateOf(true) }
	var interactionNonce by remember { mutableStateOf(0) }

	fun revealControls() {
		controlsVisible = true
		interactionNonce++
	}

	LaunchedEffect(controlsVisible, interactionNonce, isPlaying) {
		if (controlsVisible && isPlaying) {
			delay(3_000)
			controlsVisible = false
		}
	}

	val items = remember(playable) {
		playable.map { exercise ->
			VideoItem(
				playbackId = exercise.playbackId,
				token480 = exercise.tokenFor(VideoQuality.R480),
				token720 = exercise.tokenFor(VideoQuality.R720),
				token1080 = exercise.tokenFor(VideoQuality.R1080),
				title = exercise.title,
			)
		}
	}

	val current = playable[currentIndex.coerceIn(0, playable.lastIndex)]

	BoxWithConstraints(
		modifier = Modifier
			.fillMaxSize()
			.background(appBackgroundGradient()),
	) {
		val isLandscape = maxWidth > maxHeight
		val expanded = isFullscreen || isLandscape

		Column(
			modifier = Modifier
				.fillMaxSize()
				.then(if (expanded) Modifier else Modifier.statusBarsPadding()),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			val playerShape = RoundedCornerShape(20.dp)
			Box(
				modifier = if (expanded) {
					Modifier.fillMaxSize()
				} else {
					Modifier
						.fillMaxWidth()
						.padding(horizontal = 12.dp)
						.padding(top = 56.dp)
						.height(340.dp)
						.shadow(elevation = 12.dp, shape = playerShape, clip = false)
						.clip(playerShape)
						.background(Color.Black)
						.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.20f), playerShape)
				},
			) {
				VideoPlayer(
					items = items,
					startIndex = safeStartIndex,
					quality = quality,
					muted = muted,
					isFullscreen = isFullscreen,
					isImmersive = expanded,
					onFullscreenToggle = { isFullscreen = it },
					modifier = Modifier.fillMaxSize(),
					onIndexChanged = { index ->
						if (index in playable.indices) {
							currentIndex = index
						}
					},
					onControlsVisibilityChanged = { visible ->
						if (visible) {
							revealControls()
						} else if (isPlaying) {
							controlsVisible = false
						}
					},
					onIsPlayingChanged = { playing ->
						isPlaying = playing
						if (!playing) revealControls()
					},
				)

				androidx.compose.animation.AnimatedVisibility(
					visible = controlsVisible,
					enter = fadeIn(),
					exit = fadeOut(),
					modifier = Modifier
						.align(Alignment.TopEnd)
						.padding(8.dp),
				) {
					PlayerOverlayControls(
						exercise = current,
						quality = quality,
						muted = muted,
						onToggleMute = { muted = !muted },
						onShowQualityPicker = { showQualityPicker = true },
						onInteraction = { revealControls() },
					)
				}
			}

			if (!expanded) {
				Spacer(modifier = Modifier.height(20.dp))

				Text(
					text = current.title,
					color = MaterialTheme.colorScheme.onSurface,
					fontSize = 18.sp,
					fontWeight = FontWeight.SemiBold,
					textAlign = TextAlign.Center,
					modifier = Modifier.padding(horizontal = 24.dp),
				)

				Spacer(modifier = Modifier.height(16.dp))

				Icon(
					painter = painterResource(Res.drawable.mobile_rotate),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.size(84.dp),
				)

				Spacer(modifier = Modifier.height(8.dp))

				Text(
					text = stringResource(Res.string.turn_your_phone),
					color = MaterialTheme.colorScheme.onSurface,
					fontSize = 18.sp,
					fontWeight = FontWeight.Medium,
					textAlign = TextAlign.Center,
				)

				Spacer(modifier = Modifier.height(16.dp))

				Text(
					text = stringResource(
						Res.string.exercise_counter,
						currentIndex + 1,
						playable.size,
					),
					color = MaterialTheme.colorScheme.primary,
					fontSize = 16.sp,
					fontWeight = FontWeight.Medium,
					textAlign = TextAlign.Center,
				)
			}
		}

		if (!expanded) {
			BackButton(
				onClick = onBack,
				modifier = Modifier
					.align(Alignment.TopStart)
					.statusBarsPadding()
					.padding(start = 16.dp, top = 8.dp),
			)
		}

		AppToastHost(
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.navigationBarsPadding(),
		)
	}

	PlatformBackHandler(enabled = isFullscreen) { isFullscreen = false }

	if (showQualityPicker) {
		AlertDialog(
			onDismissRequest = { showQualityPicker = false },
			confirmButton = {
				TextButton(onClick = { showQualityPicker = false }) {
					Text(stringResource(Res.string.close))
				}
			},
			title = { Text(stringResource(Res.string.resolution)) },
			text = {
				Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
					VideoQuality.entries.forEach { option ->
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.clip(RoundedCornerShape(12.dp))
								.selectable(
									selected = quality == option,
									role = Role.RadioButton,
									onClick = {
										quality = option
										showQualityPicker = false
									},
								)
								.padding(horizontal = 8.dp, vertical = 4.dp),
							verticalAlignment = Alignment.CenterVertically,
						) {
							RadioButton(
								selected = quality == option,
								onClick = null,
							)
							Spacer(modifier = Modifier.width(8.dp))
							Text(
								text = option.label,
								color = MaterialTheme.colorScheme.onSurface,
								fontSize = 16.sp,
							)
						}
					}
				}
			},
		)
	}
}

@Composable
private fun PlayerOverlayControls(
	exercise: ProgramExerciseDto,
	quality: VideoQuality,
	muted: Boolean,
	onToggleMute: () -> Unit,
	onShowQualityPicker: () -> Unit,
	modifier: Modifier = Modifier,
	onInteraction: () -> Unit = {},
) {
	Column(
		modifier = modifier.pointerInput(Unit) {
			awaitPointerEventScope {
				while (true) {
					awaitFirstDown(requireUnconsumed = false)
					onInteraction()
				}
			}
		},
		verticalArrangement = Arrangement.spacedBy(4.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		VideoPlayerDownloadButton(
			exercise = exercise,
			quality = quality,
		)

		CastButton()

		VideoOverlayButton(
			icon = Icons.Default.Hd,
			contentDescription = stringResource(Res.string.resolution),
			onClick = onShowQualityPicker,
		)

		VideoOverlayButton(
			icon = if (muted) {
				Icons.AutoMirrored.Filled.VolumeOff
			} else {
				Icons.AutoMirrored.Filled.VolumeUp
			},
			contentDescription = if (muted) {
				stringResource(Res.string.unmute)
			} else {
				stringResource(Res.string.mute)
			},
			onClick = onToggleMute,
		)
	}
}

private val VideoQualitySaver: Saver<VideoQuality, String> = Saver(
	save = { it.name },
	restore = { VideoQuality.fromName(it) },
)
