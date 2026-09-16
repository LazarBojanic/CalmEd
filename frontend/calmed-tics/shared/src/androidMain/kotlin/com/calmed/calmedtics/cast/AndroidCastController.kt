package com.calmed.calmedtics.cast

import android.content.Context
import androidx.media3.cast.RemoteCastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class CastLocalState(
	val items: List<MediaItem>,
	val index: Int,
	val positionMs: Long,
)

data class CastResumeTarget(
	val index: Int,
	val positionMs: Long,
)

@OptIn(UnstableApi::class)
class AndroidCastController(context: Context) {

	val remotePlayer: RemoteCastPlayer =
		RemoteCastPlayer.Builder(context.applicationContext).build()

	private val _isCasting = MutableStateFlow(false)
	val isCasting: StateFlow<Boolean> = _isCasting

	var localStateProvider: (() -> CastLocalState?)? = null

	private var resumeTarget: CastResumeTarget? = null

	private val sessionListener = object : SessionAvailabilityListener {
		override fun onCastSessionAvailable() {
			resumeTarget = null
			_isCasting.value = true
			transferToRemote()
		}

		override fun onCastSessionUnavailable() {
			resumeTarget = CastResumeTarget(
				index = remotePlayer.currentMediaItemIndex.coerceAtLeast(0),
				positionMs = remotePlayer.currentPosition.coerceAtLeast(0L),
			)
			_isCasting.value = false
		}
	}

	init {
		remotePlayer.setSessionAvailabilityListener(sessionListener)
	}

	fun consumeResumeTarget(): CastResumeTarget? {
		val target = resumeTarget
		resumeTarget = null
		return target
	}

	fun release() {
		remotePlayer.setSessionAvailabilityListener(null)
		remotePlayer.release()
	}

	private fun transferToRemote() {
		val state = localStateProvider?.invoke() ?: return
		if (state.items.isEmpty()) return

		val index = state.index.coerceIn(0, state.items.lastIndex)
		remotePlayer.setMediaItems(state.items, index, state.positionMs)
		remotePlayer.prepare()
		remotePlayer.playWhenReady = true
	}
}
