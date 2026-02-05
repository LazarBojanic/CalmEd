package com.calmed.calmedfrontendtourettes.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.calmed.calmedfrontendtourettes.ui.component.GoogleSignInButton
import com.calmed.calmedfrontendtourettes.ui.component.AppleSignInButton
import com.calmed.calmedfrontendtourettes.ui.component.PasswordTextField
import com.calmed.calmedfrontendtourettes.ui.component.PrimaryButton
import com.calmed.calmedfrontendtourettes.ui.component.ScreenScaffold
import com.calmed.calmedfrontendtourettes.ui.component.SecondaryButton
import com.calmed.calmedfrontendtourettes.ui.component.TextField
import com.calmed.calmedfrontendtourettes.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.calmed.calmedfrontendtourettes.auth.AppleAuthBridge

@Composable
fun LoginScreen(
    onNavigateRegister: () -> Unit,
    onNavigateForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onAppleSignIn: () -> Unit,
    viewModel: AuthViewModel = koinInject()
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        AppleAuthBridge.onIdToken = { idToken ->
            if(idToken.isSuccess){
                println("APPLE_AUTH LoginScreen received id_token len=${idToken.getOrNull()}")
                scope.launch {
                    println("APPLE_AUTH LoginScreen CALLING VM...")
                    val ok = viewModel.loginWithApple(identityToken = idToken.getOrNull() ?: "")
                    println("APPLE_AUTH LoginScreen VM result=$ok")
                    if (ok) onLoginSuccess()
                }
            }

            AppleAuthBridge.onIdToken = null
        }
    }


    val uriHandler = LocalUriHandler.current

    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    ScreenScaffold(title = "Login") {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {


            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            TextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                singleLine = true
            )

            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                singleLine = true,
                isError = false,
                supportingText = null,
                modifier = Modifier,
            )

            PrimaryButton(
                text = if (loading) "Logging in..." else "Login",
                onClick = {
                    scope.launch {
                        val success = viewModel.login(email, password)
                        if (success) {
                            onLoginSuccess()
                        }
                    }
                },
                enabled = !loading
            )

            SecondaryButton(
                text = "Create account",
                onClick = onNavigateRegister,
                enabled = !loading
            )

            GoogleSignInButton(
                onClick = onGoogleSignIn,
                enabled = !loading
            )
            AppleSignInButton(
                onClick = onAppleSignIn,
                enabled = !loading
            )

            TextButton(
                onClick = onNavigateForgotPassword,
                enabled = !loading
            ) {
                Text("Forgot password?")
            }
        }
    }

}