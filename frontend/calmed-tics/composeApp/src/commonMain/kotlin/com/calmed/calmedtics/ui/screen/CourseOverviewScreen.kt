package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calmed.calmedtics.Res
import com.calmed.calmedtics.`continue`
import com.calmed.calmedtics.dont_show_again
import com.calmed.calmedtics.error_video_failed
import com.calmed.calmedtics.error_video_no_source
import com.calmed.calmedtics.error_video_not_available
import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.loading_video
import com.calmed.calmedtics.skip
import com.calmed.calmedtics.ui.component.VideoPlayer
import com.calmed.calmedtics.settings.AppSettings
import com.calmed.calmedtics.util.getTitle
import com.calmed.calmedtics.util.getVideoURL
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import com.calmed.calmedtics.localization.LocalAppLocale
import com.calmed.calmedtics.localization.customAppLocale
import com.calmed.calmedtics.localization.resolveContentLanguage

@Composable
fun CourseOverviewScreen(
    onSkip: () -> Unit,
    onContinue: (Boolean) -> Unit,
    onOpenFullscreen: (String) -> Unit
) {
    val appApi: IAppApi = koinInject()
    val appSettings: AppSettings = koinInject()
    var dontShowAgain by remember { mutableStateOf(false) }

    var videoUrl by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val errorNotAvailable = stringResource(Res.string.error_video_not_available)
    val errorNoSource = stringResource(Res.string.error_video_no_source)
    val errorFailed = stringResource(Res.string.error_video_failed)
    val loadingVideo = stringResource(Res.string.loading_video)
    val uiLocaleTag = LocalAppLocale.current

    LaunchedEffect(customAppLocale, uiLocaleTag) {
        error = null
        try {
            val lang = resolveContentLanguage(customAppLocale, uiLocaleTag)
            val video = appApi.getCourseOverviewVideo()
            videoUrl = video?.getVideoURL(lang)
            title = video?.getTitle(lang)
            if (videoUrl == null) {
                error = errorNoSource
            }
        } catch (t: Throwable) {
            error = t.message ?: errorFailed
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            val url = videoUrl
            if (url != null) {
                VideoPlayer(
                    hlsUrl = url,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    onFullscreenToggle = { onOpenFullscreen(url) }
                )
            } else {
                Text(error ?: loadingVideo)
            }

            Text(
                title ?: "",
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.titleLarge
            )

            Row(modifier = Modifier.padding(top = 16.dp)) {
                Checkbox(
                    checked = dontShowAgain,
                    onCheckedChange = { dontShowAgain = it }
                )
                Text(
                    stringResource(Res.string.dont_show_again),
                    modifier = Modifier.padding(start = 8.dp, top = 12.dp)
                )
            }

            Button(
                onClick = onSkip,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(stringResource(Res.string.skip))
            }

            Button(
                onClick = { onContinue(dontShowAgain) },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(stringResource((Res.string.`continue`)))
            }
        }
    }
}
