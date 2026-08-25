package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.status_downloaded
import calmedtics.shared.generated.resources.status_failed
import com.calmed.calmedtics.service.specification.LocalVideoDownloadManager
import com.calmed.calmedtics.service.specification.VideoDownloadStatus
import com.calmed.calmedtics.service.specification.stateFor
import org.jetbrains.compose.resources.stringResource

private val DownloadedGreen = Color(0xFF4CAF50)
private val FailedRed = Color(0xFFF44336)
private val ScrimBlack = Color(0x99000000)

@Composable
fun VideoDownloadBadge(
    videoUrl: String?,
    modifier: Modifier = Modifier
) {
    if (videoUrl.isNullOrBlank()) return

    val states by LocalVideoDownloadManager.states.collectAsState()
    val status = states.stateFor(videoUrl).status

    when (status) {
        VideoDownloadStatus.NotDownloaded -> Unit

        VideoDownloadStatus.Downloading -> {
            BadgeContainer(modifier) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            }
        }

        VideoDownloadStatus.Downloaded -> {
            BadgeContainer(modifier) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = stringResource(Res.string.status_downloaded),
                    tint = DownloadedGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        VideoDownloadStatus.Failed -> {
            BadgeContainer(modifier) {
                Icon(
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = stringResource(Res.string.status_failed),
                    tint = FailedRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun BadgeContainer(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(ScrimBlack),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
