package com.calmed.calmedtics.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.calmed.calmedtics.service.specification.LocalVideoDownloadManager
import com.calmed.calmedtics.service.specification.VideoDownloadStatus
import com.calmed.calmedtics.service.specification.stateFor
import com.calmed.calmedtics.settings.AppSettings
import com.calmed.calmedtics.video.applyMaxResolution
import org.koin.compose.koinInject


@Composable
fun VideoPlayerDownloadButton(
    hlsUrl: String,
    title: String?,
    modifier: Modifier = Modifier
) {
    val appSettings: AppSettings = koinInject()
    val states by LocalVideoDownloadManager.states.collectAsState()
    val status = states.stateFor(hlsUrl).status

    val icon = when (status) {
        VideoDownloadStatus.NotDownloaded,
        VideoDownloadStatus.Downloading -> Icons.Filled.Download
        VideoDownloadStatus.Downloaded -> Icons.Filled.CheckCircle
        VideoDownloadStatus.Failed -> Icons.Filled.ErrorOutline
    }

    val description = when (status) {
        VideoDownloadStatus.NotDownloaded -> "Download video"
        VideoDownloadStatus.Downloading -> "Downloading video"
        VideoDownloadStatus.Downloaded -> "Remove downloaded video"
        VideoDownloadStatus.Failed -> "Retry video download"
    }

    VideoOverlayButton(
        icon = icon,
        contentDescription = description,
        onClick = {
            when (status) {
                VideoDownloadStatus.Downloaded -> LocalVideoDownloadManager.remove(hlsUrl)
                VideoDownloadStatus.Downloading -> Unit
                VideoDownloadStatus.NotDownloaded,
                VideoDownloadStatus.Failed -> {
                    val resolved =
                        applyMaxResolution(hlsUrl, appSettings.getDownloadResolution())
                    LocalVideoDownloadManager.download(resolved, title)
                }
            }
        },
        modifier = modifier.then(
            if (status == VideoDownloadStatus.Downloading) {
                Modifier.alpha(0.5f)
            } else {
                Modifier
            }
        )
    )
}
