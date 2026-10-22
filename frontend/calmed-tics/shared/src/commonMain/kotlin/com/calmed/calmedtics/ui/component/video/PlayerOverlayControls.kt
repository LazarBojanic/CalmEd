package com.calmed.calmedtics.ui.component.video

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Hd
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.mute
import calmedtics.shared.generated.resources.resolution
import calmedtics.shared.generated.resources.unmute
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.ui.component.CastButton
import com.calmed.calmedtics.ui.component.VideoOverlayButton
import com.calmed.calmedtics.ui.component.VideoPlayerDownloadButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlayerOverlayControls(
    exercise: ProgramExerciseDto,
    muted: Boolean,
    onToggleMute: () -> Unit,
    onShowQualityPicker: () -> Unit,
    modifier: Modifier = Modifier,
    onInteraction: () -> Unit = {},
) {
    Column(
        modifier = modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    awaitFirstDown(requireUnconsumed = false)
                    onInteraction()
                }
            }
        },
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        VideoPlayerDownloadButton(
            exercise = exercise,
        )

        CastButton()

        VideoOverlayButton(
            icon = Icons.Default.Hd,
            contentDescription = stringResource(Res.string.resolution),
            onClick = onShowQualityPicker,
        )

        VideoOverlayButton(
            icon = if (muted) {
                Icons.AutoMirrored.Filled.VolumeOff
            } else {
                Icons.AutoMirrored.Filled.VolumeUp
            },
            contentDescription = if (muted) {
                stringResource(Res.string.unmute)
            } else {
                stringResource(Res.string.mute)
            },
            onClick = onToggleMute,
        )
    }
}
