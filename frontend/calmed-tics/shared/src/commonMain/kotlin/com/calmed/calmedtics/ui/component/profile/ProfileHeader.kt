package com.calmed.calmedtics.ui.component.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.default_user
import calmedtics.shared.generated.resources.profile_photo
import com.calmed.calmedtics.model.joined.UserInfoTicsJoined
import com.calmed.calmedtics.model.joined.UserJoined
import com.calmed.calmedtics.appBaseUrl
import com.calmed.calmedtics.ui.component.Thumbnail
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileHeader(
	user: UserJoined?,
	userInfo: UserInfoTicsJoined?,
	modifier: Modifier = Modifier
) {
	val displayName = userInfo?.preferredName
		?: user?.username
		?: stringResource(Res.string.default_user)

	val email = user?.email ?: ""

	val profileImageUrl =
		user?.profileImageUrl?.let { url ->
			if (url.startsWith("http")) {
				url
			} else {
				"$appBaseUrl$url"
			}
		}

	Surface(
		modifier = modifier.fillMaxWidth(),
		shape = RoundedCornerShape(28.dp),
		color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
		border = BorderStroke(
			1.dp,
			MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
		)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(
					horizontal = 20.dp,
					vertical = 28.dp
				),
			horizontalAlignment = Alignment.CenterHorizontally
		) {

			Box(
				modifier = Modifier
					.size(96.dp)
					.clip(CircleShape)
					.background(
						MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
					)
					.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape),
				contentAlignment = Alignment.Center
			) {

				if (!profileImageUrl.isNullOrBlank()) {

					Thumbnail(
						url = profileImageUrl,
						contentDescription = stringResource(Res.string.profile_photo),
						modifier = Modifier
							.fillMaxSize()
							.clip(CircleShape)
					)

				} else {

					Icon(
						imageVector = Icons.Default.Person,
						contentDescription = null,
						modifier = Modifier.size(52.dp),
						tint = MaterialTheme.colorScheme.onSurface
					)
				}
			}

			Spacer(
				modifier = Modifier.height(16.dp)
			)

			Text(
				text = displayName,
				style = MaterialTheme.typography.headlineSmall,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onSurface
			)

			if (email.isNotEmpty()) {
				Spacer(
					modifier = Modifier.height(4.dp)
				)

				Text(
					text = email,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}
