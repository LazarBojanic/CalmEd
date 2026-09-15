package com.calmed.calmedtics.util

interface ImagePicker {
    fun pickImage(onImageSelected: (ByteArray?) -> Unit)
}

fun interface ImagePickerProvider {
    fun create(): ImagePicker
}
