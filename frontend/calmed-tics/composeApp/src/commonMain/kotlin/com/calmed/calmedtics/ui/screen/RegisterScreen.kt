package com.calmed.calmedtics.ui.screen

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
import com.calmed.calmedtics.Res
import com.calmed.calmedtics.back_to_login
import com.calmed.calmedtics.confirm_password
import com.calmed.calmedtics.creating
import com.calmed.calmedtics.email
import com.calmed.calmedtics.password_label
import com.calmed.calmedtics.register_title
import com.calmed.calmedtics.ui.component.PasswordTextField
import com.calmed.calmedtics.ui.component.PrimaryButton
import com.calmed.calmedtics.ui.component.ScreenScaffold
import com.calmed.calmedtics.ui.component.SecondaryButton
import com.calmed.calmedtics.ui.component.TextField
import com.calmed.calmedtics.username
import com.calmed.calmedtics.username_label
import com.calmed.calmedtics.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
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

    ScreenScaffold(title = stringResource(Res.string.register_title)){
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
                label = stringResource(Res.string.email),
                singleLine = true,
            )

            TextField(
                value = username,
                onValueChange = { username = it },
                label = stringResource(Res.string.username_label),
                singleLine = true,
            )

            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(Res.string.password_label),
                singleLine = true
            )

            PasswordTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = stringResource(Res.string.confirm_password),
                singleLine = true
            )

            PrimaryButton(
                text = if (loading) stringResource(Res.string.creating) else stringResource(Res.string.register_title),
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
                text = stringResource(Res.string.back_to_login),
                onClick = onNavigateLogin,
                enabled = !loading
            )
        }
    }


}