package com.calmed.calmedtics.video.download

import android.app.Notification
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import com.calmed.calmedtics.R

@OptIn(UnstableApi::class)
class VideoDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    FOREGROUND_NOTIFICATION_UPDATE_INTERVAL_MS,
    NOTIFICATION_CHANNEL_ID,
    R.string.download_channel_name,
    0
) {

    override fun getDownloadManager(): DownloadManager {
        return DownloadUtil.getDownloadManager(applicationContext)
    }

    override fun getScheduler(): Scheduler? {
        return null
    }

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
        return DownloadNotificationHelper(
            this,
            NOTIFICATION_CHANNEL_ID
        ).buildProgressNotification(
            this,
            android.R.drawable.stat_sys_download,
            null,
            null,
            downloads,
            notMetRequirements
        )
    }

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1001
        private const val FOREGROUND_NOTIFICATION_UPDATE_INTERVAL_MS = 1_000L
        private const val NOTIFICATION_CHANNEL_ID = "video_download_channel"
    }
}