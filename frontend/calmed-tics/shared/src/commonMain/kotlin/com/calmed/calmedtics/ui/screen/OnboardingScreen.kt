package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material.icons.outlined.SentimentVeryDissatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.age_title
import calmedtics.shared.generated.resources.back
import calmedtics.shared.generated.resources.finish
import calmedtics.shared.generated.resources.frequency_daily
import calmedtics.shared.generated.resources.frequency_moderate
import calmedtics.shared.generated.resources.frequency_rare
import calmedtics.shared.generated.resources.goal_example
import calmedtics.shared.generated.resources.goal_input_label
import calmedtics.shared.generated.resources.goal_question
import calmedtics.shared.generated.resources.goal_title
import calmedtics.shared.generated.resources.next
import calmedtics.shared.generated.resources.onboarding_questions_intro
import calmedtics.shared.generated.resources.personalize_experience
import calmedtics.shared.generated.resources.preferred_name_description
import calmedtics.shared.generated.resources.preferred_name_title
import calmedtics.shared.generated.resources.skip
import calmedtics.shared.generated.resources.start
import calmedtics.shared.generated.resources.stress_level_question
import calmedtics.shared.generated.resources.tailor_app_experience
import calmedtics.shared.generated.resources.tics_both
import calmedtics.shared.generated.resources.tics_both_description
import calmedtics.shared.generated.resources.tics_frequency_daily_description
import calmedtics.shared.generated.resources.tics_frequency_moderate_description
import calmedtics.shared.generated.resources.tics_frequency_question
import calmedtics.shared.generated.resources.tics_frequency_rare_description
import calmedtics.shared.generated.resources.tics_frequency_title
import calmedtics.shared.generated.resources.tics_motor
import calmedtics.shared.generated.resources.tics_motor_description
import calmedtics.shared.generated.resources.tics_type_title
import calmedtics.shared.generated.resources.tics_vocal
import calmedtics.shared.generated.resources.tics_vocal_description
import calmedtics.shared.generated.resources.welcome_user
import com.calmed.calmedtics.*
import com.calmed.calmedtics.model.dto.request.UserInfoTicsUpdateDto
import com.calmed.calmedtics.model.joined.UserInfoTicsJoined
import com.calmed.calmedtics.model.joined.UserJoined
import com.calmed.calmedtics.model.raw.TicDuration
import com.calmed.calmedtics.model.raw.TickFrequency
import com.calmed.calmedtics.model.raw.TickType
import com.calmed.calmedtics.theme.appBackgroundGradient
import com.calmed.calmedtics.ui.component.TextField
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    user: UserJoined,
    userInfo: UserInfoTicsJoined,
    onSkip: () -> Unit,
    onFinished: (UserInfoTicsUpdateDto) -> Unit
) {
    val step = remember { mutableIntStateOf(0) }
    val preferredName = remember { mutableStateOf(userInfo.preferredName ?: "") }
    val age = remember { mutableIntStateOf(userInfo.age ?: 18) }
    val stress = remember { mutableIntStateOf(userInfo.stressLevel ?: 5) }
    val tickType = remember { mutableStateOf(userInfo.tickType ?: TickType.BOTH) }
    val tickFrequency =
        remember { mutableStateOf(userInfo.tickFrequency ?: TickFrequency.MODERATE) }
    val ticDuration = remember {
        mutableStateOf(userInfo.ticDuration ?: TicDuration.ZERO_TO_ONE_YEAR)
    }
    val goal = remember { mutableStateOf(userInfo.goal ?: "") }
    val followProgress = remember { mutableStateOf(userInfo.followProgress ?: true) }
    fun buildUpdate(): UserInfoTicsUpdateDto {
        return UserInfoTicsUpdateDto(
            userId = user.id,
            preferredName = preferredName.value.trim(),
            age = age.intValue,
            stressLevel = stress.intValue,
            tickType = tickType.value,
            tickFrequency = tickFrequency.value,
            ticDuration = ticDuration.value,
            goal = goal.value.trim(),
            followProgress = followProgress.value
        )
    }
    @Composable
    fun RadioOptionRow(
        text: String,
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            border = BorderStroke(
                width = 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selected,
                        onClick = onClick,
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selected, onClick = null)
                Spacer(Modifier.width(10.dp))
                Text(text = text, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appBackgroundGradient())
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            when (step.intValue) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.large,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    TextButton(onClick = onSkip) {
                                        Text(
                                            text = stringResource(Res.string.skip),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(48.dp))

                            Text(
                                text = stringResource(Res.string.welcome_user, user.username),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(Res.string.personalize_experience),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(48.dp))

                            Surface(
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = stringResource(Res.string.onboarding_questions_intro),
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(5) { index ->
                                    Surface(
                                        modifier = Modifier
                                            .width(if (index == 0) 18.dp else 8.dp)
                                            .height(8.dp),
                                        shape = MaterialTheme.shapes.large,
                                        color = if (index == 0) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                        }
                                    ) {}
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                TextButton(
                                    onClick = { step.intValue = 1 },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(Res.string.start),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { step.intValue = 0 }
                                ) {
                                    Text(
                                        text = stringResource(Res.string.back),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = MaterialTheme.shapes.large,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    TextButton(onClick = onSkip) {
                                        Text(
                                            text = stringResource(Res.string.skip),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(48.dp))

                            Text(
                                text = stringResource(Res.string.preferred_name_title),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = stringResource(Res.string.preferred_name_description),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(40.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    TextField(
                                        value = preferredName.value,
                                        onValueChange = { preferredName.value = it },
                                        label = "",
                                        singleLine = true
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(5) { index ->
                                    Surface(
                                        modifier = Modifier
                                            .width(if (index == 1) 18.dp else 8.dp)
                                            .height(8.dp),
                                        shape = MaterialTheme.shapes.large,
                                        color = if (index == 1) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                        }
                                    ) {}
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                TextButton(
                                    onClick = { step.intValue = 2 },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(Res.string.next),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { step.intValue = 1 }
                                ) {
                                    Text(
                                        text = stringResource(Res.string.back),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = MaterialTheme.shapes.large,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    TextButton(
                                        onClick = { step.intValue = 3 }
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.next),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = stringResource(Res.string.age_title),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Spacer(modifier = Modifier.height(40.dp))

                            Box(
                                modifier = Modifier.height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val ages = 18..125
                                val initialIndex = (age.intValue - ages.first)
                                    .coerceIn(0, ages.count() - 1)

                                val listState = rememberLazyListState(
                                    initialFirstVisibleItemIndex = initialIndex
                                )

                                val snapBehavior = rememberSnapFlingBehavior(listState)

                                LaunchedEffect(listState) {
                                    snapshotFlow {
                                        val layoutInfo = listState.layoutInfo
                                        val viewportCenter =
                                            (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2

                                        layoutInfo.visibleItemsInfo
                                            .minByOrNull { item ->
                                                abs((item.offset + item.size / 2) - viewportCenter)
                                            }
                                            ?.index
                                    }.collect { index ->
                                        if (index != null) {
                                            age.intValue = ages.first + index
                                        }
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth(0.45f)
                                        .height(64.dp),
                                    shape = MaterialTheme.shapes.large,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                    border = BorderStroke(
                                        1.5.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                ) {}

                                LazyColumn(
                                    state = listState,
                                    flingBehavior = snapBehavior,
                                    modifier = Modifier.height(180.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    contentPadding = PaddingValues(vertical = 60.dp)
                                ) {
                                    items(ages.count()) { index ->
                                        val value = ages.first + index
                                        val selected = value == age.intValue

                                        Box(
                                            modifier = Modifier
                                                .height(60.dp)
                                                .fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = value.toString(),
                                                style = if (selected) {
                                                    MaterialTheme.typography.displayLarge.copy(
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                } else {
                                                    MaterialTheme.typography.headlineMedium.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                            alpha = 0.45f
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(5) { index ->
                                    Surface(
                                        modifier = Modifier
                                            .width(if (index == 1) 18.dp else 8.dp)
                                            .height(8.dp),
                                        shape = MaterialTheme.shapes.large,
                                        color = if (index == 1) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                        }
                                    ) {}
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = stringResource(Res.string.tailor_app_experience),
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                3 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { step.intValue = 2 }) {
                                    Text(
                                        text = stringResource(Res.string.back),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = MaterialTheme.shapes.large,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    TextButton(onClick = { step.intValue = 4 }) {
                                        Text(
                                            text = stringResource(Res.string.next),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(48.dp))

                            Text(
                                text = stringResource(Res.string.stress_level_question),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Spacer(modifier = Modifier.height(56.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.SentimentSatisfiedAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(28.dp)
                                    )

                                    Icon(
                                        imageVector = Icons.Outlined.SentimentVeryDissatisfied,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .align(Alignment.Center)
                                            .background(
                                                color = MaterialTheme.colorScheme.outlineVariant,
                                                shape = CircleShape
                                            )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        (0..10).forEach { value ->
                                            val selected = stress.intValue == value

                                            Box(
                                                modifier = Modifier
                                                    .size(if (selected) 22.dp else 14.dp)
                                                    .background(
                                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                        shape = CircleShape
                                                    )
                                                    .border(
                                                        width = if (selected) 3.dp else 1.5.dp,
                                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                        shape = CircleShape
                                                    )
                                                    .clickable { stress.intValue = value }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    (0..10).forEach { value ->
                                        Text(
                                            text = value.toString(),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(5) { index ->
                                    Surface(
                                        modifier = Modifier
                                            .width(if (index == 2) 18.dp else 8.dp)
                                            .height(8.dp),
                                        shape = MaterialTheme.shapes.large,
                                        color = if (index == 2) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                        }
                                    ) {}
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = stringResource(Res.string.tailor_app_experience),
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                4 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { step.intValue = 3 }) {
                                    Text(
                                        text = stringResource(Res.string.back),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = MaterialTheme.shapes.large,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    TextButton(onClick = { step.intValue = 5 }) {
                                        Text(
                                            text = stringResource(Res.string.next),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(40.dp))

                            Text(
                                text = stringResource(Res.string.tics_type_title),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { tickType.value = TickType.MOTOR },
                                    shape = MaterialTheme.shapes.extraLarge,
                                    color = if (tickType.value == TickType.MOTOR) {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.25f)
                                    },
                                    border = BorderStroke(
                                        width = if (tickType.value == TickType.MOTOR) 2.dp else 1.dp,
                                        color = if (tickType.value == TickType.MOTOR) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 18.dp, vertical = 18.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = stringResource(Res.string.tics_motor),
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = stringResource(Res.string.tics_motor_description),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(
                                                    color = if (tickType.value == TickType.MOTOR) {
                                                        MaterialTheme.colorScheme.onSurface
                                                    } else {
                                                        Color.Transparent
                                                    },
                                                    shape = CircleShape
                                                )
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { tickType.value = TickType.VOCAL },
                                    shape = MaterialTheme.shapes.extraLarge,
                                    color = if (tickType.value == TickType.VOCAL) {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.25f)
                                    },
                                    border = BorderStroke(
                                        width = if (tickType.value == TickType.VOCAL) 2.dp else 1.dp,
                                        color = if (tickType.value == TickType.VOCAL) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 18.dp, vertical = 18.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = stringResource(Res.string.tics_vocal),
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = stringResource(Res.string.tics_vocal_description),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(
                                                    color = if (tickType.value == TickType.VOCAL) {
                                                        MaterialTheme.colorScheme.onSurface
                                                    } else {
                                                        Color.Transparent
                                                    },
                                                    shape = CircleShape
                                                )
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { tickType.value = TickType.BOTH },
                                    shape = MaterialTheme.shapes.extraLarge,
                                    color = if (tickType.value == TickType.BOTH) {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.25f)
                                    },
                                    border = BorderStroke(
                                        width = if (tickType.value == TickType.BOTH) 2.dp else 1.dp,
                                        color = if (tickType.value == TickType.BOTH) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 18.dp, vertical = 18.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = stringResource(Res.string.tics_both),
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = stringResource(Res.string.tics_both_description),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(
                                                    color = if (tickType.value == TickType.BOTH) {
                                                        MaterialTheme.colorScheme.onSurface
                                                    } else {
                                                        Color.Transparent
                                                    },
                                                    shape = CircleShape
                                                )
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }


                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(5) { index ->
                                    Surface(
                                        modifier = Modifier
                                            .width(if (index == 3) 18.dp else 8.dp)
                                            .height(8.dp),
                                        shape = MaterialTheme.shapes.large,
                                        color = if (index == 3) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                        }
                                    ) {}
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = stringResource(Res.string.tailor_app_experience),
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                5 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { step.intValue = 4 }) {
                                    Text(
                                        text = stringResource(Res.string.back),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = MaterialTheme.shapes.large,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    TextButton(onClick = { step.intValue = 6 }) {
                                        Text(
                                            text = stringResource(Res.string.next),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(40.dp))

                            Text(
                                text = stringResource(Res.string.tics_frequency_title),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(Res.string.tics_frequency_question),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                )
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { tickFrequency.value = TickFrequency.RARE },
                                    shape = MaterialTheme.shapes.extraLarge,
                                    color = if (tickFrequency.value == TickFrequency.RARE) {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.25f)
                                    },
                                    border = BorderStroke(
                                        width = if (tickFrequency.value == TickFrequency.RARE) 2.dp else 1.dp,
                                        color = if (tickFrequency.value == TickFrequency.RARE) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 18.dp, vertical = 18.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = stringResource(Res.string.frequency_rare),
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = stringResource(Res.string.tics_frequency_rare_description),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(
                                                    color = if (tickFrequency.value == TickFrequency.RARE) {
                                                        MaterialTheme.colorScheme.onSurface
                                                    } else {
                                                        Color.Transparent
                                                    },
                                                    shape = CircleShape
                                                )
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { tickFrequency.value = TickFrequency.MODERATE },
                                    shape = MaterialTheme.shapes.extraLarge,
                                    color = if (tickFrequency.value == TickFrequency.MODERATE) {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.25f)
                                    },
                                    border = BorderStroke(
                                        width = if (tickFrequency.value == TickFrequency.MODERATE) 2.dp else 1.dp,
                                        color = if (tickFrequency.value == TickFrequency.MODERATE) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 18.dp, vertical = 18.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = stringResource(Res.string.frequency_moderate),
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = stringResource(Res.string.tics_frequency_moderate_description),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(
                                                    color = if (tickFrequency.value == TickFrequency.MODERATE) {
                                                        MaterialTheme.colorScheme.onSurface
                                                    } else {
                                                        Color.Transparent
                                                    },
                                                    shape = CircleShape
                                                )
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { tickFrequency.value = TickFrequency.DAILY },
                                    shape = MaterialTheme.shapes.extraLarge,
                                    color = if (tickFrequency.value == TickFrequency.DAILY) {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.25f)
                                    },
                                    border = BorderStroke(
                                        width = if (tickFrequency.value == TickFrequency.DAILY) 2.dp else 1.dp,
                                        color = if (tickFrequency.value == TickFrequency.DAILY) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 18.dp, vertical = 18.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = stringResource(Res.string.frequency_daily),
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = stringResource(Res.string.tics_frequency_daily_description),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(
                                                    color = if (tickFrequency.value == TickFrequency.DAILY) {
                                                        MaterialTheme.colorScheme.onSurface
                                                    } else {
                                                        Color.Transparent
                                                    },
                                                    shape = CircleShape
                                                )
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(5) { index ->
                                    Surface(
                                        modifier = Modifier
                                            .width(if (index == 4) 18.dp else 8.dp)
                                            .height(8.dp),
                                        shape = MaterialTheme.shapes.large,
                                        color = if (index == 4) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                        }
                                    ) {}
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = stringResource(Res.string.tailor_app_experience),
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                6 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { step.intValue = 5 }) {
                                    Text(
                                        text = stringResource(Res.string.back),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = MaterialTheme.shapes.large,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    TextButton(onClick = { step.intValue = 7 }) {
                                        Text(
                                            text = stringResource(Res.string.next),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(40.dp))

                            Text(
                                text = "How long have you had tics?",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { ticDuration.value = TicDuration.ZERO_TO_ONE_YEAR },
                                    shape = MaterialTheme.shapes.extraLarge,
                                    color = if (ticDuration.value == TicDuration.ZERO_TO_ONE_YEAR) {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.25f)
                                    },
                                    border = BorderStroke(
                                        width = if (ticDuration.value == TicDuration.ZERO_TO_ONE_YEAR) 2.dp else 1.dp,
                                        color = if (ticDuration.value == TicDuration.ZERO_TO_ONE_YEAR) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 18.dp, vertical = 20.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "0–1 year",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(
                                                    color = if (ticDuration.value == TicDuration.ZERO_TO_ONE_YEAR) {
                                                        MaterialTheme.colorScheme.onSurface
                                                    } else {
                                                        Color.Transparent
                                                    },
                                                    shape = CircleShape
                                                )
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { ticDuration.value = TicDuration.ONE_TO_THREE_YEARS },
                                    shape = MaterialTheme.shapes.extraLarge,
                                    color = if (ticDuration.value == TicDuration.ONE_TO_THREE_YEARS) {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.25f)
                                    },
                                    border = BorderStroke(
                                        width = if (ticDuration.value == TicDuration.ONE_TO_THREE_YEARS) 2.dp else 1.dp,
                                        color = if (ticDuration.value == TicDuration.ONE_TO_THREE_YEARS) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 18.dp, vertical = 20.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "1–3 years",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(
                                                    color = if (ticDuration.value == TicDuration.ONE_TO_THREE_YEARS) {
                                                        MaterialTheme.colorScheme.onSurface
                                                    } else {
                                                        Color.Transparent
                                                    },
                                                    shape = CircleShape
                                                )
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { ticDuration.value = TicDuration.THREE_PLUS_YEARS },
                                    shape = MaterialTheme.shapes.extraLarge,
                                    color = if (ticDuration.value == TicDuration.THREE_PLUS_YEARS) {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.25f)
                                    },
                                    border = BorderStroke(
                                        width = if (ticDuration.value == TicDuration.THREE_PLUS_YEARS) 2.dp else 1.dp,
                                        color = if (ticDuration.value == TicDuration.THREE_PLUS_YEARS) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 18.dp, vertical = 20.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "3+ years",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(
                                                    color = if (ticDuration.value == TicDuration.THREE_PLUS_YEARS) {
                                                        MaterialTheme.colorScheme.onSurface
                                                    } else {
                                                        Color.Transparent
                                                    },
                                                    shape = CircleShape
                                                )
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(5) { index ->
                                    Surface(
                                        modifier = Modifier
                                            .width(if (index == 4) 18.dp else 8.dp)
                                            .height(8.dp),
                                        shape = MaterialTheme.shapes.large,
                                        color = if (index == 4) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                        }
                                    ) {}
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = stringResource(Res.string.tailor_app_experience),
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }             7 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { step.intValue = 6 }) {
                                Text(
                                    text = stringResource(Res.string.back),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            TextButton(onClick = onSkip) {
                                Text(
                                    text = stringResource(Res.string.skip),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        Text(
                            text = stringResource(Res.string.goal_title),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(Res.string.goal_question),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            )
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp)
                            ) {
                                Text(
                                    text = stringResource(Res.string.goal_input_label),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                TextField(
                                    value = goal.value,
                                    onValueChange = { goal.value = it },
                                    label = "",
                                    singleLine = false,
                                    modifier = Modifier.height(180.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = stringResource(Res.string.goal_example),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                )
                            )
                        }
                    }


                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(5) { index ->
                                Surface(
                                    modifier = Modifier
                                        .width(if (index == 4) 18.dp else 8.dp)
                                        .height(8.dp),
                                    shape = MaterialTheme.shapes.large,
                                    color = if (index == 4) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                    }
                                ) {}
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            TextButton(
                                onClick = { onFinished(buildUpdate()) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(Res.string.finish),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }


            }
        }
    }
}
