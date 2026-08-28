package com.calmed.calmedtics.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import calmedtics.shared.generated.resources.*
import com.calmed.calmedtics.ui.component.*
import com.calmed.calmedtics.util.isValidEmail
import com.calmed.calmedtics.util.validatePassword
import com.calmed.calmedtics.util.PasswordValidationError
import com.calmed.calmedtics.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

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

	var emailError by remember { mutableStateOf(false) }
	var passwordError by remember { mutableStateOf<PasswordValidationError?>(null) }

	AuthScaffold(
		title = stringResource(Res.string.register_title),
		onBack = onNavigateLogin,
	) {
		if (error != null) {
			Text(
				error!!,
				color = MaterialTheme.colorScheme.error
			)
		}

		if (info != null) {
			Text(
				info!!,
				color = MaterialTheme.colorScheme.primary
			)
		}

		TextField(
			value = email,
			onValueChange = {
				email = it
				emailError = false
			},
			label = stringResource(Res.string.email),
			singleLine = true,
			isError = emailError,
			modifier = Modifier.onFocusChanged { focusState ->
				if (!focusState.isFocused) {
					emailError = email.isNotBlank() && !isValidEmail(email)
				}
			}
		)

		if (emailError) {
			Text(
				text = "Please enter a valid email address.",
				color = MaterialTheme.colorScheme.error
			)
		}

		TextField(
			value = username,
			onValueChange = { username = it },
			label = stringResource(Res.string.username_label),
			singleLine = true,
		)

		PasswordTextField(
			value = password,
			onValueChange = {
				password = it
				passwordError = null
			},
			label = stringResource(Res.string.password_label),
			singleLine = true,
			modifier = Modifier.onFocusChanged { focusState ->
				if (!focusState.isFocused && password.isNotBlank()) {
					passwordError = validatePassword(password, confirmPassword)
				}
			}
		)

		PasswordTextField(
			value = confirmPassword,
			onValueChange = {
				confirmPassword = it
				passwordError = null
			},
			label = stringResource(Res.string.confirm_password),
			singleLine = true,
			modifier = Modifier.onFocusChanged { focusState ->
				if (!focusState.isFocused && confirmPassword.isNotBlank()) {
					passwordError = validatePassword(password, confirmPassword)
				}
			}
		)

		if (passwordError != null) {
			Text(
				text = passwordErrorMessage(passwordError!!),
				color = MaterialTheme.colorScheme.error
			)
		}

		PrimaryButton(
			text = if (loading) {
				stringResource(Res.string.creating)
			} else {
				stringResource(Res.string.register_title)
			},
			onClick = {
				if (!isValidEmail(email)) {
					emailError = true
					return@PrimaryButton
				}

				val pwError = validatePassword(password, confirmPassword)
				if (pwError != null) {
					passwordError = pwError
					return@PrimaryButton
				}

				scope.launch {
					val success = viewModel.register(
						email,
						username,
						password,
						confirmPassword
					)

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
	}
}

@Composable
private fun passwordErrorMessage(error: PasswordValidationError): String {
	return stringResource(
		when (error) {
			PasswordValidationError.TOO_SHORT -> Res.string.password_min_length
			PasswordValidationError.MISSING_UPPERCASE -> Res.string.password_uppercase
			PasswordValidationError.MISSING_LOWERCASE -> Res.string.password_lowercase
			PasswordValidationError.MISSING_DIGIT -> Res.string.password_digit
			PasswordValidationError.MISMATCH -> Res.string.passwords_do_not_match
		}
	)
}

