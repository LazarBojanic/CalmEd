package com.calmed.calmedbackend.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.videoPlaybackRoutes() {

    authenticate("auth-jwt") {

        get("/videos/{playbackId}/token") {

            val playbackId = call.parameters["playbackId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing playbackId")

            val muxKid = System.getenv("MUX_TOKEN_ID") ?: System.getenv("MUX_KID")
            val muxPrivateKeyPath = System.getenv("MUX_PRIVATE_KEY_PATH")
            val muxPrivateKey = System.getenv("MUX_PRIVATE_KEY")

            val privateKeyPem = when {
                !muxPrivateKeyPath.isNullOrBlank() -> java.io.File(muxPrivateKeyPath).readText()
                !muxPrivateKey.isNullOrBlank() -> muxPrivateKey
                else -> null
            }

            if (muxKid.isNullOrBlank() || privateKeyPem.isNullOrBlank()) {
                return@get call.respond(HttpStatusCode.InternalServerError, "Mux credentials missing")
            }
            val fixedKey = privateKeyPem.replace("\\n", "\n")

            val token = MuxTokenGenerator.generatePlaybackToken(
                playbackId = playbackId,
                kid = muxKid,
                privateKeyPemPkcs8 = fixedKey
            )

            call.respond(
                mapOf(
                    "playbackId" to playbackId,
                    "token" to token
                )
            )
        }
    }
}