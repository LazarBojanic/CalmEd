package com.calmed.calmedtics.video.download

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.scheduler.Requirements
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@OptIn(UnstableApi::class)
object DownloadUtil {

    private const val DOWNLOAD_CONTENT_DIRECTORY = "video_downloads"

    @Volatile
    private var downloadManager: DownloadManager? = null

    @Volatile
    private var downloadCache: SimpleCache? = null

    @Volatile
    private var databaseProvider: StandaloneDatabaseProvider? = null

    @Volatile
    private var downloadExecutor: Executor? = null

    fun getDownloadManager(context: Context): DownloadManager {
        val appContext = context.applicationContext

        return downloadManager ?: synchronized(this) {
            downloadManager
                ?: buildDownloadManager(appContext)
                    .also { downloadManager = it }
        }
    }

    fun getDownloadDataSourceFactory(
        context: Context
    ): DefaultDataSource.Factory {
        val appContext = context.applicationContext

        return DefaultDataSource.Factory(
            appContext,
            DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
        )
    }

    fun getPlaybackDataSourceFactory(
        context: Context
    ): CacheDataSource.Factory {
        val appContext = context.applicationContext

        val upstreamFactory =
            DefaultDataSource.Factory(
                appContext,
                DefaultHttpDataSource.Factory()
                    .setAllowCrossProtocolRedirects(true)
            )

        return CacheDataSource.Factory()
            .setCache(getDownloadCache(appContext))
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(null)
    }

    private fun buildDownloadManager(
        context: Context
    ): DownloadManager {
        val manager =
            DownloadManager(
                context,
                getDatabaseProvider(context),
                getDownloadCache(context),
                DefaultHttpDataSource.Factory()
                    .setAllowCrossProtocolRedirects(true),
                getDownloadExecutor()
            )

        manager.maxParallelDownloads = 2


        manager.setRequirements(
            Requirements(Requirements.NETWORK)
        )

        manager.resumeDownloads()

        return manager
    }

    private fun getDownloadCache(
        context: Context
    ): SimpleCache {
        val downloadDirectory =
            File(
                context.filesDir,
                DOWNLOAD_CONTENT_DIRECTORY
            ).apply {
                if (!exists()) {
                    mkdirs()
                }
            }

        return downloadCache ?: synchronized(this) {
            downloadCache
                ?: SimpleCache(
                    downloadDirectory,
                    NoOpCacheEvictor(),
                    getDatabaseProvider(context)
                ).also {
                    downloadCache = it
                }
        }
    }

    private fun getDatabaseProvider(
        context: Context
    ): StandaloneDatabaseProvider {
        return databaseProvider ?: synchronized(this) {
            databaseProvider
                ?: StandaloneDatabaseProvider(context)
                    .also {
                        databaseProvider = it
                    }
        }
    }

    private fun getDownloadExecutor(): Executor {
        return downloadExecutor ?: synchronized(this) {
            downloadExecutor
                ?: Executors.newFixedThreadPool(2)
                    .also {
                        downloadExecutor = it
                    }
        }
    }
}