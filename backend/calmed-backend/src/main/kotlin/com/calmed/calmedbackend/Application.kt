package com.calmed.calmedbackend;

import com.calmed.calmedbackend.auth.configureSecurity
import com.calmed.calmedbackend.database.configureDatabase
import com.calmed.calmedbackend.di.configureFrameworks
import com.calmed.calmedbackend.error.configureStatusPages
import com.calmed.calmedbackend.http.configureHTTP
import com.calmed.calmedbackend.requestvalidation.configureRequestValidation
import com.calmed.calmedbackend.routing.configureRouting
import com.calmed.calmedbackend.routing.configureStaticRouting
import com.calmed.calmedbackend.util.configureMonitoring
import com.calmed.calmedbackend.util.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
	io.ktor.server.netty.EngineMain.main(args)
}

suspend fun Application.module() {
	configureFrameworks()
	configureHTTP()
	configureSecurity()
	configureSerialization()
	configureMonitoring()
	configureDatabase()
	configureRequestValidation()
	configureRouting()
	configureStaticRouting()
	configureStatusPages()
}

