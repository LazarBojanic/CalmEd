package com.calmed.calmedtics.util

import kotlin.concurrent.Volatile
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Network.nw_interface_type_cellular
import platform.Network.nw_interface_type_loopback
import platform.Network.nw_interface_type_other
import platform.Network.nw_interface_type_wifi
import platform.Network.nw_interface_type_wired
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_t
import platform.Network.nw_path_uses_interface_type

@OptIn(ExperimentalForeignApi::class)
private object IosNetworkMonitor {

    @Volatile
    var cachedType: NetworkType = NetworkType.None

    init {
        val monitor = nw_path_monitor_create()
        if (monitor != null) {
            nw_path_monitor_set_update_handler(monitor) { path ->
                cachedType = mapPath(path)
            }
            nw_path_monitor_start(monitor)
        }
    }

    private fun mapPath(path: nw_path_t?): NetworkType {
        if (path == null) {
            return NetworkType.None
        }

        return when {
            nw_path_uses_interface_type(path, nw_interface_type_wifi) ->
                NetworkType.Wifi

            nw_path_uses_interface_type(path, nw_interface_type_wired) ->
                NetworkType.Ethernet

            nw_path_uses_interface_type(path, nw_interface_type_cellular) ->
                NetworkType.Mobile

            nw_path_uses_interface_type(path, nw_interface_type_loopback) ||
                nw_path_uses_interface_type(path, nw_interface_type_other) ->
                NetworkType.Other

            else -> NetworkType.None
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun currentNetworkType(): NetworkType = IosNetworkMonitor.cachedType
