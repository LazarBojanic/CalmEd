package com.calmed.calmedtics.util

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun decodeImage(bytes: ByteArray): ImageBitmap? {
    return try {
        BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size
        )?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}