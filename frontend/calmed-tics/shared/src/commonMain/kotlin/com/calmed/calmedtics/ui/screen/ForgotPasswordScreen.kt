package com.calmed.calmedtics.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.back
import calmedtics.shared.generated.resources.email_label
import calmedtics.shared.generated.resources.forgot_password_title
import calmedtics.shared.generated.resources.send_reset_email
import calmedtics.shared.generated.resources.sending
import com.calmed.calmedtics.ui.component.PrimaryButton
import com.calmed.calmedtics.ui.component.AuthScaffold
import com.calmed.calmedtics.ui.component.SecondaryButton
import com.calmed.calmedtics.ui.component.TextField
import com.calmed.calmedtics.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()

    val loading by viewModel.loading.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
    val error by viewModel.error.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
    val info by viewModel.info.collectAsStateWithLifecycle(LocalLifecycleOwner.current)

    var email by remember { mutableStateOf("") }

    AuthScaffold(
        title = stringResource(Res.string.forgot_password_title),
        onBack = onNavigateBack,
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
                label = stringResource(Res.string.email_label),
                singleLine = true,
            )

            PrimaryButton(
                text = if (loading) stringResource(Res.string.sending) else stringResource(Res.string.send_reset_email),
                onClick = {
                    scope.launch {
                        viewModel.forgotPassword(email)
                    }
                },
                enabled = !loading
            )

            SecondaryButton(
                text = stringResource(Res.string.back),
                onClick = onNavigateBack,
                enabled = !loading
            )
    }


}