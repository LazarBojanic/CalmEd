package com.calmed.calmedbackend.auth.apple

data class AppleConfig(
    val teamId: String,
    val keyId: String,
    val clientId: String,
    val privateKeyPem: String
) {
    companion object {
        fun fromEnv(): AppleConfig {
            val teamId = System.getenv("APPLE_TEAM_ID") ?: error("Missing APPLE_TEAM_ID")
            val keyId = System.getenv("APPLE_KEY_ID") ?: error("Missing APPLE_KEY_ID")
            val clientId = System.getenv("APPLE_CLIENT_ID") ?: error("Missing APPLE_CLIENT_ID")

            // IntelliJ env: \n -> real newline
            val privateKeyPem = (System.getenv("APPLE_PRIVATE_KEY") ?: error("Missing APPLE_PRIVATE_KEY"))
                .replace("\\n", "\n")

            return AppleConfig(
                teamId = teamId,
                keyId = keyId,
                clientId = clientId,
                privateKeyPem = privateKeyPem
            )
        }
    }
}