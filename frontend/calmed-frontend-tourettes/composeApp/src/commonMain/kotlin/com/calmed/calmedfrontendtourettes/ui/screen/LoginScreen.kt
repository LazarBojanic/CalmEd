package com.calmed.calmedfrontendtourettes.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.calmed.calmedfrontendtourettes.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable


fun LoginScreen(
    onNavigateRegister: () -> Unit,
    onNavigateForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    onGoogleSignIn: () -> Unit,
    viewModel: AuthViewModel = koinInject()
) {
    val scope = rememberCoroutineScope()

    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Button(
            onClick = {
                scope.launch {
                    val success = viewModel.login(email, password)
                    if (success) {
                        onLoginSuccess()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            if (loading) {
                Text("Logging in...")
            } else {
                Text("Login")
            }
        }
        Button(
            onClick = onGoogleSignIn,
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text("Continue with Google")
        }

        TextButton(
            onClick = onNavigateForgotPassword,
            enabled = !loading
        ) {
            Text("Forgot password?")
        }

        TextButton(
            onClick = onNavigateRegister,
            enabled = !loading
        ) {
            Text("Create account")
        }
    }
}