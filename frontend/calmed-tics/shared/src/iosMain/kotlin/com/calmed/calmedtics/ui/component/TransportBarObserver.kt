@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.calmed.calmedtics.ui.component

import platform.AVKit.AVPlayerViewController

/**
 * Implemented in Swift. The Kotlin/Native AVKit bindings don't expose
 * [AVPlayerViewControllerDelegate.willTransitionToVisibilityOfTransportBar],
 * so the Swift side attaches itself as the controller's delegate and reports
 * transport-bar visibility back through [TransportBarObserverHolder].
 */
interface TransportBarObserver {
    fun observe(controller: AVPlayerViewController)
}

object TransportBarObserverHolder {
    var observer: TransportBarObserver? = null

    var onVisibilityChanged: ((Boolean) -> Unit)? = null

    fun notifyVisibilityChanged(visible: Boolean) {
        onVisibilityChanged?.invoke(visible)
    }
}
