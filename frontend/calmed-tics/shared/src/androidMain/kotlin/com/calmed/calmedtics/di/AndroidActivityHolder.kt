package com.calmed.calmedtics.di

import androidx.activity.ComponentActivity
import java.lang.ref.WeakReference

object AndroidActivityHolder {
    private var activityRef: WeakReference<ComponentActivity>? = null

    fun set(activity: ComponentActivity) {
        activityRef = WeakReference(activity)
    }

    fun clear() {
        activityRef = null
    }

    fun current(): ComponentActivity? = activityRef?.get()
}
