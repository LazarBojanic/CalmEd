package com.calmed.calmedtics.util

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerEditedImage
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import platform.posix.memcpy

private var activePickerDelegate: IosImagePickerDelegate? = null

class IosImagePicker : ImagePicker {
    override fun pickImage(onImageSelected: (ByteArray?) -> Unit) {
        val rootViewController = getTopViewController()
        if (rootViewController == null) {
            onImageSelected(null)
            return
        }

        val picker = UIImagePickerController()
        val delegate = IosImagePickerDelegate(picker, onImageSelected)
        activePickerDelegate = delegate
        picker.delegate = delegate
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary

        rootViewController.presentViewController(picker, animated = true, completion = null)
    }

    private fun getTopViewController(): UIViewController? {
        val window = UIApplication.sharedApplication.keyWindow
            ?: UIApplication.sharedApplication.windows.mapNotNull { it as? UIWindow }.firstOrNull { it.isKeyWindow() }
            ?: UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
        var topController = window?.rootViewController ?: return null
        while (topController.presentedViewController != null) {
            topController = topController.presentedViewController ?: break
        }
        return topController
    }
}

private class IosImagePickerDelegate(
    private val picker: UIImagePickerController,
    private val onImageSelected: (ByteArray?) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    @OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        try {
            val image = (didFinishPickingMediaWithInfo[UIImagePickerControllerEditedImage]
                ?: didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage]) as? UIImage
            val nsData: NSData? = image?.let { UIImageJPEGRepresentation(it, 0.9) }
            val bytes = nsData?.toByteArray()
            onImageSelected(bytes)
        } catch (e: Exception) {
            onImageSelected(null)
        } finally {
            picker.dismissViewControllerAnimated(true, completion = null)
            activePickerDelegate = null
        }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        try {
            onImageSelected(null)
        } finally {
            picker.dismissViewControllerAnimated(true, completion = null)
            activePickerDelegate = null
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val length = this.length.toInt()
    if (length == 0) return ByteArray(0)
    return ByteArray(length).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
        }
    }
}

actual fun createImagePicker(): ImagePicker = IosImagePicker()
