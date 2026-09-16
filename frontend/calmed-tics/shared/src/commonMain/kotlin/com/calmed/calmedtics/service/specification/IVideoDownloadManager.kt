package com.calmed.calmedtics.service.specification

import com.calmed.calmedtics.video.VideoQuality
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Download lifecycle, mirroring the state set Mux Player reports. Downloads are keyed
 * by Mux playback ID.
 */
enum class VideoDownloadStatus {
	NotDownloaded,
	Starting,
	Queued,
	Downloading,
	Downloaded,
	Expired,
	Failed,
	Removing,
	Stopped,
}

data class VideoDownloadState(
	val status: VideoDownloadStatus,
	val progressPercent: Float? = null,
	val title: String? = null,
)

data class DownloadedVideo(
	val playbackId: String,
	val title: String? = null,
)

enum class DownloadEventType {
	Completed,
	Failed,
	Expired,
}

data class DownloadEvent(
	val type: DownloadEventType,
	val title: String? = null,
)

interface IVideoDownloadManager {
	val states: StateFlow<Map<String, VideoDownloadState>>

	val downloadedVideos: StateFlow<List<DownloadedVideo>>

	val events: SharedFlow<DownloadEvent>

	fun startDownload(
		playbackId: String,
		token: String? = null,
		title: String? = null,
		quality: VideoQuality = VideoQuality.R720,
	)

	fun remove(playbackId: String)

	fun refresh()

	fun setWifiOnly(wifiOnly: Boolean)
}

fun Map<String, VideoDownloadState>.stateFor(playbackId: String): VideoDownloadState {
	return this[playbackId] ?: VideoDownloadState(VideoDownloadStatus.NotDownloaded)
}
