package com.calmed.calmedtics.video.download

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.exoplayer.scheduler.Requirements
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.calmed.calmedtics.logging.AppLog
import com.calmed.calmedtics.logging.LogTags
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.service.specification.DownloadEvent
import com.calmed.calmedtics.service.specification.DownloadEventType
import com.calmed.calmedtics.service.specification.IVideoDownloadManager
import com.calmed.calmedtics.service.specification.VideoDownloadState
import com.calmed.calmedtics.service.specification.VideoDownloadStatus
import com.calmed.calmedtics.video.VideoQuality
import com.mux.player.media.MediaItems
import com.mux.player.media.PlaybackResolution
import com.mux.player.offline.MuxDownload
import com.mux.player.offline.MuxDownloadManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val log = AppLog(LogTags.VIDEO)

class MuxVideoDownloadManager(
	context: Context,
	private val wifiOnlyProvider: () -> Boolean = { false },
) : IVideoDownloadManager {

	private val appContext = context.applicationContext
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

	private val _states = MutableStateFlow<Map<String, VideoDownloadState>>(emptyMap())
	override val states: StateFlow<Map<String, VideoDownloadState>> = _states

	private val _events = MutableSharedFlow<DownloadEvent>(extraBufferCapacity = 32)
	override val events: SharedFlow<DownloadEvent> = _events

	private val listener = object : MuxDownloadManager.Listener {
		override fun onDownloadChanged(download: MuxDownload, error: Throwable?) {
			if (error != null) {
				log.error("Download failed for playback ID ${download.playbackId}", error)
			}
			applySnapshot(download)
		}

		override fun onDownloadRemoved(download: MuxDownload) {
			_states.update { it - download.playbackId }
		}
	}

	init {
		MuxDownloadManager.addListener(appContext, listener)
		applyRequirements()
		refresh()
	}

	override fun startDownload(
		exercise: ProgramExerciseDto,
		quality: VideoQuality,
	) {
		val playbackId = exercise.playbackId
		if (playbackId.isBlank()) return

		applyRequirements()

		_states.update {
			it + (playbackId to VideoDownloadState(VideoDownloadStatus.Starting, 0f))
		}

		val token = exercise.tokenFor(quality)
		val mediaItem = MediaItems.builderFromMuxPlaybackId(
			playbackId = playbackId,
			maxResolution = if (token.isNullOrBlank()) quality.toPlaybackResolution() else null,
			playbackToken = token,
		)
			.setMediaMetadata(
				MediaMetadata.Builder()
					.setTitle(exercise.title)
					.build()
			)
			.build()

		MuxDownloadManager.startDownload(appContext, mediaItem)
	}

	override fun remove(playbackId: String) {
		_states.update { it - playbackId }
		MuxDownloadManager.removeDownload(appContext, playbackId)
	}

	override fun refresh() {
		scope.launch {
			val downloads = runCatching { MuxDownloadManager.allDownloads(appContext) }
				.getOrElse { emptyList() }
			_states.value = downloads.associate { download ->
				download.playbackId to download.toState()
			}
		}
	}
	@OptIn(UnstableApi::class)
	override fun setWifiOnly(wifiOnly: Boolean) {
		val requirement =
			if (wifiOnly) Requirements.NETWORK_UNMETERED else Requirements.NETWORK
		MuxDownloadManager.setRequirements(appContext, Requirements(requirement))
	}

	private fun applyRequirements() {
		setWifiOnly(wifiOnlyProvider())
	}

	private fun applySnapshot(download: MuxDownload) {
		val previous = _states.value[download.playbackId]?.status
		val next = download.toState()

		_states.update { it + (download.playbackId to next) }
		emitTransition(previous, next.status, download.playbackId)
	}

	private fun emitTransition(
		previous: VideoDownloadStatus?,
		current: VideoDownloadStatus,
		playbackId: String,
	) {
		if (previous == current) return
		when (current) {
			VideoDownloadStatus.Downloaded ->
				_events.tryEmit(DownloadEvent(DownloadEventType.Completed, playbackId))
			VideoDownloadStatus.Expired ->
				_events.tryEmit(DownloadEvent(DownloadEventType.Expired, playbackId))
			VideoDownloadStatus.Failed ->
				_events.tryEmit(DownloadEvent(DownloadEventType.Failed, playbackId))
			else -> Unit
		}
	}

	private fun MuxDownload.toState(): VideoDownloadState {
		val progress = percentDownloaded
			.takeIf { it >= 0f }
			?.coerceIn(0f, 100f)

		return VideoDownloadState(
			status = state.toStatus(),
			progressPercent = progress,
		)
	}

	private fun MuxDownload.State.toStatus(): VideoDownloadStatus = when (this) {
		MuxDownload.State.STARTING -> VideoDownloadStatus.Starting
		MuxDownload.State.QUEUED -> VideoDownloadStatus.Queued
		MuxDownload.State.DOWNLOADING -> VideoDownloadStatus.Downloading
		MuxDownload.State.COMPLETED -> VideoDownloadStatus.Downloaded
		MuxDownload.State.EXPIRED -> VideoDownloadStatus.Expired
		MuxDownload.State.FAILED -> VideoDownloadStatus.Failed
		MuxDownload.State.REMOVING -> VideoDownloadStatus.Removing
		MuxDownload.State.STOPPED -> VideoDownloadStatus.Stopped
	}

	private fun VideoQuality.toPlaybackResolution(): PlaybackResolution = when (this) {
		VideoQuality.R480 -> PlaybackResolution.LD_480
		VideoQuality.R720 -> PlaybackResolution.HD_720
		VideoQuality.R1080 -> PlaybackResolution.FHD_1080
	}
}
