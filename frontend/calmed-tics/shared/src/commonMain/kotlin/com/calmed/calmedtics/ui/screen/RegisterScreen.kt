package com.calmed.calmedtics.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.back_to_login
import calmedtics.shared.generated.resources.confirm_password
import calmedtics.shared.generated.resources.creating
import calmedtics.shared.generated.resources.email
import calmedtics.shared.generated.resources.password_label
import calmedtics.shared.generated.resources.register_title
import com.calmed.calmedtics.ui.component.CheckboxWithLabel
import com.calmed.calmedtics.ui.component.PasswordTextField
import com.calmed.calmedtics.ui.component.PrimaryButton
import com.calmed.calmedtics.ui.component.AuthScaffold
import com.calmed.calmedtics.ui.component.SecondaryButton
import com.calmed.calmedtics.ui.component.TextField
import calmedtics.shared.generated.resources.username_label
import com.calmed.calmedtics.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import calmedtics.shared.generated.resources.confirm_over_eighteen
import androidx.compose.runtime.LaunchedEffect
import com.calmed.calmedtics.auth.AppleAuthBridge
import com.calmed.calmedtics.auth.GoogleAuthBridge
import com.calmed.calmedtics.ui.component.GoogleSignInButton
import com.calmed.calmedtics.ui.component.AppleSignInButton

@Composable
fun RegisterScreen(
	onNavigateLogin: () -> Unit,
	onRegisterSuccess: () -> Unit,
	onGoogleSignIn: () -> Unit,
	onAppleSignIn: () -> Unit,
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
	var confirmOverEighteen by remember { mutableStateOf(false) }

	AuthScaffold(
		title = stringResource(Res.string.register_title),
		onBack = onNavigateLogin,
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
			CheckboxWithLabel(
				label = stringResource(Res.string.confirm_over_eighteen),
				checked = confirmOverEighteen,
				onCheckedChange = { confirmOverEighteen = it },
			)

			PrimaryButton(
				text = if (loading) stringResource(Res.string.creating) else stringResource(Res.string.register_title),
				onClick = {
					scope.launch {
						val success = viewModel.register(email, username, password, confirmPassword, confirmOverEighteen)
						if (success) {
							onRegisterSuccess()
						}
					}
				},
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

			SecondaryButton(
				text = stringResource(Res.string.back_to_login),
				onClick = onNavigateLogin,
				enabled = !loading
			)
	}
}