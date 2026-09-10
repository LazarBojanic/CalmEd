package com.calmed.calmedtics.util

enum class NetworkType {
    Wifi,
    Ethernet,
    Mobile,
    Other,
    None
}

expect fun currentNetworkType(): NetworkType
