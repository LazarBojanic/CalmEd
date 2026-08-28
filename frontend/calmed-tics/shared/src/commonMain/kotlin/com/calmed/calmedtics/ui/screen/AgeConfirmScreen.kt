package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.age_confirmation_subtitle
import calmedtics.shared.generated.resources.age_confirmation_title
import calmedtics.shared.generated.resources.confirm_over_eighteen
import calmedtics.shared.generated.resources.not_over_eighteen
import calmedtics.shared.generated.resources.res_continue
import com.calmed.calmedtics.ui.component.AuthScaffold
import com.calmed.calmedtics.ui.component.CheckboxWithLabel
import com.calmed.calmedtics.ui.component.PrimaryButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun AgeConfirmScreen(
	onConfirm: () -> Unit,
	onDecline: () -> Unit,
	loading: Boolean = false,
	error: String? = null,
) {
	var confirmed by remember { mutableStateOf(false) }

	AuthScaffold(
		title = stringResource(Res.string.age_confirmation_title),
		subtitle = stringResource(Res.string.age_confirmation_subtitle),
	) {
		if (error != null) {
			Text(error, color = MaterialTheme.colorScheme.error)
		}

		CheckboxWithLabel(
			label = stringResource(Res.string.confirm_over_eighteen),
			checked = confirmed,
			onCheckedChange = { confirmed = it },
		)

		PrimaryButton(
			text = stringResource(Res.string.res_continue),
			onClick = onConfirm,
			enabled = confirmed && !loading,
		)

		TextButton(
			onClick = onDecline,
			enabled = !loading,
			modifier = Modifier.fillMaxWidth(),
		) {
			Text(stringResource(Res.string.not_over_eighteen))
		}
	}
}
