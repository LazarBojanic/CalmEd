package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.calmed.calmedtics.ui.component.GoogleSignInButton
import com.calmed.calmedtics.ui.component.AppleSignInButton
import com.calmed.calmedtics.ui.component.PasswordTextField
import com.calmed.calmedtics.ui.component.PrimaryButton
import com.calmed.calmedtics.ui.component.ScreenScaffold
import com.calmed.calmedtics.ui.component.SecondaryButton
import com.calmed.calmedtics.ui.component.TextField
import com.calmed.calmedtics.ui.component.LanguageToggle
import com.calmed.calmedtics.settings.AppSettings
import com.calmed.calmedtics.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.calmed.calmedtics.auth.AppleAuthBridge
import com.calmed.calmedtics.auth.GoogleAuthBridge
import org.jetbrains.compose.resources.stringResource
import com.calmed.calmedtics.Res
import com.calmed.calmedtics.create_account
import com.calmed.calmedtics.email_label
import com.calmed.calmedtics.forgot_password
import com.calmed.calmedtics.logging_in
import com.calmed.calmedtics.login_button
import com.calmed.calmedtics.login_title
import com.calmed.calmedtics.password_label
import com.calmed.calmedtics.use_offline_mode
import com.calmed.calmedtics.localization.customAppLocale

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
    var currentLanguage by remember { mutableStateOf(appSettings.getAppLanguage()) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    ScreenScaffold(title = stringResource(Res.string.login_title)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            LanguageToggle(
                selectedLanguage = currentLanguage,
                onLanguageSelected = { lang ->
                    currentLanguage = lang
                    appSettings.setAppLanguage(lang)
                    customAppLocale = lang
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            TextField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(Res.string.email_label),
                singleLine = true
            )

            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(Res.string.password_label),
                singleLine = true,
                isError = false,
                supportingText = null,
                modifier = Modifier,
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
                enabled = !loading
            ) {
                Text(stringResource(Res.string.forgot_password))
            }
        }
    }

}
