package com.calmed.calmedfrontendtourettes.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calmed.calmedfrontendtourettes.ui.component.PrimaryButton
import com.calmed.calmedfrontendtourettes.ui.component.SecondaryButton
import com.calmed.calmedfrontendtourettes.ui.component.TextField
import com.calmed.calmedfrontendtourettes.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = koinInject()
) {
    val scope = rememberCoroutineScope()

    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val info by viewModel.info.collectAsState()

    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Forgot password", style = MaterialTheme.typography.headlineMedium)

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        if (info != null) {
            Text(info!!, color = MaterialTheme.colorScheme.primary)
        }

        TextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            singleLine = true,
        )

        PrimaryButton(
            text = if (loading) "Sending..." else "Send reset email",
            onClick = {
                scope.launch {
                    viewModel.forgotPassword(email)
                }
            },
            enabled = !loading
        )

        SecondaryButton(
            text = "Back",
            onClick = onNavigateBack,
            enabled = !loading
        )
    }
}