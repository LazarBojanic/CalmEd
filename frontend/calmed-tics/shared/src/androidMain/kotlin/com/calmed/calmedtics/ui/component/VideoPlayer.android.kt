package com.calmed.calmedtics.ui.component

import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.calmed.calmedtics.cast.AndroidCastController
import com.calmed.calmedtics.cast.CastLocalState
import com.calmed.calmedtics.logging.isDevelopment
import com.calmed.calmedtics.service.specification.IVideoDownloadManager
import com.calmed.calmedtics.service.specification.VideoDownloadStatus
import com.calmed.calmedtics.service.specification.stateFor
import com.calmed.calmedtics.video.VideoQuality
import com.mux.player.MuxPlayer
import com.mux.player.media.MediaItems
import com.mux.player.media.PlaybackResolution
import org.koin.compose.koinInject

@OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayer(
	items: List<VideoItem>,
	startIndex: Int,
	modifier: Modifier,
	quality: VideoQuality,
	muted: Boolean,
	autoPlay: Boolean,
	allowFullscreen: Boolean,
	isFullscreen: Boolean,
	onFullscreenToggle: (Boolean) -> Unit,
	onIndexChanged: (Int) -> Unit,
) {
	val context = LocalContext.current
	val activity = LocalActivity.current
	val downloadManager: IVideoDownloadManager = koinInject()
	val castController: AndroidCastController = koinInject()
	val isCasting by castController.isCasting.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
	val currentOnIndexChanged by rememberUpdatedState(onIndexChanged)
	val currentOnFullscreenToggle by rememberUpdatedState(onFullscreenToggle)
	val latestStartIndex by rememberUpdatedState(startIndex)

	val muxPlayer = remember {
		MuxPlayer.Builder(context = context)
			.enableLogcat(isDevelopment)
			.enableSmartCache(true)
			.applyExoConfig {
				setHandleAudioBecomingNoisy(true)
			}
			.build()
	}

	DisposableEffect(muxPlayer) {
		onDispose { muxPlayer.release() }
	}

	LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
		muxPlayer.pause()
		castController.remotePlayer.pause()
	}

	val windowInfo = LocalWindowInfo.current

	LaunchedEffect(
		isFullscreen,
		activity,
		windowInfo.containerSize,
		windowInfo.isWindowFocused,
	) {
		val window = activity?.window ?: return@LaunchedEffect
		val controller = WindowCompat.getInsetsController(window, window.decorView)

		if (isFullscreen) {
			activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
			controller.systemBarsBehavior =
				WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
			controller.hide(WindowInsetsCompat.Type.systemBars())
		} else {
			activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
			controller.show(WindowInsetsCompat.Type.systemBars())
		}
	}

	DisposableEffect(activity) {
		onDispose {
			if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
				activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
				activity.window?.let { window ->
					WindowCompat.getInsetsController(window, window.decorView)
						.show(WindowInsetsCompat.Type.systemBars())
				}
			}
		}
	}

	val mediaItems = remember(items, quality) {
		items.map { item ->
			val downloaded =
				downloadManager.states.value.stateFor(item.playbackId).status ==
					VideoDownloadStatus.Downloaded

			if (downloaded) {
				MediaItems.forMuxDownload(item.playbackId, item.title)
			} else {
				item.toMuxMediaItem(quality)
			}
		}
	}

	val castMediaItems = remember(items, quality) {
		items.map { item -> item.toMuxMediaItem(quality, forCast = true) }
	}

	LaunchedEffect(muxPlayer, mediaItems) {
		if (mediaItems.isEmpty()) return@LaunchedEffect

		val resuming = muxPlayer.mediaItemCount > 0 &&
			muxPlayer.playbackState != Player.STATE_IDLE
		val index = if (resuming) muxPlayer.currentMediaItemIndex else latestStartIndex
		val position = if (resuming) muxPlayer.currentPosition else 0L

		muxPlayer.setMediaItems(mediaItems, index.coerceIn(0, mediaItems.lastIndex), position)
		muxPlayer.prepare()
		muxPlayer.playWhenReady = autoPlay
	}

	LaunchedEffect(muxPlayer, muted) {
		muxPlayer.volume = if (muted) 0f else 1f
	}

	DisposableEffect(castMediaItems, muxPlayer) {
		castController.localStateProvider = {
			CastLocalState(
				items = castMediaItems,
				index = if (muxPlayer.mediaItemCount > 0) {
					muxPlayer.currentMediaItemIndex
				} else {
					latestStartIndex
				},
				positionMs = if (muxPlayer.mediaItemCount > 0) {
					muxPlayer.currentPosition.coerceAtLeast(0L)
				} else {
					0L
				},
			)
		}
		onDispose { castController.localStateProvider = null }
	}

	LaunchedEffect(isCasting) {
		if (isCasting) {
			muxPlayer.pause()
		} else {
			val target = castController.consumeResumeTarget()
			if (target != null && muxPlayer.mediaItemCount > 0) {
				muxPlayer.seekTo(
					target.index.coerceIn(0, muxPlayer.mediaItemCount - 1),
					target.positionMs,
				)
				muxPlayer.play()
			}
		}
	}

	val activePlayer: Player = if (isCasting) castController.remotePlayer else muxPlayer

	DisposableEffect(activePlayer, currentOnIndexChanged) {
		val listener = object : Player.Listener {
			override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
				currentOnIndexChanged(activePlayer.currentMediaItemIndex)
			}
		}
		activePlayer.addListener(listener)
		onDispose { activePlayer.removeListener(listener) }
	}

	AndroidView(
		factory = { ctx ->
			PlayerView(ctx).apply {
				layoutParams = FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT
				)
				setBackgroundColor(android.graphics.Color.BLACK)
				setShutterBackgroundColor(android.graphics.Color.BLACK)
				useController = true
				setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
				if (allowFullscreen) {
					setFullscreenButtonClickListener { shouldBeFullscreen ->
						currentOnFullscreenToggle(shouldBeFullscreen)
					}
				} else {
					setFullscreenButtonClickListener(null)
				}
				player = activePlayer
			}
		},
		update = { view ->
			view.player = activePlayer
			view.setFullscreenButtonState(isFullscreen)
		},
		modifier = modifier,
	)
}

private fun VideoItem.toMuxMediaItem(
	quality: VideoQuality,
	forCast: Boolean = false,
): MediaItem {
	val builder = MediaItems.builderFromMuxPlaybackId(
		playbackId = playbackId,
		maxResolution = quality.toPlaybackResolution(),
		playbackToken = playbackToken,
	)
		.setMediaMetadata(
			MediaMetadata.Builder()
				.setTitle(title)
				.build()
		)

	if (forCast) {
		builder.setMimeType(MimeTypes.APPLICATION_M3U8)
	}

	return builder.build()
}

private fun VideoQuality.toPlaybackResolution(): PlaybackResolution = when (this) {
	VideoQuality.R480 -> PlaybackResolution.LD_480
	VideoQuality.R720 -> PlaybackResolution.HD_720
	VideoQuality.R1080 -> PlaybackResolution.FHD_1080
}
