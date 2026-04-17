package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.calmed.calmedtics.Res
import com.calmed.calmedtics.`continue`
import com.calmed.calmedtics.dont_show_again
import com.calmed.calmedtics.error_video_failed
import com.calmed.calmedtics.error_video_no_source
import com.calmed.calmedtics.loading_video
import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.settings.AppSettings
import com.calmed.calmedtics.skip
import com.calmed.calmedtics.ui.component.VideoPlayer
import com.calmed.calmedtics.util.getTitle
import com.calmed.calmedtics.util.getVideoURL
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import com.calmed.calmedtics.localization.LocalAppLocale
import com.calmed.calmedtics.localization.customAppLocale
import com.calmed.calmedtics.localization.resolveContentLanguage
import androidx.compose.foundation.layout.navigationBarsPadding

@Composable
fun WelcomeVideoScreen(
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

    val errorNoSource = stringResource(Res.string.error_video_no_source)
    val errorFailed = stringResource(Res.string.error_video_failed)
    val loadingVideo = stringResource(Res.string.loading_video)
    val uiLocaleTag = LocalAppLocale.current

    LaunchedEffect(customAppLocale, uiLocaleTag) {
        error = null
        try {
            val lang = resolveContentLanguage(customAppLocale, uiLocaleTag)
            val welcomeVideo = appApi.getWelcomeVideo()
            videoUrl = welcomeVideo?.getVideoURL(lang)
            title = welcomeVideo?.getTitle(lang)
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
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF7B7DE5),
                            Color(0xFFE5C8E8)
                        )
                    )
                )
                .padding(padding)
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.White.copy(alpha = 0.14f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.25f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = error ?: loadingVideo,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                if (!title.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = title.orEmpty(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.White.copy(alpha = 0.16f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "About the OSSHR method",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "OSSHR is an educational method that the Stošljević family has been developing since 1953. Over this long period, our method has helped more than 100,000 children and adults.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "OSSHR means that the professional applying the method is not limited to predefined procedures when working with children and parents, but is open to using all acquired knowledge in line with the individual needs of the child and family. Although the system itself is open, it is firmly based on scientific theories and facts from both medical and non-medical disciplines related to psychophysiological disorders in a broader sense. The method involves working with children and their parents.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.86f)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "The goal of the method is to help each child reach their maximum potential in thinking, emotions, social development, motor skills, and speech, while also transferring the specialist’s knowledge to parents and guardians so they can support the child’s daily development.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.86f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dontShowAgain = !dontShowAgain },
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.White.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (dontShowAgain) 2.dp else 1.dp,
                        color = if (dontShowAgain) {
                            Color.White.copy(alpha = 0.95f)
                        } else {
                            Color.White.copy(alpha = 0.35f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(Res.string.dont_show_again),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White
                            )
                        )

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = if (dontShowAgain) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(Res.string.skip),
                            color = Color.White
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.White.copy(alpha = 0.28f)
                ) {
                    TextButton(
                        onClick = { onContinue(dontShowAgain) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(Res.string.`continue`),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}