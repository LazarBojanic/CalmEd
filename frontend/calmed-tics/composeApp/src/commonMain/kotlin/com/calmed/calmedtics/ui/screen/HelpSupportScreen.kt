package com.calmed.calmedtics.ui.screen

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF7B7DE5),
                        Color(0xFFE5C8E8)
                    )
                )
            )
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
                color = Color.White
            )

            Text(
                text = stringResource(Res.string.help_support_description),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.88f)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.16f)
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
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.45f),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.78f),
                            cursorColor = Color.White,
                            focusedContainerColor = Color.White.copy(alpha = 0.08f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.06f)
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
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.45f),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.78f),
                            cursorColor = Color.White,
                            focusedContainerColor = Color.White.copy(alpha = 0.08f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.06f)
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
                            color = Color.White,
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