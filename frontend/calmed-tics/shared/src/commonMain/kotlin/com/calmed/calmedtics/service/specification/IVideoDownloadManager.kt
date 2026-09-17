package com.calmed.calmedtics.service.specification

import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.video.VideoQuality
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

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
)

enum class DownloadEventType {
	Completed,
	Failed,
	Expired,
}

data class DownloadEvent(
	val type: DownloadEventType,
	val playbackId: String,
)

interface IVideoDownloadManager {
	val states: StateFlow<Map<String, VideoDownloadState>>

	val events: SharedFlow<DownloadEvent>

	fun startDownload(
		exercise: ProgramExerciseDto,
		quality: VideoQuality = VideoQuality.R720,
	)

	fun remove(playbackId: String)

	fun refresh()

	fun setWifiOnly(wifiOnly: Boolean)
}

fun Map<String, VideoDownloadState>.stateFor(playbackId: String): VideoDownloadState {
	return this[playbackId] ?: VideoDownloadState(VideoDownloadStatus.NotDownloaded)
}
