package com.calmed.calmedbackend.di

import com.calmed.calmedbackend.config.DatabaseConfig
import com.calmed.calmedbackend.config.EmailConfig
import com.calmed.calmedbackend.config.JwtConfig
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
	install(Koin) {
		slf4jLogger()
		modules(
			module { single { JwtConfig.from(environment.config) } },
			module { single { DatabaseConfig.from(environment.config) } },
			module { single { EmailConfig.from(environment.config) } },
			repositoryModule,
			serviceModule,
		)
	}
}
