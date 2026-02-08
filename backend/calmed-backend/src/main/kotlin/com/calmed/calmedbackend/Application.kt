package com.calmed.calmedbackend;

import com.calmed.calmedbackend.auth.configureSecurity
import com.calmed.calmedbackend.config.KtorConfig
import com.calmed.calmedbackend.database.configureDatabase
import com.calmed.calmedbackend.di.configureFrameworks
import com.calmed.calmedbackend.error.configureStatusPages
import com.calmed.calmedbackend.http.configureHTTP
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialTable
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExerciseTable
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshTokenTable
import com.calmed.calmedbackend.model.raw.user.UserTable
import com.calmed.calmedbackend.model.raw.userexerciseprogress.UserExerciseProgressTable
import com.calmed.calmedbackend.model.raw.userinfo.tourettes.UserInfoTourettesTable
import com.calmed.calmedbackend.model.raw.userprogram.UserProgramTable
import com.calmed.calmedbackend.routing.configureRouting
import com.calmed.calmedbackend.routing.configureStaticRouting
import com.calmed.calmedbackend.util.configureMonitoring
import com.calmed.calmedbackend.util.configureSerialization
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.ktor.ext.inject
import com.calmed.calmedbackend.model.MapMode
import com.calmed.calmedbackend.model.setFrom
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExercise
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExerciseEntity
import java.time.Instant
import java.util.UUID

fun main(args: Array<String>) {
	io.ktor.server.netty.EngineMain.main(args)
}

suspend fun Application.module() {
	val config = environment.config
//	println("APPLE clientId = " + config.property("oauth.apple.client_id").getString())
//	println("APPLE redirectUri = " + config.property("oauth.apple.redirect_uri").getString())

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
		UserInfoTourettesTable,
		UserProgramTable,
		ProgramExerciseTable,
		UserExerciseProgressTable
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
	val seedExercises = listOf(
		ProgramExercise(
			id = UUID.fromString("11b919a8-bedb-4321-92cf-c7d4ed343219"),
			weekNumber = 5,
			title = "Čitanje pravila edukacije sa ritmom",
			description = null,
			videoURL = "https://bombona.rs/videos/week5/exercise_01/week05_exe01.m3u8",
			thumbnailURL = null,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("1b1e8afa-e689-4886-87a1-a340f6b18f20"),
			weekNumber = 2,
			title = "Vežbe protiv tikova očne regije",
			description = null,
			videoURL = "https://bombona.rs/videos/week2/exercise_02/week02_exe02.m3u8",
			thumbnailURL = null,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("2e2f609e-330e-42d1-befb-28f7eb4ad4ff"),
			weekNumber = 2,
			title = "Čitanje pravila edukacije sa ritmom",
			description = null,
			videoURL = "https://bombona.rs/videos/week2/exercise_01/week02_exe01.m3u8",
			thumbnailURL = null,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("2ff667ca-da16-4fb7-b058-a574d4afb83e"),
			weekNumber = 6,
			title = "Vežbe za pažnju u Lazarovim krugovima",
			description = null,
			videoURL = "https://bombona.rs/videos/week6/exercise_02/week06_exe02.m3u8",
			thumbnailURL = null,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("4d375a8b-6308-4d7d-a265-7e7bb5b7a2e5"),
			weekNumber = 7,
			title = "Vežbe imaginacije (pisanje slova A; 5x)",
			description = null,
			videoURL = "https://bombona.rs/videos/week7/exercise_02/week07_exe02.m3u8",
			thumbnailURL = null,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("4d761512-a099-4a9f-bb64-c72232aec082"),
			weekNumber = 1,
			title = "Citanje pravila edukacije",
			description = null,
			videoURL = "https://bombona.rs/videos/week1/exercise_01/week01_exe01.m3u8",
			thumbnailURL = null,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("6a523493-1c23-4a6c-b3bd-35f7b5aed130"),
			weekNumber = 5,
			title = "Predilekciono pisanje",
			description = null,
			videoURL = "https://bombona.rs/videos/week5/exercise_02/week05_exe02.m3u8",
			thumbnailURL = null,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("8c43414b-fea9-48ae-b9b0-f0f7214d2188"),
			weekNumber = 8,
			title = "Vežbe za pažnju (brojanje od broja do broja)",
			description = null,
			videoURL = "https://bombona.rs/videos/week8/exercise_01/week08_exe01.m3u8",
			thumbnailURL = null,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("91f4b22c-43d9-4413-ad96-e13306eb3276"),
			weekNumber = 3,
			title = "Progresivna relaksacija",
			description = null,
			videoURL = "https://bombona.rs/videos/week3/exercise_02/week03_exe02.m3u8",
			thumbnailURL = null,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("92dffa84-a20c-4526-a8a6-e6669f2443b8"),
			weekNumber = 7,
			title = "Vežbe za pažnju u Lazarovim krugovima",
			description = null,
			videoURL = "https://bombona.rs/videos/week7/exercise_01/week07_exe01.m3u8",
			thumbnailURL = null,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("9cad8950-c379-4251-890a-697b4968343f"),
			weekNumber = 3,
			title = "Vežbe protiv tikova očne regije",
			description = null,
			videoURL = "https://bombona.rs/videos/week3/exercise_01/week03_exe01.m3u8",
			thumbnailURL = null,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("b5aec508-8c17-4003-a7ff-74994bb93ed0"),
			weekNumber = 4,
			title = "Progresivna relaksacija",
			description = null,
			videoURL = "https://bombona.rs/videos/week4/exercise_02/week04_exe02.m3u8",
			thumbnailURL = null,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("b8aaf9a7-f668-4847-9dc8-5e6a8c26fcd6"),
			weekNumber = 8,
			title = "Vežbe imaginacije (pisanje slova E; 5x)",
			description = null,
			videoURL = "https://bombona.rs/videos/week8/exercise_02/week08_exe02.m3u8",
			thumbnailURL = null,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("cf3fa3d2-3e3d-4faf-885f-d5264b8f294c"),
			weekNumber = 1,
			title = "Vežbe za diferencijaciju sheme tela",
			description = null,
			videoURL = "https://bombona.rs/videos/week1/exercise_02/week01_exe02.m3u8",
			thumbnailURL = null,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("d6c3ffff-cc26-4174-9a45-ad2ab8f31ed9"),
			weekNumber = 6,
			title = "Predilekciono pisanje",
			description = null,
			videoURL = "https://bombona.rs/videos/week6/exercise_01/week06_exe01.m3u8",
			thumbnailURL = null,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("fada6c0b-78f1-43ae-a61e-a943f15647e8"),
			weekNumber = 4,
			title = "Vežbe protiv tikova očne regije",
			description = null,
			videoURL = "https://bombona.rs/videos/week4/exercise_01/week04_exe01.m3u8",
			thumbnailURL = null,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		)
	)

	transaction {
		for (ex in seedExercises) {
			val existing = ProgramExerciseEntity.findById(ex.id)
			if (existing == null) {
				ProgramExerciseEntity.new(ex.id) {
					setFrom(ex, MapMode.CREATE)
				}
			}
		}
	}
}