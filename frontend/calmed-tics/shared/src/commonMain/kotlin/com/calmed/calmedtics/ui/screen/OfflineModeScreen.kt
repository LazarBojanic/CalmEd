package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.cancel
import calmedtics.shared.generated.resources.delete
import calmedtics.shared.generated.resources.delete_confirm_message
import calmedtics.shared.generated.resources.delete_confirm_title
import calmedtics.shared.generated.resources.downloaded_videos
import calmedtics.shared.generated.resources.no_downloads
import calmedtics.shared.generated.resources.offline_description
import calmedtics.shared.generated.resources.offline_label
import calmedtics.shared.generated.resources.play_offline_video
import calmedtics.shared.generated.resources.remove_downloaded_video
import calmedtics.shared.generated.resources.status_downloaded
import calmedtics.shared.generated.resources.status_downloading
import calmedtics.shared.generated.resources.status_failed
import calmedtics.shared.generated.resources.status_not_downloaded
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.repository.DownloadedVideosRepository
import com.calmed.calmedtics.service.specification.IVideoDownloadManager
import com.calmed.calmedtics.service.specification.VideoDownloadState
import com.calmed.calmedtics.service.specification.VideoDownloadStatus
import com.calmed.calmedtics.service.specification.stateFor
import com.calmed.calmedtics.theme.appBackgroundGradient
import calmedtics.shared.generated.resources.try_online
import calmedtics.shared.generated.resources.you_are_offline
import com.calmed.calmedtics.ui.component.AppToastHost
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun OfflineModeScreen(
	onTryOnline: () -> Unit,
	onOpenDownload: (ProgramExerciseDto) -> Unit,
) {
	val videoDownloadManager: IVideoDownloadManager = koinInject()
	val downloadedVideosRepository: DownloadedVideosRepository = koinInject()
	val downloadedVideos by downloadedVideosRepository.downloadedExercises
		.collectAsStateWithLifecycle(
			initialValue = emptyList(),
			lifecycleOwner = LocalLifecycleOwner.current,
		)

	val states by videoDownloadManager.states
		.collectAsStateWithLifecycle(LocalLifecycleOwner.current)

	var pendingDelete by remember { mutableStateOf<ProgramExerciseDto?>(null) }

	LaunchedEffect(Unit) {
		videoDownloadManager.refresh()
	}

	Box(modifier = Modifier.fillMaxSize()) {
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.background(appBackgroundGradient()),
			contentPadding = PaddingValues(
				start = 16.dp,
				top = 18.dp,
				end = 16.dp,
				bottom = 96.dp,
			),
			verticalArrangement = Arrangement.spacedBy(18.dp),
		) {
			item {
				Text(
					text = stringResource(Res.string.you_are_offline),
					fontSize = 15.sp,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}

			item {
				Card(
					modifier = Modifier.fillMaxWidth(),
					shape = RoundedCornerShape(24.dp),
					colors = CardDefaults.cardColors(
						containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
					),
					elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
				) {
					Row(
						modifier = Modifier.padding(18.dp),
						verticalAlignment = Alignment.CenterVertically,
					) {
						Box(
							modifier = Modifier
								.size(48.dp)
								.background(
									brush = Brush.linearGradient(
										colors = listOf(
											MaterialTheme.colorScheme.primaryContainer,
											MaterialTheme.colorScheme.secondaryContainer,
										)
									),
									shape = RoundedCornerShape(15.dp),
								),
							contentAlignment = Alignment.Center,
						) {
							Icon(
								imageVector = Icons.Default.CloudOff,
								contentDescription = stringResource(Res.string.offline_label),
								tint = MaterialTheme.colorScheme.primary,
								modifier = Modifier.size(27.dp),
							)
						}

						Spacer(modifier = Modifier.width(13.dp))

						Column(
							modifier = Modifier.weight(1f),
							verticalArrangement = Arrangement.spacedBy(2.dp),
						) {
							Text(
								text = stringResource(Res.string.offline_label),
								fontSize = 17.sp,
								fontWeight = FontWeight.Bold,
								color = MaterialTheme.colorScheme.onSurface,
							)

							Text(
								text = stringResource(Res.string.offline_description),
								fontSize = 13.sp,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
							)
						}
					}
				}
			}

			item {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.clip(RoundedCornerShape(18.dp))
						.background(
							brush = Brush.horizontalGradient(
								colors = listOf(
									MaterialTheme.colorScheme.primary,
									MaterialTheme.colorScheme.secondary,
								)
							)
						)
						.clickable { onTryOnline() }
						.padding(14.dp),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = stringResource(Res.string.try_online),
						fontSize = 15.sp,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onPrimary,
					)
				}
			}

			item {
				Text(
					text = stringResource(Res.string.downloaded_videos, downloadedVideos.size),
					fontSize = 18.sp,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onSurface,
				)
			}

			if (downloadedVideos.isEmpty()) {
				item {
					Card(
						modifier = Modifier.fillMaxWidth(),
						shape = RoundedCornerShape(18.dp),
						colors = CardDefaults.cardColors(
							containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
						),
						elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
					) {
						Text(
							text = stringResource(Res.string.no_downloads),
							modifier = Modifier.padding(16.dp),
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
						)
					}
				}
			} else {
				items(downloadedVideos, key = { it.playbackId }) { exercise ->
					val state = states.stateFor(exercise.playbackId)
					val displayTitle = exercise.title.ifBlank { exercise.playbackId }

					Card(
						modifier = Modifier
							.fillMaxWidth()
							.clickable { onOpenDownload(exercise) },
						shape = RoundedCornerShape(20.dp),
						colors = CardDefaults.cardColors(
							containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
						),
						elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
					) {
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.padding(12.dp),
							verticalAlignment = Alignment.CenterVertically,
						) {
							Box(
								modifier = Modifier
									.size(44.dp)
									.background(
										brush = Brush.linearGradient(
											colors = listOf(
												MaterialTheme.colorScheme.primary,
												MaterialTheme.colorScheme.secondary,
											)
										),
										shape = CircleShape,
									),
								contentAlignment = Alignment.Center,
							) {
								Icon(
									imageVector = Icons.Default.PlayArrow,
									contentDescription = stringResource(Res.string.play_offline_video),
									tint = MaterialTheme.colorScheme.onPrimary,
									modifier = Modifier.size(22.dp),
								)
							}

							Spacer(modifier = Modifier.width(12.dp))

							Column(
								modifier = Modifier.weight(1f),
								verticalArrangement = Arrangement.spacedBy(4.dp),
							) {
								Text(
									text = displayTitle,
									fontSize = 15.sp,
									fontWeight = FontWeight.Bold,
									color = MaterialTheme.colorScheme.onSurface,
									maxLines = 2,
									overflow = TextOverflow.Ellipsis,
								)

								Text(
									text = statusLabel(state),
									fontSize = 11.sp,
									fontWeight = FontWeight.Medium,
									color = MaterialTheme.colorScheme.onSurfaceVariant,
								)
							}

							Icon(
								imageVector = statusIcon(state.status),
								contentDescription = statusLabel(state),
								tint = MaterialTheme.colorScheme.primary,
								modifier = Modifier.size(20.dp),
							)

							Spacer(modifier = Modifier.width(6.dp))

							IconButton(onClick = { pendingDelete = exercise }) {
								Icon(
									imageVector = Icons.Default.Delete,
									contentDescription = stringResource(Res.string.remove_downloaded_video),
									tint = MaterialTheme.colorScheme.onSurfaceVariant,
								)
							}
						}
					}
				}
			}
		}

		AppToastHost(
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.navigationBarsPadding(),
		)
	}

	pendingDelete?.let { video ->
		AlertDialog(
			onDismissRequest = { pendingDelete = null },
			title = { Text(stringResource(Res.string.delete_confirm_title)) },
			text = { Text(stringResource(Res.string.delete_confirm_message)) },
			confirmButton = {
				TextButton(
					onClick = {
						videoDownloadManager.remove(video.playbackId)
						pendingDelete = null
					}
				) {
					Text(stringResource(Res.string.delete))
				}
			},
			dismissButton = {
				TextButton(onClick = { pendingDelete = null }) {
					Text(stringResource(Res.string.cancel))
				}
			},
		)
	}
}

@Composable
private fun statusLabel(state: VideoDownloadState): String {
	return when (state.status) {
		VideoDownloadStatus.NotDownloaded,
		VideoDownloadStatus.Stopped -> stringResource(Res.string.status_not_downloaded)

		VideoDownloadStatus.Starting,
		VideoDownloadStatus.Queued,
		VideoDownloadStatus.Downloading,
		VideoDownloadStatus.Removing -> {
			val progress = state.progressPercent?.toInt()
			if (progress != null) {
				"${stringResource(Res.string.status_downloading)} $progress%"
			} else {
				stringResource(Res.string.status_downloading)
			}
		}

		VideoDownloadStatus.Downloaded -> stringResource(Res.string.status_downloaded)

		VideoDownloadStatus.Failed,
		VideoDownloadStatus.Expired -> stringResource(Res.string.status_failed)
	}
}

private fun statusIcon(status: VideoDownloadStatus) = when (status) {
	VideoDownloadStatus.Downloaded -> Icons.Default.CheckCircle
	VideoDownloadStatus.Failed,
	VideoDownloadStatus.Expired -> Icons.Default.ErrorOutline
	else -> Icons.Default.Download
}
