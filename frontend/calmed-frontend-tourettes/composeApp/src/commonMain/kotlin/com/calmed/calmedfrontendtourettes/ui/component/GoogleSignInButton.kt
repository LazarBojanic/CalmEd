package com.calmed.calmedfrontendtourettes.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
private fun GoogleGLogo(
	modifier: Modifier = Modifier
) {
	val blue = Color(0xFF4285F4)
	val red = Color(0xFFEA4335)
	val yellow = Color(0xFFFBBC05)
	val green = Color(0xFF34A853)

	Canvas(modifier = modifier) {
		val strokeWidth = size.minDimension * 0.18f
		val inset = strokeWidth / 2f
		val arcSize = Size(size.width - 2f * inset, size.height - 2f * inset)
		val topLeft = Offset(inset, inset)

		val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

		drawArc(
			color = red,
			startAngle = -45f,
			sweepAngle = 90f,
			useCenter = false,
			topLeft = topLeft,
			size = arcSize,
			style = stroke
		)
		drawArc(
			color = yellow,
			startAngle = 45f,
			sweepAngle = 90f,
			useCenter = false,
			topLeft = topLeft,
			size = arcSize,
			style = stroke
		)
		drawArc(
			color = green,
			startAngle = 135f,
			sweepAngle = 90f,
			useCenter = false,
			topLeft = topLeft,
			size = arcSize,
			style = stroke
		)
		drawArc(
			color = blue,
			startAngle = 225f,
			sweepAngle = 90f,
			useCenter = false,
			topLeft = topLeft,
			size = arcSize,
			style = stroke
		)

		val y = size.height * 0.52f
		drawLine(
			color = blue,
			start = Offset(size.width * 0.54f, y),
			end = Offset(size.width * 0.92f, y),
			strokeWidth = strokeWidth,
			cap = StrokeCap.Round
		)
	}
}

@Composable
fun GoogleSignInButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	text: String = "Continue with Google",
) {
	OutlinedButton(
		onClick = onClick,
		enabled = enabled,
		modifier = Modifier
			.fillMaxWidth()
			.defaultMinSize(minHeight = 48.dp)
			.padding(horizontal = 16.dp)
			.then(modifier),
		colors = ButtonDefaults.outlinedButtonColors(
			containerColor = Color.White,
			contentColor = Color(0xFF3C4043),
			disabledContainerColor = Color.White,
			disabledContentColor = Color(0xFF3C4043).copy(alpha = 0.38f),
		),
		border = ButtonDefaults.outlinedButtonBorder.copy(
			brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFDADCE0))
		)
	) {
		Row(
			horizontalArrangement = Arrangement.Start
		) {
			GoogleGLogo(modifier = Modifier.size(18.dp))
			Spacer(Modifier.width(12.dp))
			Text(
				text = text,
				style = MaterialTheme.typography.labelLarge,
				fontWeight = FontWeight.Medium
			)
		}
	}
}