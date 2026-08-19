package com.calmed.calmedtics.billing

import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray

/**
 * Deterministic, obfuscated, short identifier derived from a stable account key (e.g. email).
 *
 * Used as Google Play's `obfuscatedAccountId` so a purchase can be correlated to an app account
 * without exposing the raw email. This must match the backend implementation byte-for-byte
 * (FNV-1a 64-bit, hex encoded).
 */
fun obfuscateAccountId(identifier: String): String {
    var hash = 0xcbf29ce484222325UL
    for (byte in identifier.trim().lowercase().toByteArray(Charsets.UTF_8)) {
        hash = hash xor (byte.toInt() and 0xFF).toULong()
	    hash *= 0x100000001b3UL
    }
    return hash.toString(16).padStart(16, '0')
}
