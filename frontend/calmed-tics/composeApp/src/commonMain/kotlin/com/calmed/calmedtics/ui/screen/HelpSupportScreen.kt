package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calmed.calmedtics.ui.component.PrimaryButton
import com.calmed.calmedtics.ui.component.ScreenScaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.calmed.calmedtics.Res
import com.calmed.calmedtics.back_button
import com.calmed.calmedtics.help_support_description
import com.calmed.calmedtics.help_support_heading
import com.calmed.calmedtics.help_support_title
import com.calmed.calmedtics.message_label
import com.calmed.calmedtics.message_success
import com.calmed.calmedtics.send_message_button
import com.calmed.calmedtics.subject_label
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

    ScreenScaffold(title = stringResource(Res.string.help_support_title)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.help_support_heading),
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = stringResource(Res.string.help_support_description),
                style = MaterialTheme.typography.bodyMedium
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    OutlinedTextField(
                        value = subject.value,
                        onValueChange = {
                            subject.value = it
                            showSuccessMessage.value = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.subject_label)) }
                    )

                    OutlinedTextField(
                        value = message.value,
                        onValueChange = {
                            message.value = it
                            showSuccessMessage.value = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.message_label)) },
                        minLines = 5
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
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            PrimaryButton(
                text = stringResource(Res.string.back_button),
                onClick = onBack
            )
        }
    }
}