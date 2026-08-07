package com.calmed.calmedtics.util

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

private var imagePickerActivityProvider: (() -> ComponentActivity)? = null

fun setImagePickerActivityProvider(
    provider: () -> ComponentActivity
) {
    imagePickerActivityProvider = provider
}

class AndroidImagePicker(
    private val activity: ComponentActivity
) : ImagePicker {

    override fun pickImage(
        onImageSelected: (ByteArray?) -> Unit
    ) {
        val launcher =
            activity.activityResultRegistry.register(
                "profile_image_picker_${System.currentTimeMillis()}",
                ActivityResultContracts.GetContent()
            ) { uri: Uri? ->

                if (uri == null) {
                    onImageSelected(null)
                    return@register
                }

                try {
                    val bytes =
                        activity.contentResolver
                            .openInputStream(uri)
                            ?.use { it.readBytes() }

                    onImageSelected(bytes)
                } catch (e: Exception) {
                    onImageSelected(null)
                }
            }

        launcher.launch("image/*")
    }
}

actual fun createImagePicker(): ImagePicker {
    val activity =
        imagePickerActivityProvider?.invoke()
            ?: error("ImagePicker Activity provider is not set")

    return AndroidImagePicker(activity)
}