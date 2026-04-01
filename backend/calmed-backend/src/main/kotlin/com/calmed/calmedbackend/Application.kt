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
import com.calmed.calmedbackend.model.raw.userinfo.tics.UserInfoTicsTable
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
import com.calmed.calmedbackend.model.raw.programexercise.Visibility
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
		UserInfoTicsTable,
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

suspend fun Application.seed() {
	val seedExercises = listOf(
		ProgramExercise(
			id = UUID.fromString("3a420f83-c314-4731-b319-310c94e55752"),
			weekNumber = 0,
			title = "Introdukcija",
			description = null,
			playbackId = "uttZtmeMxS3Lwg0257AIQHgClHuGGxj1dE7fbrVkOMKg",
			thumbnailURL = null,
			visibility = Visibility.PUBLIC,
			orderInWeek = 0,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),

		// Week 1
		ProgramExercise(
			id = UUID.fromString("4d761512-a099-4a9f-bb64-c72232aec082"),
			weekNumber = 1,
			title = "Citanje pravila edukacije",
			description = null,
			playbackId = "301ftAS6RZ1OYvumxZl1bOaajZM45Yn7jMC7UIgOBMpM",
			thumbnailURL = null,
			visibility = Visibility.PUBLIC,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("cf3fa3d2-3e3d-4faf-885f-d5264b8f294c"),
			weekNumber = 1,
			title = "Vežbe za diferencijaciju sheme tela",
			description = null,
			playbackId = "eserrArshN3dSxsO8ENIv6VpUCJf01W017vWKq2U9TJ6o",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),

		// Week 2
		ProgramExercise(
			id = UUID.fromString("2e2f609e-330e-42d1-befb-28f7eb4ad4ff"),
			weekNumber = 2,
			title = "Čitanje pravila edukacije sa ritmom",
			description = null,
			playbackId = "Brs9MKd1Wqfy02wBCsrKQkJf6HWzMtrrDRpErc01hPJP8",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("1b1e8afa-e689-4886-87a1-a340f6b18f20"),
			weekNumber = 2,
			title = "Vežbe protiv tikova očne regije",
			description = null,
			playbackId = "ekYrMIwOGWWBwue00SJzOfxanX2APkJ1QbS6g1u6tKVI",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),

		// Week 3
		ProgramExercise(
			id = UUID.fromString("9cad8950-c379-4251-890a-697b4968343f"),
			weekNumber = 3,
			title = "Vežbe protiv tikova očne regije",
			description = null,
			playbackId = "01B00Frwi8KAs8z02o6P02YwEJ00ZKIKGUCc01UesKcl8UVrI",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("91f4b22c-43d9-4413-ad96-e13306eb3276"),
			weekNumber = 3,
			title = "Progresivna relaksacija",
			description = null,
			playbackId = "BtqluLXNZf8bCNd13s8HLg8EtUFK9xT5GTXjQtj4Pd8",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),

		// Week 4
		ProgramExercise(
			id = UUID.fromString("fada6c0b-78f1-43ae-a61e-a943f15647e8"),
			weekNumber = 4,
			title = "Vežbe protiv tikova očne regije",
			description = null,
			playbackId = "9Iu8lJgg3JzHucSfrPwRvk9FB852RhcrUiGKiFfLCG00",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("b5aec508-8c17-4003-a7ff-74994bb93ed0"),
			weekNumber = 4,
			title = "Progresivna relaksacija",
			description = null,
			playbackId = "H3k1Z5sg9m01weNxiIqqNfOjawecGqfaNPu4zTUhemu8",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),

		// Week 5
		ProgramExercise(
			id = UUID.fromString("11b919a8-bedb-4321-92cf-c7d4ed343219"),
			weekNumber = 5,
			title = "Čitanje pravila edukacije sa ritmom",
			description = null,
			playbackId = "NYALF100QxOttoG02v2flVRcR8v6Fn9UR8tktA3mKdoiA",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("6a523493-1c23-4a6c-b3bd-35f7b5aed130"),
			weekNumber = 5,
			title = "Predilekciono pisanje",
			description = null,
			playbackId = "hqOwZ9euuPPrC6EpZ01oKz02mcV4CiTDbwzNhE37Y02UOk",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),

		// Week 6
		ProgramExercise(
			id = UUID.fromString("d6c3ffff-cc26-4174-9a45-ad2ab8f31ed9"),
			weekNumber = 6,
			title = "Predilekciono pisanje",
			description = null,
			playbackId = "X28gzL23RtOvnrvlRa7N2bry6m4NvvZOn6J3KW01qdRE",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("2ff667ca-da16-4fb7-b058-a574d4afb83e"),
			weekNumber = 6,
			title = "Vežbe za pažnju u Lazarovim krugovima",
			description = null,
			playbackId = "BqQWZZhnXWWbREy3q5EQk902BzS1bgOCyMiX01gpz9kiw",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),

		// Week 7
		ProgramExercise(
			id = UUID.fromString("92dffa84-a20c-4526-a8a6-e6669f2443b8"),
			weekNumber = 7,
			title = "Vežbe za pažnju u Lazarovim krugovima",
			description = null,
			playbackId = "nnU5TIpvFRASeunnqDLH7j7X8D2uBpALBsO8z21zEbQ",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("4d375a8b-6308-4d7d-a265-7e7bb5b7a2e5"),
			weekNumber = 7,
			title = "Vežbe imaginacije (pisanje slova A; 5x)",
			description = null,
			playbackId = "iDKrQc5X3cN1IG4d139vzPmMeA016hVOmn5MXysu4BWo",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),

		// Week 8
		ProgramExercise(
			id = UUID.fromString("8c43414b-fea9-48ae-b9b0-f0f7214d2188"),
			weekNumber = 8,
			title = "Vežbe za pažnju (brojanje od broja do broja)",
			description = null,
			playbackId = "WVlRE01013JVODg7PQwv9JFg02CrN11bwncLJZ4AnctAoQ",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("b8aaf9a7-f668-4847-9dc8-5e6a8c26fcd6"),
			weekNumber = 8,
			title = "Vežbe imaginacije (pisanje slova E; 5x)",
			description = null,
			playbackId = "HGzTKFXGdI8401nhtqnS7puZezE3029ppWT7bVb4dtjOM",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
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