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

@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    onSendMessage: (String, String) -> Unit
) {
    val subject = remember { mutableStateOf("") }
    val message = remember { mutableStateOf("") }
    val showSuccessMessage = remember { mutableStateOf(false) }

    val isFormValid = subject.value.isNotBlank() && message.value.isNotBlank()

    ScreenScaffold(title = "Help & Support") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "How can we help?",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Send us your question and we’ll get back to you as soon as possible.",
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
                        label = { Text("Subject") }
                    )

                    OutlinedTextField(
                        value = message.value,
                        onValueChange = {
                            message.value = it
                            showSuccessMessage.value = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Message") },
                        minLines = 5
                    )

                    PrimaryButton(
                        text = "Send message",
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
                            text = "Your message has been prepared successfully.",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            PrimaryButton(
                text = "Back",
                onClick = onBack
            )
        }
    }
}