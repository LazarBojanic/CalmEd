package com.calmed.calmedtics.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.calmed.calmedtics.di.appContext

actual fun currentNetworkType(): NetworkType {
    val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return NetworkType.None

    val network = connectivityManager.activeNetwork ?: return NetworkType.None

    val capabilities =
        connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.None

    return when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
            NetworkType.Wifi

        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->
            NetworkType.Ethernet

        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
            NetworkType.Mobile

        else -> NetworkType.Other
    }
}
