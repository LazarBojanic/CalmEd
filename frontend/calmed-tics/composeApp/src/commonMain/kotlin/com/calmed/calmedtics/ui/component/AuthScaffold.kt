package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.calmed.calmedtics.Res
import com.calmed.calmedtics.calmed_splash_logo
import com.calmed.calmedtics.logo
import com.calmed.calmedtics.theme.appBackgroundGradient
import org.jetbrains.compose.resources.painterResource

@Composable
fun AuthScaffold(
	title: String,
	subtitle: String? = null,
	onBack: (() -> Unit)? = null,
	content: @Composable ColumnScope.() -> Unit,
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(appBackgroundGradient()),
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.statusBarsPadding()
				.navigationBarsPadding()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 24.dp, vertical = 16.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			if (onBack != null) {
				Box(modifier = Modifier.fillMaxWidth()) {
					IconButton(
						onClick = onBack,
						modifier = Modifier.align(Alignment.CenterStart),
					) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onSurface,
						)
					}
				}
			} else {
				Spacer(modifier = Modifier.height(8.dp))
			}

			Image(
				painter = painterResource(Res.drawable.calmed_splash_logo),
				contentDescription = null,
				modifier = Modifier.size(96.dp),
			)

			Spacer(modifier = Modifier.height(20.dp))

			Column(
				modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
				verticalArrangement = Arrangement.spacedBy(12.dp),
			) {
				Text(
					text = title,
					style = MaterialTheme.typography.headlineSmall,
					fontWeight = FontWeight.SemiBold,
					color = MaterialTheme.colorScheme.onSurface,
					textAlign = TextAlign.Center,
					modifier = Modifier.fillMaxWidth(),
				)

				if (subtitle != null) {
					Text(
						text = subtitle,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						textAlign = TextAlign.Center,
						modifier = Modifier.fillMaxWidth(),
					)
				}

				content()
			}
		}
	}
}
