package com.calmed.calmedtics.billing

import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray

fun obfuscateAccountId(identifier: String): String {
    var hash = 0xcbf29ce484222325UL
    for (byte in identifier.trim().lowercase().toByteArray(Charsets.UTF_8)) {
        hash = hash xor (byte.toInt() and 0xFF).toULong()
	    hash *= 0x100000001b3UL
    }
    return hash.toString(16).padStart(16, '0')
}
