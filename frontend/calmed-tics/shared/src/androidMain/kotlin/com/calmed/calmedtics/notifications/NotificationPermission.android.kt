package com.calmed.calmedtics.notifications

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.calmed.calmedtics.logging.AppLog
import com.calmed.calmedtics.logging.LogTags

private val log = AppLog(LogTags.NOTIFICATIONS)

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
		log.warn("Permission requester not set; cannot request POST_NOTIFICATIONS.")
		return false
	}

	requester(Manifest.permission.POST_NOTIFICATIONS)
	log.debug("Requested POST_NOTIFICATIONS permission.")
	return false
}
