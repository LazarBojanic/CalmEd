package com.calmed.calmedtics.util

interface ImagePicker {
    fun pickImage(onImageSelected: (ByteArray?) -> Unit)
}

expect fun createImagePicker(): ImagePicker