package com.calmed.calmedfrontendtourettes.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calmed.calmedfrontendtourettes.service.specification.LocalVideoDownloadManager
import com.calmed.calmedfrontendtourettes.service.specification.VideoDownloadStatus
import com.calmed.calmedfrontendtourettes.service.specification.stateFor
import com.calmed.calmedfrontendtourettes.ui.component.PrimaryButton
import com.calmed.calmedfrontendtourettes.ui.component.ScreenScaffold

@Composable
fun OfflineModeScreen(
    onTryOnline: () -> Unit,
    onOpenFullscreen: (String) -> Unit
) {
    val downloadedUrls by LocalVideoDownloadManager.downloadedUrls.collectAsState()
    val states by LocalVideoDownloadManager.states.collectAsState()

    LaunchedEffect(Unit) {
        LocalVideoDownloadManager.refreshDownloaded()
    }

    LaunchedEffect(downloadedUrls) {
        downloadedUrls.forEach { url ->
            LocalVideoDownloadManager.refresh(url)
        }
    }

    ScreenScaffold(title = "Offline Library") {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Offline",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(
                        modifier = Modifier.padding(start = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "You are offline",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Only videos already downloaded to this device are available.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            PrimaryButton(
                text = "Try Online Mode",
                onClick = onTryOnline
            )

            Text(
                text = "Downloaded Videos (${downloadedUrls.size})",
                style = MaterialTheme.typography.titleMedium
            )

            if (downloadedUrls.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "No downloaded videos found on this device.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                return@Column
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(downloadedUrls, key = { it }) { url ->
                    val state = states.stateFor(url)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenFullscreen(url) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = url.fileNameOrFallback(),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = statusLabel(state.status),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Icon(
                                imageVector = statusIcon(state.status),
                                contentDescription = statusLabel(state.status),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            IconButton(onClick = { onOpenFullscreen(url) }) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play offline video"
                                )
                            }

                            IconButton(
                                onClick = {
                                    LocalVideoDownloadManager.remove(url)
                                    LocalVideoDownloadManager.refreshDownloaded()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove downloaded video"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun statusLabel(status: VideoDownloadStatus): String {
    return when (status) {
        VideoDownloadStatus.NotDownloaded -> "Not downloaded"
        VideoDownloadStatus.Downloading -> "Downloading"
        VideoDownloadStatus.Downloaded -> "Downloaded"
        VideoDownloadStatus.Failed -> "Download failed"
    }
}

private fun statusIcon(status: VideoDownloadStatus) = when (status) {
    VideoDownloadStatus.NotDownloaded -> Icons.Default.Download
    VideoDownloadStatus.Downloading -> Icons.Default.Download
    VideoDownloadStatus.Downloaded -> Icons.Default.CheckCircle
    VideoDownloadStatus.Failed -> Icons.Default.ErrorOutline
}

private fun String.fileNameOrFallback(): String {
    val fileName = substringAfterLast('/').substringBefore('?')
    return if (fileName.isBlank()) this else fileName
}
