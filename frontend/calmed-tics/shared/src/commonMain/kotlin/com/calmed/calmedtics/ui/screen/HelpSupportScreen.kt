package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.calmed.calmedtics.theme.appBackgroundGradient
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.help_support_description
import calmedtics.shared.generated.resources.help_support_heading
import calmedtics.shared.generated.resources.message_label
import calmedtics.shared.generated.resources.message_success
import calmedtics.shared.generated.resources.send_message_button
import calmedtics.shared.generated.resources.sending
import calmedtics.shared.generated.resources.subject_label
import calmedtics.shared.generated.resources.support_send_failed
import com.calmed.calmedtics.ui.component.BackButton
import com.calmed.calmedtics.ui.component.PrimaryButton
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    onSendMessage: suspend (String, String) -> Boolean
) {
    val scope = rememberCoroutineScope()
    val subject = remember { mutableStateOf("") }
    val message = remember { mutableStateOf("") }
    val showSuccessMessage = remember { mutableStateOf(false) }
    val showFailureMessage = remember { mutableStateOf(false) }
    val isSending = remember { mutableStateOf(false) }

    val isFormValid = subject.value.isNotBlank() && message.value.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appBackgroundGradient())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackButton(onClick = onBack)
            }

            Text(
                text = stringResource(Res.string.help_support_heading),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(Res.string.help_support_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = subject.value,
                        onValueChange = {
                            subject.value = it
                            showSuccessMessage.value = false
                            showFailureMessage.value = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                text = stringResource(Res.string.subject_label),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            cursorColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    )

                    OutlinedTextField(
                        value = message.value,
                        onValueChange = {
                            message.value = it
                            showSuccessMessage.value = false
                            showFailureMessage.value = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        label = {
                            Text(
                                text = stringResource(Res.string.message_label),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            cursorColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    )

                    PrimaryButton(
                        text = if (isSending.value) {
                            stringResource(Res.string.sending)
                        } else {
                            stringResource(Res.string.send_message_button)
                        },
                        enabled = isFormValid && !isSending.value,
                        onClick = {
                            val currentSubject = subject.value
                            val currentMessage = message.value
                            scope.launch {
                                isSending.value = true
                                showSuccessMessage.value = false
                                showFailureMessage.value = false
                                val sent = onSendMessage(currentSubject, currentMessage)
                                isSending.value = false
                                if (sent) {
                                    showSuccessMessage.value = true
                                    subject.value = ""
                                    message.value = ""
                                } else {
                                    showFailureMessage.value = true
                                }
                            }
                        }
                    )

                    if (showSuccessMessage.value) {
                        Text(
                            text = stringResource(Res.string.message_success),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (showFailureMessage.value) {
                        Text(
                            text = stringResource(Res.string.support_send_failed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

        }
    }
}