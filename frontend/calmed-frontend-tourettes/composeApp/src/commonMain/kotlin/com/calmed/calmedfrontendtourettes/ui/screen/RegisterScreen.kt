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
import com.calmed.calmedfrontendtourettes.ui.component.PasswordTextField
import com.calmed.calmedfrontendtourettes.ui.component.PrimaryButton
import com.calmed.calmedfrontendtourettes.ui.component.ScreenScaffold
import com.calmed.calmedfrontendtourettes.ui.component.SecondaryButton
import com.calmed.calmedfrontendtourettes.ui.component.TextField
import com.calmed.calmedfrontendtourettes.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun RegisterScreen(
    onNavigateLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = koinInject()
) {
    val scope = rememberCoroutineScope()

    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val info by viewModel.info.collectAsState()

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    ScreenScaffold(title = "Register"){
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

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

            TextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                singleLine = true,
            )

            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                singleLine = true
            )

            PasswordTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm password",
                singleLine = true
            )

            PrimaryButton(
                text = if (loading) "Creating..." else "Register",
                onClick = {
                    scope.launch {
                        val success = viewModel.register(email, username, password, confirmPassword)
                        if (success) {
                            onRegisterSuccess()
                        }
                    }
                },
                enabled = !loading
            )

            SecondaryButton(
                text = "Back to login",
                onClick = onNavigateLogin,
                enabled = !loading
            )
        }
    }


}