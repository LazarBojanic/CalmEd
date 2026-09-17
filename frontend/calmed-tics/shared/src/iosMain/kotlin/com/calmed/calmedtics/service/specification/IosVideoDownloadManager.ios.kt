package com.calmed.calmedtics.service.specification

import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.video.VideoQuality
import com.calmed.calmedtics.video.download.IosOfflineDownloadListener
import com.calmed.calmedtics.video.download.IosOfflineDownloadRegistry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class IosVideoDownloadManager : IVideoDownloadManager {

	private val bridge get() = IosOfflineDownloadRegistry.bridge

	private val _states = MutableStateFlow<Map<String, VideoDownloadState>>(emptyMap())
	override val states: StateFlow<Map<String, VideoDownloadState>> = _states

	private val _events = MutableSharedFlow<DownloadEvent>(extraBufferCapacity = 32)
	override val events: SharedFlow<DownloadEvent> = _events

	private val listener = object : IosOfflineDownloadListener {
		override fun onDownloadState(
			playbackId: String,
			status: String,
			progress: Double,
			title: String?,
		) {
			val previous = _states.value[playbackId]?.status
			val mapped = status.toDownloadStatus()
			val state = VideoDownloadState(
				status = mapped,
				progressPercent = progress.takeIf { it >= 0.0 }?.toFloat(),
			)

			_states.update { it + (playbackId to state) }
			emitTransition(previous, mapped, playbackId)
		}

		override fun onDownloadRemoved(playbackId: String) {
			_states.update { it - playbackId }
		}
	}

	init {
		bridge?.startObserving(listener)
		bridge?.resumePendingDownloads()
		refresh()
	}

	override fun startDownload(
		exercise: ProgramExerciseDto,
		quality: VideoQuality,
	) {
		val playbackId = exercise.playbackId
		if (playbackId.isBlank()) return

		_states.update {
			it + (playbackId to VideoDownloadState(VideoDownloadStatus.Starting, 0f))
		}

		bridge?.startDownload(
			playbackId = playbackId,
			token = exercise.tokenFor(quality),
			title = exercise.title,
			maxResolution = quality.maxResolution,
		)
	}

	override fun remove(playbackId: String) {
		_states.update { it - playbackId }
		bridge?.removeDownload(playbackId)
	}

	override fun refresh() {
		bridge?.refresh()
	}

	override fun setWifiOnly(wifiOnly: Boolean) {
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

	private fun String.toDownloadStatus(): VideoDownloadStatus =
		VideoDownloadStatus.entries.firstOrNull { it.name == this }
			?: VideoDownloadStatus.NotDownloaded
}
