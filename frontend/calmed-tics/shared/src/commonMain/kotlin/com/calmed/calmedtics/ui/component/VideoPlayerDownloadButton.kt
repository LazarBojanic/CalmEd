package com.calmed.calmedtics.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.cancel
import calmedtics.shared.generated.resources.delete
import calmedtics.shared.generated.resources.delete_confirm_message
import calmedtics.shared.generated.resources.delete_confirm_title
import calmedtics.shared.generated.resources.download_wifi_only_blocked
import com.calmed.calmedtics.service.specification.LocalVideoDownloadManager
import com.calmed.calmedtics.service.specification.VideoDownloadStatus
import com.calmed.calmedtics.service.specification.stateFor
import com.calmed.calmedtics.settings.AppSettings
import com.calmed.calmedtics.util.NetworkType
import com.calmed.calmedtics.util.currentNetworkType
import com.calmed.calmedtics.video.applyMaxResolution
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject


@Composable
fun VideoPlayerDownloadButton(
    hlsUrl: String,
    title: String?,
    modifier: Modifier = Modifier
) {
    val appSettings: AppSettings = koinInject()
    val states by LocalVideoDownloadManager.states.collectAsState()
    val state = states.stateFor(hlsUrl)
    val status = state.status

    var showDeleteConfirm by remember { mutableStateOf(false) }

    val wifiBlockedMessage = stringResource(Res.string.download_wifi_only_blocked)

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

    fun tryDownload() {
        if (appSettings.isDownloadWifiOnly()) {
            val networkType = currentNetworkType()
            if (networkType != NetworkType.Wifi && networkType != NetworkType.Ethernet) {
                ToastCenter.show(wifiBlockedMessage, ToastKind.Error)
                return
            }
        }

        val resolved =
            applyMaxResolution(hlsUrl, appSettings.getDownloadResolution())
        LocalVideoDownloadManager.download(resolved, title)
    }

    val onClick = {
        when (status) {
            VideoDownloadStatus.Downloaded -> showDeleteConfirm = true
            VideoDownloadStatus.Downloading -> Unit
            VideoDownloadStatus.NotDownloaded,
            VideoDownloadStatus.Failed -> tryDownload()
        }
    }

    if (status == VideoDownloadStatus.Downloading) {
        VideoOverlayProgressButton(
            progress = (state.progressPercent ?: 0f) / 100f,
            contentDescription = description,
            onClick = onClick,
            modifier = modifier
        )
    } else {
        VideoOverlayButton(
            icon = icon,
            contentDescription = description,
            onClick = onClick,
            modifier = modifier
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(stringResource(Res.string.delete_confirm_title))
            },
            text = {
                Text(stringResource(Res.string.delete_confirm_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        LocalVideoDownloadManager.remove(hlsUrl)
                        showDeleteConfirm = false
                    }
                ) {
                    Text(stringResource(Res.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false }
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}
