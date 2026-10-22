package com.calmed.calmedtics.ui.component.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.default_user
import calmedtics.shared.generated.resources.hello_user
import calmedtics.shared.generated.resources.home_welcome
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeHeader(
    preferredName: String?,
    username: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(
                Res.string.hello_user,
                if (preferredName.isNullOrBlank()) {
                    username ?: stringResource(Res.string.default_user)
                } else {
                    preferredName
                }
            ),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = stringResource(Res.string.home_welcome),
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
