package com.calmed.calmedtics.ui.component

import androidx.compose.ui.unit.Dp

/**
 * Extra vertical inset applied to the top-right overlay button column in
 * [VideoScreen] so it clears any player-level overlay button rendered in the
 * same corner (the download button on iOS).
 *
 * Android has no player-level button in that corner (its download button
 * lives inside the native Media3 control row), so its inset is the plain
 * corner padding.
 */
expect val PlayerTopOverlayInset: Dp
