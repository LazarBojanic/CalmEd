package com.calmed.calmedbackend;

import com.calmed.calmedbackend.auth.configureSecurity
import com.calmed.calmedbackend.config.KtorConfig
import com.calmed.calmedbackend.database.configureDatabase
import com.calmed.calmedbackend.di.configureFrameworks
import com.calmed.calmedbackend.error.configureStatusPages
import com.calmed.calmedbackend.http.configureHTTP
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialTable
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshTokenTable
import com.calmed.calmedbackend.model.raw.user.UserTable
import com.calmed.calmedbackend.model.raw.userinfo.tourettes.UserInfoTourettesTable
import com.calmed.calmedbackend.routing.configureRouting
import com.calmed.calmedbackend.routing.configureStaticRouting
import com.calmed.calmedbackend.util.configureMonitoring
import com.calmed.calmedbackend.util.configureSerialization
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.ktor.ext.inject

fun main(args: Array<String>) {
	io.ktor.server.netty.EngineMain.main(args)
}

suspend fun Application.module() {
	val ktorConfig by inject<KtorConfig>()
	configureFrameworks()
	configureHTTP()
	configureSecurity()
	configureSerialization()
	configureMonitoring()
	configureDatabase()
	configureRouting()
	configureStaticRouting()
	configureStatusPages()


	val allTables = arrayOf(
		UserTable,
		AuthCredentialTable,
		RefreshTokenTable,
		UserInfoTourettesTable
	)
	transaction {
		if(ktorConfig.development){
			exec("DROP SCHEMA IF EXISTS public CASCADE;")
			exec("CREATE SCHEMA public;")
		}
	}
	transaction{
		SchemaUtils.createMissingTablesAndColumns(*allTables)
	}
	seed()

}

suspend fun Application.seed(){

}