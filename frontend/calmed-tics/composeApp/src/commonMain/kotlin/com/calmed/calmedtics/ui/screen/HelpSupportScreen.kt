package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Modifier
import com.calmed.calmedtics.theme.appBackgroundGradient
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calmed.calmedtics.Res
import com.calmed.calmedtics.back_button
import com.calmed.calmedtics.help_support_description
import com.calmed.calmedtics.help_support_heading
import com.calmed.calmedtics.message_label
import com.calmed.calmedtics.message_success
import com.calmed.calmedtics.send_message_button
import com.calmed.calmedtics.subject_label
import com.calmed.calmedtics.ui.component.PrimaryButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    onSendMessage: (String, String) -> Unit
) {
    val subject = remember { mutableStateOf("") }
    val message = remember { mutableStateOf("") }
    val showSuccessMessage = remember { mutableStateOf(false) }

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
                        text = stringResource(Res.string.send_message_button),
                        enabled = isFormValid,
                        onClick = {
                            onSendMessage(subject.value, message.value)
                            showSuccessMessage.value = true
                            subject.value = ""
                            message.value = ""
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
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            PrimaryButton(
                text = stringResource(Res.string.back_button),
                onClick = onBack
            )
        }
    }
}