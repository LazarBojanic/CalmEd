package com.calmed.calmedfrontendtourettes.notifications

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

private var permissionRequester: ((String) -> Unit)? = null

fun setNotificationPermissionRequester(requester: (String) -> Unit) {
	permissionRequester = requester
}

fun requestNotificationPermissionIfNeeded(context: android.content.Context): Boolean {
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
		return true
	}

	val granted = ContextCompat.checkSelfPermission(
		context,
		Manifest.permission.POST_NOTIFICATIONS
	) == PackageManager.PERMISSION_GRANTED

	if (granted) return true

	val requester = permissionRequester
	if (requester == null) {
		Log.w("NOTIFICATIONS", "Permission requester not set; cannot request POST_NOTIFICATIONS.")
		return false
	}

	requester(Manifest.permission.POST_NOTIFICATIONS)
	Log.d("NOTIFICATIONS", "Requested POST_NOTIFICATIONS permission.")
	return false
}
