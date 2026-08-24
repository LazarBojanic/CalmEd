package com.calmed.calmedtics.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A small circular overlay button used on top of the video player.
 *
 * Each platform renders it to match its native player buttons:
 *  - Android: Material surface circle (fits next to the Media3 controls).
 *  - iOS: dark translucent circle with a white icon (fits next to the
 *    AVKit playback controls).
 */
@Composable
expect fun VideoOverlayButton(
    icon: ImageVector,
    contentDescription: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
