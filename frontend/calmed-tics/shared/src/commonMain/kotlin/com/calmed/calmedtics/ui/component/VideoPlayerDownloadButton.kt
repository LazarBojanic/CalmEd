package com.calmed.calmedtics.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.cancel
import calmedtics.shared.generated.resources.cancel_download
import calmedtics.shared.generated.resources.cancel_download_confirm_message
import calmedtics.shared.generated.resources.cancel_download_confirm_title
import calmedtics.shared.generated.resources.delete
import calmedtics.shared.generated.resources.delete_confirm_message
import calmedtics.shared.generated.resources.delete_confirm_title
import calmedtics.shared.generated.resources.download_video
import calmedtics.shared.generated.resources.download_wifi_only_blocked
import calmedtics.shared.generated.resources.remove_downloaded_video
import calmedtics.shared.generated.resources.retry_video_download
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.service.specification.IVideoDownloadManager
import com.calmed.calmedtics.service.specification.VideoDownloadStatus
import com.calmed.calmedtics.service.specification.stateFor
import com.calmed.calmedtics.settings.AppSettings
import com.calmed.calmedtics.util.NetworkType
import com.calmed.calmedtics.util.currentNetworkType
import com.calmed.calmedtics.video.VideoQuality
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun VideoPlayerDownloadButton(
	exercise: ProgramExerciseDto,
	quality: VideoQuality,
	modifier: Modifier = Modifier,
) {
	val playbackId = exercise.playbackId
	val appSettings: AppSettings = koinInject()
	val videoDownloadManager: IVideoDownloadManager = koinInject()
	val states by
		videoDownloadManager.states.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
	val state = states.stateFor(playbackId)
	val status = state.status

	var showDeleteConfirm by remember { mutableStateOf(false) }
	var showCancelConfirm by remember { mutableStateOf(false) }

	val wifiBlockedMessage = stringResource(Res.string.download_wifi_only_blocked)

	val inFlight = status.isInFlight()

	val description = when (status) {
		VideoDownloadStatus.NotDownloaded,
		VideoDownloadStatus.Stopped -> stringResource(Res.string.download_video)
		VideoDownloadStatus.Starting,
		VideoDownloadStatus.Queued,
		VideoDownloadStatus.Downloading,
		VideoDownloadStatus.Removing -> stringResource(Res.string.cancel_download)
		VideoDownloadStatus.Downloaded -> stringResource(Res.string.remove_downloaded_video)
		VideoDownloadStatus.Failed,
		VideoDownloadStatus.Expired -> stringResource(Res.string.retry_video_download)
	}

	fun tryDownload() {
		if (appSettings.isDownloadWifiOnly()) {
			val networkType = currentNetworkType()
			if (networkType != NetworkType.Wifi && networkType != NetworkType.Ethernet) {
				ToastCenter.show(wifiBlockedMessage, ToastKind.Error)
				return
			}
		}

		videoDownloadManager.startDownload(exercise, quality)
	}

	val onClick = {
		when (status) {
			VideoDownloadStatus.Downloaded -> showDeleteConfirm = true
			VideoDownloadStatus.Starting,
			VideoDownloadStatus.Queued,
			VideoDownloadStatus.Downloading -> showCancelConfirm = true
			VideoDownloadStatus.Removing -> Unit
			VideoDownloadStatus.NotDownloaded,
			VideoDownloadStatus.Failed,
			VideoDownloadStatus.Expired,
			VideoDownloadStatus.Stopped -> tryDownload()
		}
	}

	if (inFlight) {
		VideoOverlayProgressButton(
			progress = (state.progressPercent ?: 0f) / 100f,
			contentDescription = description,
			onClick = onClick,
			modifier = modifier,
		)
	} else {
		val icon = when (status) {
			VideoDownloadStatus.Downloaded -> Icons.Filled.CheckCircle
			VideoDownloadStatus.Failed,
			VideoDownloadStatus.Expired -> Icons.Filled.ErrorOutline
			else -> Icons.Filled.Download
		}

		VideoOverlayButton(
			icon = icon,
			contentDescription = description,
			onClick = onClick,
			modifier = modifier,
		)
	}

	if (showDeleteConfirm) {
		AlertDialog(
			onDismissRequest = { showDeleteConfirm = false },
			title = { Text(stringResource(Res.string.delete_confirm_title)) },
			text = { Text(stringResource(Res.string.delete_confirm_message)) },
			confirmButton = {
				TextButton(
					onClick = {
						videoDownloadManager.remove(playbackId)
						showDeleteConfirm = false
					}
				) {
					Text(stringResource(Res.string.delete))
				}
			},
			dismissButton = {
				TextButton(onClick = { showDeleteConfirm = false }) {
					Text(stringResource(Res.string.cancel))
				}
			},
		)
	}

	if (showCancelConfirm) {
		AlertDialog(
			onDismissRequest = { showCancelConfirm = false },
			title = { Text(stringResource(Res.string.cancel_download_confirm_title)) },
			text = { Text(stringResource(Res.string.cancel_download_confirm_message)) },
			confirmButton = {
				TextButton(
					onClick = {
						videoDownloadManager.remove(playbackId)
						showCancelConfirm = false
					}
				) {
					Text(stringResource(Res.string.cancel_download))
				}
			},
			dismissButton = {
				TextButton(onClick = { showCancelConfirm = false }) {
					Text(stringResource(Res.string.cancel))
				}
			},
		)
	}
}

private fun VideoDownloadStatus.isInFlight(): Boolean = when (this) {
	VideoDownloadStatus.Starting,
	VideoDownloadStatus.Queued,
	VideoDownloadStatus.Downloading,
	VideoDownloadStatus.Removing -> true
	else -> false
}
