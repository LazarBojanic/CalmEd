package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.layout.fillMaxWidth
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
import com.calmed.calmedtics.ui.component.GoogleSignInButton
import com.calmed.calmedtics.ui.component.AppleSignInButton
import com.calmed.calmedtics.ui.component.PasswordTextField
import com.calmed.calmedtics.ui.component.PrimaryButton
import com.calmed.calmedtics.ui.component.AuthScaffold
import com.calmed.calmedtics.ui.component.SecondaryButton
import com.calmed.calmedtics.ui.component.TextField
import com.calmed.calmedtics.settings.AppSettings
import com.calmed.calmedtics.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.calmed.calmedtics.auth.AppleAuthBridge
import com.calmed.calmedtics.auth.GoogleAuthBridge
import org.jetbrains.compose.resources.stringResource
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.create_account
import calmedtics.shared.generated.resources.email_label
import calmedtics.shared.generated.resources.forgot_password
import calmedtics.shared.generated.resources.logging_in
import calmedtics.shared.generated.resources.login_button
import calmedtics.shared.generated.resources.login_title
import calmedtics.shared.generated.resources.password_label
import calmedtics.shared.generated.resources.use_offline_mode

@Composable
fun LoginScreen(
    onNavigateRegister: () -> Unit,
    onNavigateForgotPassword: () -> Unit,
    onNavigateOffline: () -> Unit,
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

        GoogleAuthBridge.onIdToken = { idToken ->
            if (idToken.isSuccess) {
                scope.launch {
                    val ok = viewModel.loginWithGoogle(idToken.getOrNull() ?: "")
                    if (ok) onLoginSuccess()
                }
            }
            GoogleAuthBridge.onIdToken = null
        }
    }

    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val appSettings: AppSettings = koinInject()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AuthScaffold(title = stringResource(Res.string.login_title)) {
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            TextField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(Res.string.email_label),
                singleLine = true,
            )

            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(Res.string.password_label),
                singleLine = true,
                isError = false,
                supportingText = null,
            )

            PrimaryButton(
                text = if (loading) stringResource(Res.string.logging_in) else stringResource(Res.string.login_button),
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
                text = stringResource(Res.string.create_account),
                onClick = onNavigateRegister,
                enabled = !loading
            )

            SecondaryButton(
                text = stringResource(Res.string.use_offline_mode),
                onClick = onNavigateOffline,
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
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.forgot_password))
            }
    }

}
