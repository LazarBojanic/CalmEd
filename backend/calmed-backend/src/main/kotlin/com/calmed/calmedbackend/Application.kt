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
 		    title = "Introduction",
			titleEs =  "Introducción",
			description = null,
			playbackId = "yza0200syNTLqmW76p4i01n01DNRjpP302OKPRDb6nWH7K6A",
			playbackIdEs = "o02f6PH4nphA02e01dVRXZGypG02Y6Y7XkMXSGyLBWck2GM",
			thumbnailURL = null,
			visibility = Visibility.PUBLIC,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),

		ProgramExercise(
			id = UUID.fromString("d84f5be0-c5e1-4445-a102-3e16e1b32355"),
			weekNumber = 0,
			title = "Course overview",
			titleEs =  "Descripción general del curso",
			description = null,
			playbackId = "qiLxkby2JXpht2u6m7juSXBbIEIvLm5y01DIR2CEFhPY",
			playbackIdEs = "YIV9RjaFAoqpTlnBpXGxjYdlgpAFzodssCec01cC0202D8",
			thumbnailURL = null,
			visibility = Visibility.PUBLIC,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),

		// Week 1
		ProgramExercise(
			id = UUID.fromString("4d761512-a099-4a9f-bb64-c72232aec082"),
			weekNumber = 1,
			title = "Reading the education rules",
			titleEs = "Lectura de las reglas del entrenamiento",
			description = null,
			playbackId = "uuzZQ84KTfpl1sythkLjMzXMAOwG4Twp018Z9XnCr1rA",
			playbackIdEs = "UbKnbgy01gFLM2E01iIgn4VqXZviKC8s9sGYkrb00e1e00g",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("cf3fa3d2-3e3d-4faf-885f-d5264b8f294c"),
			weekNumber = 1,
			title = "Body schema differentiation exercises",
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "ckVz3Tu1r55I8y8dmR9AZOF01KcgpOTusvdFyfehepNQ",
			playbackIdEs = "xLLOw1qBhA1zb1qCQCNTJUCjj00VepeZWOOcvX8AtiaY",
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
			title = "Reading education rules with rhythm",
			titleEs = "Lectura de las reglas del entrenamiento con ritmo",
			description = null,
			playbackId = "vauO02f8CTtBnauyNk6DI3FmdVNI201Cj5WV4ZaIkNp01k",
			playbackIdEs = "RtCwn2wVwv00E8leoCUPjTNuKPur00Z01SZ01w00sFbVcVRY",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("1b1e8afa-e689-4886-87a1-a340f6b18f20"),
			weekNumber = 2,
			title = "Exercises against eye region tics",
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "2j02r8V2Y72LAs00WpZ8x9aY5RzsQw201bGBy8iDGmQdiE",
			playbackIdEs = "sy401KY1afHI8xy01jFqAdaa02owuOnQrfiZ4JE3VpQpCY",
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
			title = "Exercises against eye region tics",
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "BaWkDlVeNUQwkOMmJVuAs01nNCXmmooOlT8OYauCqYqE",
			playbackIdEs = "XoYuKxT00G3dD1tSt3j02UDwx56JoqcpU019eVScvTH9G8",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("91f4b22c-43d9-4413-ad96-e13306eb3276"),
			weekNumber = 3,
			title = "Progressive relaxation",
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "E02s7D02inmmWf5S6Vo00Rbv5pAEUaFJpxJ5xqpqZtxpgo",
			playbackIdEs = "tdKLRKQk692p8YkztpbZjvuq00mV3q9n3SnnJA7Gguos",
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
			title = "Exercises against eye region tics",
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "XeqLn4xXiRkr5bbi00xe1Oy4HEE002mKb8FyV02SoL6rEQ",
			playbackIdEs = "bWumIXSlmfzZ7SS7t0216OShTVi500fh7MoQg02Je02qWrU",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("b5aec508-8c17-4003-a7ff-74994bb93ed0"),
			weekNumber = 4,
			title = "Progressive relaxation",
			titleEs =  "Relajación progresiva",
			description = null,
			playbackId = "gnMJmS9FsEQ6Gk502Uj6WvQUIKY3bL00EaPIPBa16P7s8",
			playbackIdEs = "2L1aAuUSFhzpaIqJrmw101SR2V2UlMIvAi6rZij44SAg",
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
			title = "Reading education rules with rhythm",
			titleEs = "Lectura de las reglas del entrenamiento con ritmo",
			description = null,
			playbackId = "Ppxu9aXKTGE41Jg9kxNnVeLPHvy5zzhhUnZW013npqD00",
			playbackIdEs = "uWql0001n01h1oEgDqaEZtD00IAkLzd3OuyOJABkfgZwUww",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("6a523493-1c23-4a6c-b3bd-35f7b5aed130"),
			weekNumber = 5,
			title = "Pre-lesson writing",
			titleEs = "Escritura previa a la lección",
			description = null,
			playbackId = "YBUttzEhG8skplf02EBLeQhQ6Oovxw00ojo010101a89qVtw",
			playbackIdEs = "1E1M1PrxAw02Gi9dqoy2MLIyshjJFL21vUmYpUhXhOY00",
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
			title = "Pre-lesson writing",
			titleEs = "Escritura previa a la lección",
			description = null,
			playbackId = "002EblzN4b7aIVrxp3qsU9xHMLhm8OBb6dix15xoun00w",
			playbackIdEs = "r01yfExihi5S3589mYqYzh2og6NBppOeapPZEfyTkYLw",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("2ff667ca-da16-4fb7-b058-a574d4afb83e"),
			weekNumber = 6,
			title = "Attention exercises in Lazarov's circles",
			titleEs = "Ejercicios de atención en los círculos de Lazarov",
			description = null,
			playbackId = "e8UehTKQgchIxy00sUaLph7j6GjEicq2Mg4FhbmaL3Rc",
			playbackIdEs = "JM57yhz00ewZzEaWjvpjHw01Yqwcn45fAOkMmCo8MqOkA",
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
			title = "Attention exercises in Lazarov's circles",
			titleEs = "Ejercicios de atención en los círculos de Lazarov",
			description = null,
			playbackId = "W6LNDerGRFQ0002u302i627IGcI02GQyHG02sYF00fyFa2CAA",
			playbackIdEs = "tm9AapCeNXghSWzH3486LjLmvgF8RBLumScdsP02P02rg",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("4d375a8b-6308-4d7d-a265-7e7bb5b7a2e5"),
			weekNumber = 7,
			title = "Imagination exercises (writing the letter A; 5x)",
			titleEs = "Ejercicios de imaginación (escritura de la letra A; 5x)",
			description = null,
			playbackId = "cklloJi92a6w4FN5k9i2j5i2WLmAiS7ypsGqF1nmN5U",
			playbackIdEs = "AHeFserXYj5ZC01Y1LLZ02prYDYrLS1brwjAI4Ou5GObo",
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
			title = "Attention exercises (counting from number to number)",
			titleEs = "Ejercicios de atención (contar de un número a otro)",
			description = null,
			playbackId = "nlNY101LHcE01wsb5ErrsC9AG6gAE9hwo4rlBifWEu6JY",
			playbackIdEs = "ygXypeM99HPj5TWBe7CuiXQH2gzqY3c1WpTjfGdApFc",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("b8aaf9a7-f668-4847-9dc8-5e6a8c26fcd6"),
			weekNumber = 8,
			title = "Imagination exercises (writing the letter E; 5x)",
			titleEs = "Ejercicios de imaginación (escritura de la letra E; 5x)",
			description = null,
			playbackId = "u5BC5ChQlioowU800oiOen7URkz01H8PJWQR8IfqrfHO00",
			playbackIdEs = "qDyEGEQK1vMz2O1a02k8eKJfcMg6gaY5Js01YCaAdjxKk",
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