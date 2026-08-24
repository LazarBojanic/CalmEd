package com.calmed.calmedtics.ui.component

import android.view.ContextThemeWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.calmed.calmedtics.shared.R
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import android.net.Uri
import android.util.Log
import android.os.Handler
import android.os.Looper
import com.google.android.gms.cast.HlsSegmentFormat
import com.google.android.gms.cast.HlsVideoSegmentFormat

@Composable
actual fun CastButton(
    hlsUrl: String,
    title: String,
    modifier: Modifier
) {
    val context = LocalContext.current
    val castContext = remember(context) {
        CastContext.getSharedInstance(context)
    }

    val currentUrl by rememberUpdatedState(hlsUrl)
    val currentTitle by rememberUpdatedState(title)

    fun loadVideo(session: CastSession?) {
        if (session == null || currentUrl.isBlank()) return

        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
            putString(MediaMetadata.KEY_TITLE, currentTitle)
        }

        val mediaInfo = MediaInfo.Builder(currentUrl)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("application/x-mpegurl")
            .setHlsSegmentFormat(HlsSegmentFormat.FMP4)
            .setHlsVideoSegmentFormat(HlsVideoSegmentFormat.FMP4)
            .setMetadata(metadata)
            .build()

        val request = MediaLoadRequestData.Builder()
            .setMediaInfo(mediaInfo)
            .setAutoplay(true)
            .build()

        val client = session.remoteMediaClient

        if (client == null) {
            Log.e("CAST_DEBUG", "RemoteMediaClient is null")
            return
        }

        val uri = Uri.parse(currentUrl)

        Log.d(
            "CAST_DEBUG",
            "Trying to cast host=${uri.host}, path=${uri.path}"
        )

        client.load(request).setResultCallback { result ->
            Log.d(
                "CAST_DEBUG",
                "LOAD RESULT success=${result.status.isSuccess}, " +
                        "code=${result.status.statusCode}, " +
                        "message=${result.status.statusMessage}, " +
                        "mediaError=${result.mediaError}"
            )
        }
        Handler(Looper.getMainLooper()).postDelayed({
            val status = client.mediaStatus

            Log.d(
                "CAST_DEBUG",
                "AFTER 3s: " +
                        "playerState=${status?.playerState}, " +
                        "idleReason=${status?.idleReason}, " +
                        "currentItemId=${status?.currentItemId}"
            )
        }, 3000)
    }

    DisposableEffect(castContext) {
        val listener = object : SessionManagerListener<CastSession> {

            override fun onSessionStarted(
                session: CastSession,
                sessionId: String
            ) {
                loadVideo(session)
            }

            override fun onSessionResumed(
                session: CastSession,
                wasSuspended: Boolean
            ) {
                loadVideo(session)
            }

            override fun onSessionStarting(session: CastSession) = Unit

            override fun onSessionStartFailed(
                session: CastSession,
                error: Int
            ) = Unit

            override fun onSessionEnding(session: CastSession) = Unit

            override fun onSessionEnded(
                session: CastSession,
                error: Int
            ) = Unit

            override fun onSessionResuming(
                session: CastSession,
                sessionId: String
            ) = Unit

            override fun onSessionResumeFailed(
                session: CastSession,
                error: Int
            ) = Unit

            override fun onSessionSuspended(
                session: CastSession,
                reason: Int
            ) = Unit
        }

        castContext.sessionManager.addSessionManagerListener(
            listener,
            CastSession::class.java
        )

        onDispose {
            castContext.sessionManager.removeSessionManagerListener(
                listener,
                CastSession::class.java
            )
        }
    }

    LaunchedEffect(hlsUrl, title) {
        val session = castContext.sessionManager.currentCastSession
        if (session?.isConnected == true) {
            loadVideo(session)
        }
    }

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f)
            ),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MediaRouteButton(
                    ContextThemeWrapper(
                        ctx,
                        R.style.CalmEdCastButtonTheme
                    )
                ).apply {
                    CastButtonFactory.setUpMediaRouteButton(ctx, this)
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                }
            }
        )
    }
}