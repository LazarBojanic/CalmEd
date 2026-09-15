package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class ToastKind {
    Success,
    Error,
    Info
}

data class AppToast(
    val id: Long,
    val kind: ToastKind,
    val text: String? = null,
    val resource: StringResource? = null,
    val args: List<Any> = emptyList()
)

object ToastCenter {
    private const val MAX_VISIBLE_TOASTS = 3

    private val _toasts = MutableStateFlow<List<AppToast>>(emptyList())

    val toasts: StateFlow<List<AppToast>> = _toasts

    private var nextId = 0L

    fun show(text: String, kind: ToastKind = ToastKind.Info) {
        val toast = AppToast(
            id = nextId++,
            kind = kind,
            text = text
        )
        _toasts.update { current ->
            (current + toast).takeLast(MAX_VISIBLE_TOASTS)
        }
    }

    fun show(
        resource: StringResource,
        vararg args: Any,
        kind: ToastKind = ToastKind.Info
    ) {
        val toast = AppToast(
            id = nextId++,
            kind = kind,
            resource = resource,
            args = args.toList()
        )
        _toasts.update { current ->
            (current + toast).takeLast(MAX_VISIBLE_TOASTS)
        }
    }

    fun dismiss(id: Long) {
        _toasts.update { current ->
            current.filterNot { it.id == id }
        }
    }
}

@Composable
fun AppToastHost(modifier: Modifier = Modifier) {
    val toasts by ToastCenter.toasts.collectAsStateWithLifecycle(LocalLifecycleOwner.current)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        toasts.forEach { toast ->
            ToastCard(toast = toast)
        }
    }
}

@Composable
private fun ToastCard(toast: AppToast) {
    LaunchedEffect(toast.id) {
        delay(TOAST_DURATION_MS)
        ToastCenter.dismiss(toast.id)
    }

    val (icon, tint) = when (toast.kind) {
        ToastKind.Success -> Icons.Filled.CheckCircle to Color(0xFF2E7D32)
        ToastKind.Error -> Icons.Filled.ErrorOutline to Color(0xFFC62828)
        ToastKind.Info -> Icons.Filled.Info to MaterialTheme.colorScheme.primary
    }

    val message = toast.text ?: toast.resource?.let { resource ->
        stringResource(resource, *toast.args.toTypedArray())
    } ?: ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private const val TOAST_DURATION_MS = 3_000L
