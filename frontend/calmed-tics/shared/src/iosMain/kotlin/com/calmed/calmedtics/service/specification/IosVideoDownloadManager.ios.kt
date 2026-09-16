package com.calmed.calmedtics.service.specification

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

	private val _downloadedVideos = MutableStateFlow<List<DownloadedVideo>>(emptyList())
	override val downloadedVideos: StateFlow<List<DownloadedVideo>> = _downloadedVideos

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
				title = title,
			)

			_states.update { it + (playbackId to state) }
			recalculateDownloaded()
			emitTransition(previous, mapped, title)
		}

		override fun onDownloadRemoved(playbackId: String) {
			_states.update { it - playbackId }
			recalculateDownloaded()
		}
	}

	init {
		bridge?.startObserving(listener)
		bridge?.resumePendingDownloads()
		refresh()
	}

	override fun startDownload(
		playbackId: String,
		token: String?,
		title: String?,
		quality: VideoQuality,
	) {
		if (playbackId.isBlank()) return

		_states.update {
			it + (playbackId to VideoDownloadState(VideoDownloadStatus.Starting, 0f, title))
		}

		bridge?.startDownload(
			playbackId = playbackId,
			token = token,
			title = title,
			maxResolution = quality.maxResolution,
		)
	}

	override fun remove(playbackId: String) {
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
		title: String?,
	) {
		if (previous == current) return
		when (current) {
			VideoDownloadStatus.Downloaded ->
				_events.tryEmit(DownloadEvent(DownloadEventType.Completed, title))
			VideoDownloadStatus.Expired ->
				_events.tryEmit(DownloadEvent(DownloadEventType.Expired, title))
			VideoDownloadStatus.Failed ->
				_events.tryEmit(DownloadEvent(DownloadEventType.Failed, title))
			else -> Unit
		}
	}

	private fun recalculateDownloaded() {
		_downloadedVideos.value = _states.value
			.filterValues { it.status == VideoDownloadStatus.Downloaded }
			.map { (playbackId, state) -> DownloadedVideo(playbackId, state.title) }
			.sortedBy { it.playbackId }
	}

	private fun String.toDownloadStatus(): VideoDownloadStatus =
		VideoDownloadStatus.entries.firstOrNull { it.name == this }
			?: VideoDownloadStatus.NotDownloaded
}
