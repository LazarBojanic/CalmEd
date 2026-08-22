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
import com.calmed.calmedbackend.model.raw.payment.PaymentTable
import com.calmed.calmedbackend.model.raw.payment.StoreEntitlementTable

fun main(args: Array<String>) {
	io.ktor.server.netty.EngineMain.main(args)
}

suspend fun Application.module() {
	val config = environment.config
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
		UserExerciseProgressTable,
		PaymentTable,
		StoreEntitlementTable
	)
	transaction {
		if (ktorConfig.development) {
			exec("DROP SCHEMA IF EXISTS public CASCADE;")
			exec("CREATE SCHEMA public;")
		}
	}
	transaction {
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
			description = null,
			playbackId = "ST6mjycO5DUVrDTuhkIPLgeuRD00zw3VDWIce008N602s00",
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
			description = null,
			playbackId = "jsXyROFxOqJAzK2F1qA1bKjWkhJ00AwDg9OBI9S00FPLc",
			thumbnailURL = null,
			visibility = Visibility.PUBLIC,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 1
		ProgramExercise(
			id = UUID.fromString("5a5cd72d-22d7-4c50-bee7-507da87fd162"),
			weekNumber = 1,
			title = "Reading the education rules",
			description = null,
			playbackId = "hrLiR6lbX5nhB7vkeGAq6pjLkPv4ue2rYYvfMNUVAkI",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("b7297e5d-15c0-4e86-9808-9ccba4bf2fdf"),
			weekNumber = 1,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "X5G01Gxzxlp2iKmISPV4W87xoQ00MFDrFMXBPRpK00EyYg",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 2
		ProgramExercise(
			id = UUID.fromString("563d5ed5-fb8b-4161-80db-29327e9925ee"),
			weekNumber = 2,
			title = "Reading the education rules with rhythm",
			description = null,
			playbackId = "bBK02sVARNWvAfL4nvRP3u7AB7AnRhrPyv3Muh7cxTBY",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("a170136a-7734-4530-b472-f1c3ab83344d"),
			weekNumber = 2,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "gb11dpPrhcSKNjOhu9EYHjxPWDXi6RBYKEqx9VSCN3g",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("83f78f4a-9977-4c48-83a9-efeaba95f4bd"),
			weekNumber = 2,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "GkFfW11TYcFZqqKjAj2t1WLPLf01Voeag2aoCsNSER014",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 3
		ProgramExercise(
			id = UUID.fromString("086c9125-c043-4aea-8392-71d6a68b777a"),
			weekNumber = 3,
			title = "Reading the education rules",
			description = null,
			playbackId = "WlfiT5L699SeuwyMiFPMqeR02yosBGvM95npHQ00UFKns",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("ff475432-138f-47ab-b7e7-fee833cec0e5"),
			weekNumber = 3,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "Waj2g8DlVw4g5dIpSE2kelkQcqn02fIvwtLg02CZiTXrw",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("391648d1-1694-4917-b7a0-5594f5e099b3"),
			weekNumber = 3,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "VDeu8h4i1xbG5wME2xzu02HKbPWh4i00fLvZEodoqHO6w",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("9875a53b-45bb-4846-8135-f670cc3cbe90"),
			weekNumber = 3,
			title = "Progressive relaxation",
			description = null,
			playbackId = "M01eloEqwOYuSIGfdtTcSKDA02AnVw8dQKKOrqD024GUIc",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 4
		ProgramExercise(
			id = UUID.fromString("85dce82f-10ec-4c57-a119-14236edc1cfb"),
			weekNumber = 4,
			title = "Reading the education rules",
			description = null,
			playbackId = "P9mthzXwl00ZS17Afm02zsZDO02J3JqA7zQM1nM2LuuozM",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("bff5f045-6863-4b1e-8416-0290e8670d45"),
			weekNumber = 4,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "QwAQ9u2m3lZGzzMkWv9B2khmfjKeyvTICUkdRTn002cA",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("798595b4-2986-4607-b746-41a6e8547b3b"),
			weekNumber = 4,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "WK0102tLGQgYEFdsCjLD28qpkWOBz8dYzZ1glCLDEt9c00",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("07abc46e-84e2-411f-a473-8cf766cf392e"),
			weekNumber = 4,
			title = "Progressive relaxation",
			description = null,
			playbackId = "y2tbv4gdMzSyfBtgtVl013KUxhQQr02u2erTTIbhKnLdM",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 5
		ProgramExercise(
			id = UUID.fromString("9e911c3f-55c0-42ae-a739-4976a0ddd72d"),
			weekNumber = 5,
			title = "Reading the education rules with rhythm",
			description = null,
			playbackId = "9H02da5tbO1AsMbU001qbNyZOVHHJqPrrdbUCJ02c01vnyw",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("eaf7345d-8e74-4a48-9156-34cc904f2a05"),
			weekNumber = 5,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "p4wMFl8kwa2Smalf01sQGr02TSPhkMBh3UAxT02mfjWKKs",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("800063ef-b31d-446d-860a-6c70faaf665d"),
			weekNumber = 5,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "kzTwOtCNreziLGlMv5LaaXWC2ygMTzyzi8kIvEHWXJs",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("0ac1c8f9-4aef-4151-b4b8-fa183f7c6a7a"),
			weekNumber = 5,
			title = "Pre-writing exercises",
			description = null,
			playbackId = "FlkwF3pw00XLsPFJOKr7usb92KHaFl01PoH9tGsFn2Jqs",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("fa711c27-7dd6-4d9c-8998-bd15b11c1039"),
			weekNumber = 5,
			title = "Progressive relaxation",
			description = null,
			playbackId = "JLv1q6iChlEYGITNbWQqj02FjZD01cJAhvF67s3RtKHjo",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 6
		ProgramExercise(
			id = UUID.fromString("056f0279-638e-4eec-98bf-c1520d36643c"),
			weekNumber = 6,
			title = "Reading the education rules with rhythm",
			description = null,
			playbackId = "bg5wgvkfM902baaRRISJGFjz3eHSkxsi8NkNAJCO00XPQ",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("18f0eb26-41a4-4d8c-8541-b796d7b57070"),
			weekNumber = 6,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "sXBO01kJt1vp8PFbgp3YCn2tjDkdcT7hhGvc8Z5dUxck",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("868e6cbe-a175-4d3e-a4e6-4f505471b410"),
			weekNumber = 6,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "KjpVS1P9s7RwSVL017W014Tkl00wEH9ueIwzI95ldcZAS8",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("fa46c85e-7e73-4829-8617-b950f1daa3af"),
			weekNumber = 6,
			title = "Pre-writing exercises",
			description = null,
			playbackId = "aPTh5PL7qBGApGoU9myPsYHJE6pJoQl01tctCd2tEtI00",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("bf4c3899-79aa-4d62-b972-65ff4e987460"),
			weekNumber = 6,
			title = "Attention exercises in Lazar’s circles",
			description = null,
			playbackId = "nMKPvm4GEtBEjliomgFLOxc01MRov024QLZP4n7msz4cM",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("7b4a5cf3-5b89-45d1-bdc1-861228420677"),
			weekNumber = 6,
			title = "Progressive relaxation",
			description = null,
			playbackId = "aXTHUGXMf7vhgtT6e5PrChZ3kI02WNlXYC02EZyIbZjd00",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 6,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 7
		ProgramExercise(
			id = UUID.fromString("b863f36d-6b97-4a2a-b03b-801a429bed54"),
			weekNumber = 7,
			title = "Reading the education rules with rhythm",
			description = null,
			playbackId = "x4sA9eVQzp5007aoyDzL01hUWzwi5ECnCS3mhketZrcG8",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("44bc4e10-ad4b-48e5-8266-78ff80ada027"),
			weekNumber = 7,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "9f02r3aMZrUSjUsVxWq00dwSi6X38Oo8Stp6JF9hmwLuk",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("222376d2-f60e-44bd-a969-e8eab8e569b5"),
			weekNumber = 7,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "KiYJbHmX024sdtKM100LPevtpBKccuoN02QeqNc1PdepFY",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("c3fd9dfc-7964-480e-a72b-cf6704b55406"),
			weekNumber = 7,
			title = "Attention exercises in Lazar’s circles",
			description = null,
			playbackId = "hY9K1OhvejFTMb00THkfMQjI57epxWEXbAqm00Q83d0002A",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("61a35db1-4c4f-461c-9559-683a3f814ca0"),
			weekNumber = 7,
			title = "Imagination exercises (writing the letter A; 5x)",
			description = null,
			playbackId = "ajY12Ki87cWA0000Wsxn4ujmgGeeC88OWyv28gObllR2w",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("5c998c98-4f4a-49b6-ac33-b95f317a100f"),
			weekNumber = 7,
			title = "Progressive relaxation",
			description = null,
			playbackId = "sAer02tV7rIVNIw6MOFZUDviWbnFJsDhT8r2SQbEfXKM",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 6,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 8
		ProgramExercise(
			id = UUID.fromString("8ab3a126-6952-4367-b90b-723fa9baa1c1"),
			weekNumber = 8,
			title = "Reading the education rules with rhythm",
			description = null,
			playbackId = "QrEz8UFnp01nUNItvH7hLMoYyKwDyG2Vd1g68YNt7l4Q",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("fb0407bc-9120-4c17-898c-ac4b7f107b73"),
			weekNumber = 8,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "hQgAdIp2qEEyRtWOrrXTwXS1nmgdVunKpu900SQ9pxh8",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("7e884aae-5c7d-461a-8739-0d03256a45ed"),
			weekNumber = 8,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "Cn6AyDaJeb02lbTGXZ46iADBKY7LEyzVqtHKtw9gxYX00",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("6a9efd42-9111-420c-ace1-a56dcb0b9fd3"),
			weekNumber = 8,
			title = "Attention exercises (counting from number to number)",
			description = null,
			playbackId = "CPDsbkF5WzcqFj800DPVjng7vnmkPOE3qqE02dPtsni01k",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("83b88b2e-67ca-46b1-846b-86b2c9195d50"),
			weekNumber = 8,
			title = "Imagination exercises (writing the letter E; 5x)",
			description = null,
			playbackId = "ntkjZDUbN9ZmOdXFuz5tlz8hiRc1OnER9t8vfraFIAI",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("d7d786f0-dc2d-4297-9dd2-d4aa80b4e449"),
			weekNumber = 8,
			title = "Progressive relaxation",
			description = null,
			playbackId = "1s2mAZmhZCi3c2iLZyRz64jj27MiS5OgeOxjjmAVWsw",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 6,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 9
		ProgramExercise(
			id = UUID.fromString("66c35ea3-f0c1-43cf-bf21-0f66a5f2b916"),
			weekNumber = 9,
			title = "Reading the education rules with rhythm",
			description = null,
			playbackId = "yCzM28EhWUWq1XIDg3IN00fsm5i00wGUtBcd9Zfvvqwag",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("d6301870-6a9e-41a2-a9a2-acc1da4f23c6"),
			weekNumber = 9,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "33BRwJFAIJaMnMUlbFNPnCt02kMFCeNxmy01Uxeq4GgL8",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("3abb9a4b-ffb0-40e5-879d-b41c44a4f61e"),
			weekNumber = 9,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "xQR9fScoUmWm01G61zCIyFbIoTDYZQz7SF9JoJpbxFCY",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("c6a85da3-adeb-4085-a55c-333385e88c95"),
			weekNumber = 9,
			title = "Attention exercises (counting from number to number)",
			description = null,
			playbackId = "rNHjjCQ8AhU016nDTdVX3TTUoOUY7VwwRbhjhD00702AVo",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("c43e095a-4bee-4055-be3b-0066b1fad05e"),
			weekNumber = 9,
			title = "Imagination exercises (writing the letter U; 3x3)",
			description = null,
			playbackId = "RrbZ4TAuDyo6iZcj02GkdzmVxyiuvaaOi02hsCXj33xWc",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("f6024b7b-8f8d-45cf-8abc-01a295e0fc79"),
			weekNumber = 9,
			title = "Progressive relaxation",
			description = null,
			playbackId = "26l5bdJVIuuDF008xC02ZJwfPrVfb01zNYfSBPOJDR8IFo",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 6,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 10
		ProgramExercise(
			id = UUID.fromString("ed650106-1cf6-4388-806a-d2cd465debf0"),
			weekNumber = 10,
			title = "Reading the education rules with rhythm",
			description = null,
			playbackId = "GFKVMmv8bXvPY6kGnWQm7U9xvXJQ1dOK4o696701g9Tw",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("6ad82282-8297-4719-8a57-9332dead8dfa"),
			weekNumber = 10,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "Pv9BRPW01wffcMF02MIlh6fEVL2YFdokrnLawJuk00mTL00",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("66087278-99a3-4eec-bf92-1b0799cfac26"),
			weekNumber = 10,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "YAAN00uRRq1ZggY6dSc100aI3BbS3bKBOLRcprbP7fwoI",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("b0c9d272-119d-4bc9-b1b3-e8cc9d198cbc"),
			weekNumber = 10,
			title = "Attention exercises (separating words in text)",
			description = null,
			playbackId = "8j5W8x02R4yKW02p5oPO5agk00DJvMNn8PTUiNvCKLFulI",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("6715ceb0-a812-485c-9870-1a3684234843"),
			weekNumber = 10,
			title = "Imagination exercises (writing the letter O; 3x3)",
			description = null,
			playbackId = "HJv0288EzYkju02w5QonCGcIkN73mHvYD43XKA9Fffg01Y",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("2c8c1f96-df65-4644-8dcd-029b3bfc7a27"),
			weekNumber = 10,
			title = "Progressive relaxation",
			description = null,
			playbackId = "ronGkgzCgWlIWO00tlFAbqapTsTAqV3QLowSPFwwwz5k",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 6,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 11
		ProgramExercise(
			id = UUID.fromString("3e35111d-b70d-4d9e-9e1b-8d1227d49587"),
			weekNumber = 11,
			title = "Reading the education rules with rhythm",
			description = null,
			playbackId = "tKYGjH4ZhMrCHNRTwwULmlZxSSHIE1JuR1IR1cRStwg",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("bc03202f-ce06-4a63-bead-3384a81fa19e"),
			weekNumber = 11,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "fuiciVvlDfo9LhCzfpv7vPpePEZ7wP02CoxUSFRdnHK4",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("3a601ba1-a0f6-4bdf-aa22-4d9556710431"),
			weekNumber = 11,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "8G003PdmV2JnsAV022ucbQXJkRriokNlkjTbYN8T6fwxk",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("6516eee4-e2cd-46da-a64a-c48c900d127c"),
			weekNumber = 11,
			title = "Attention exercises (separating words in text)",
			description = null,
			playbackId = "PG0102wn9J0258XHFe01aGSFyFbGTDskF8gVtNwyPsd008S00",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("9d87a996-267f-47b8-9366-bedb81030d4b"),
			weekNumber = 11,
			title = "Imagination exercises (writing the letter I; 3x3)",
			description = null,
			playbackId = "ByR9odu3VEht3UA2NwV6ZCozzGf00DH01oXYFRuyi3hDc",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("7daf90f0-80c9-42d4-8f57-abf7f880e9e6"),
			weekNumber = 11,
			title = "Progressive relaxation",
			description = null,
			playbackId = "mN6NsJcaU6s6i43ZiIJpQ4lxelQq02Zk8rZrkXK9svV8",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 6,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 12
		ProgramExercise(
			id = UUID.fromString("d405634b-fedb-44a2-be71-a67b36ca8c13"),
			weekNumber = 12,
			title = "Reading the education rules with rhythm",
			description = null,
			playbackId = "vGhoG8c02O00HXV7Y4wtc7eNJerds00NA00FquxxRwOWx5E",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("0408e2ba-f068-4172-855f-44ebf9d56829"),
			weekNumber = 12,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "UUZ0001aMWzt3bmlLn4l02AMd02HFDuejv5aew28D7zBPoo",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("a1c939a8-c142-44a9-8ba8-457b13947db3"),
			weekNumber = 12,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "m0201R9eHk00PgVsJfmlVGNlvUB0045zJtNaeE2luIT802sk",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("d3c60162-dacf-488b-9688-5a67a1a5514a"),
			weekNumber = 12,
			title = "Progressive relaxation",
			description = null,
			playbackId = "KQgzL6qd8TNB7XVkmoDJsYOs004N01Flkf009M63201HZvA",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 13
		ProgramExercise(
			id = UUID.fromString("5d59e18a-0fda-4145-9a0d-6cbf427c1b8e"),
			weekNumber = 13,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "tqYcgoPSOXxjgF9JMoVvxisYLDzIpAQnJZD01FLAF00CE",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("2f03a707-76b1-4131-9984-a594a72b86e1"),
			weekNumber = 13,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "TT5tzFF7Z5hLCbe32oBT8StuNT018XBd9mbsSOAmXiQM",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("55cb973e-d05d-4aae-baa5-861c40c79f1d"),
			weekNumber = 13,
			title = "Attention exercises (circling the letter “A” in text)",
			description = null,
			playbackId = "11RQIp83A88GifHVSl4roHDakO00AEOt3EbhxAAhWIWw",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("c08f700c-d274-4604-990c-4441666bc50b"),
			weekNumber = 13,
			title = "Hand/finger differentiation and integration exercises (Loop no. 1; 30x)",
			description = null,
			playbackId = "HHqKwbckhnbeeoa2oD8BJqFdIUnbpZUak5iNLuNQgf00",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("23c51a42-3c84-4d09-b78c-941c1e2d638a"),
			weekNumber = 13,
			title = "Exercises against generalized tics (dynamic balance: marching)",
			description = null,
			playbackId = "NZTLDdR1G1D1uOSPMVLO400FQIvWSYJONPI02ThxvefOw",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("daad8722-c1d8-4159-8e6c-a6a6c3a9fba5"),
			weekNumber = 13,
			title = "Progressive relaxation",
			description = null,
			playbackId = "DmhTOHdjnGJ012h8Vyt4AmMr7lEnW9tv6PY3NFqvpKn00",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 6,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 14
		ProgramExercise(
			id = UUID.fromString("fc6eb8d5-656b-47b0-9937-888ffdabec39"),
			weekNumber = 14,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "HzPM7KW023tr2OIJ025PPhlVNwDp7d02pB6MQcWeaEfJDA",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("3ca0f5f2-107c-4710-8a98-ed6276b07a0b"),
			weekNumber = 14,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "9X7UeoHpur9zscqLxOPlZNC5su5vJhxs2Q9pTCQIVMY",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("0dfbca32-78d1-4af9-8592-ddba7e1dbe6b"),
			weekNumber = 14,
			title = "Attention exercises (circling the letter “E” in text)",
			description = null,
			playbackId = "5oODAgNEo7kIBe9YvcNuGwT6lbM4Ekc6L2N01ndqXlyA",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("96e487c0-437c-482f-8c76-f0c23e68ddb7"),
			weekNumber = 14,
			title = "Hand/finger differentiation and integration exercises (Loop no. 2; 30x)",
			description = null,
			playbackId = "0146ku011XO11jNJ01t02t1PLqjqDXReDaLPBoMbA01wAXHo",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("42120df1-d242-4005-a6d2-88d4bc451dca"),
			weekNumber = 14,
			title = "Exercises against generalized tics (dynamic balance: marching, heel-to-toe walking)",
			description = null,
			playbackId = "l6YZGNXYa5vsZR9Ojqk3KukNGHnoFfHgMfJEVr6WzGc",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("33895bf5-c896-40f2-9e0c-535ed5da3476"),
			weekNumber = 14,
			title = "Progressive relaxation",
			description = null,
			playbackId = "dviNgfm017ioCa6jirh02GkJv4602200r4pfcW02ftQ942UQ",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 6,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 15
		ProgramExercise(
			id = UUID.fromString("b49d5e6c-4074-4501-bc18-f83608838621"),
			weekNumber = 15,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "jREnOzo6Vqdqel2NVXMEq4QJJhG01E4phXHV6yYgfjOs",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("1d61ef1a-5073-42cb-bf4a-456bdacead91"),
			weekNumber = 15,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "DWNpF00c01YcR00KnFdP6rztaYgkYWqqq9RVb02z5lzMRCo",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("77edee66-8ab8-4115-ae3c-3f00a25a7011"),
			weekNumber = 15,
			title = "Hand/finger differentiation and integration exercises (Loop no. 3; 30x)",
			description = null,
			playbackId = "rh3iqiYMHa5Yjgwftd6M2BsRdk7OB2F4lcBa3vnm1t00",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("8bef5ebc-fcec-4658-8db8-dfdc77c5c6cd"),
			weekNumber = 15,
			title = "Exercises against generalized tics (dynamic balance: marching, heel-to-toe walking, and crossing legs)",
			description = null,
			playbackId = "kbpd9wtfrYzE01ONGYvgN9QM402loAnXsRY4Qb9GYlUJ00",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("52d82d75-d4b0-4436-8d49-54d227dbf9e4"),
			weekNumber = 15,
			title = "Progressive relaxation",
			description = null,
			playbackId = "l2D1nGQDd1EgSPANFgIzRTho7L5E8ms7av9jE7TKiQY",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 16
		ProgramExercise(
			id = UUID.fromString("a21c74b6-dee8-4710-96a2-7e35cbf0ed62"),
			weekNumber = 16,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "mUAEFhJ9xy2ff1UsW6hu9s6YwzWYMEqhocj6MpRggeM",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("e9b64ba3-73a9-4260-a597-605d4a805b47"),
			weekNumber = 16,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "SnxjtorufdxV7LpXZ2qaZG4c6haM78qEIyjVbJ38C02g",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("eaeece3e-b462-4869-b059-222d0ddd37b9"),
			weekNumber = 16,
			title = "Hand/finger differentiation and integration exercises (Loop no. 4; 30x)",
			description = null,
			playbackId = "h029LpS5ODfxw38RpzXZZHnK2X00PqgnQhUt3Kt4Jsuxg",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("f86738c8-4f9f-4ad5-86cb-a511c810b814"),
			weekNumber = 16,
			title = "Exercises against generalized tics (static balance: “scale” pose)",
			description = null,
			playbackId = "mr005cW4GhaFYJuhdH02JzX54j9EwUKTxyFP75jGoxFS8",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("6537effa-7ab3-4b0b-a8d6-4b141ef2a64d"),
			weekNumber = 16,
			title = "Progressive relaxation",
			description = null,
			playbackId = "I1MCkQqoEyeqcH57EjVVaybkI2udxZk00Z9ejqA00wLTw",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 17
		ProgramExercise(
			id = UUID.fromString("e4ef8ef2-f391-4873-858d-650cb58dfc00"),
			weekNumber = 17,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "XUPGMm8b10101vqXbwsPNyNMXGqIiRwFs2vVVKi4fwr34",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("bc3a7d7c-5196-4d56-a5e8-fbc677748319"),
			weekNumber = 17,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "LjmfziDKALNhqq6XJyqrgoPcGq2DKo00WzPHfDm1Klco",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("6a1dc659-8152-451a-b872-fd1d367ba84a"),
			weekNumber = 17,
			title = "Attention exercises (threading a pencil through a circle 10x)",
			description = null,
			playbackId = "3M3L2m6f1t94VOcSpSby6LDC1ocEP2lFl00TloQZfAV8",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("e2952116-c462-414b-9fa7-8a308bfcaf51"),
			weekNumber = 17,
			title = "Exercises against generalized tics (static balance: “scale” pose, standing on toes up to 10)",
			description = null,
			playbackId = "AvqGrvicOrHrFYDUP59P8wXYRph6qC5VleVopgZ02hZI",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("5cd3e9e4-d485-4121-9142-a2ce0e716188"),
			weekNumber = 17,
			title = "Progressive relaxation",
			description = null,
			playbackId = "xcP8aDRfnlWuLi53K7nWzACn75AtR1anOLNAJ6AnmJI",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 18
		ProgramExercise(
			id = UUID.fromString("ef8672b6-71fb-4b47-90db-cc9da2a72007"),
			weekNumber = 18,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "iUMdS6AcSXFytAnayzbhO3p6fGaald4RJz4kJDI4yfA",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("d68a5677-7c01-4942-a6f6-c821b2980e38"),
			weekNumber = 18,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "t7g9SHR8fPj00rZNcfVlZfSghjFKJsk1ELfFjsWo9uLY",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("8d6b9def-f20b-4c2a-8b2d-b38fe511881a"),
			weekNumber = 18,
			title = "Attention exercises (threading a pencil through a circle 10x)",
			description = null,
			playbackId = "Neu026gMxoMjPw00DSApQcojc3K3Ri9lMExCvx7FoOAD8",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("73a5a66b-45d0-4e23-8af8-962569b36021"),
			weekNumber = 18,
			title = "Exercises against generalized tics (static balance: “scale” pose, standing on toes up to 10, standing on heels up to 10)",
			description = null,
			playbackId = "XzgqylTOOe3alIYjDEdg700EYoUYA7WrzTehkR1ZU00RM",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("366584bc-e2e5-448f-9a88-4b6dc2f27c46"),
			weekNumber = 18,
			title = "Progressive relaxation",
			description = null,
			playbackId = "Flfh9eeYDhmNX2lZQ9cIPNRdseaXSYX5wGnoHJAtCCg",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 19
		ProgramExercise(
			id = UUID.fromString("f47e20e1-5069-4a04-9c4c-74edae08323a"),
			weekNumber = 19,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "UVRDijD3yUX01WqWQbgH8HsrA4RcDTJzs01vIzIHagY8w",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("c0ef971c-6a89-4f82-8343-927c64c535d1"),
			weekNumber = 19,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "UmuE01yxWpcujqNFWpPaejAzU9gwMCjNN4PHiOsomWls",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("7e6e20c1-89ce-47e4-8e73-5aed7e0d91e4"),
			weekNumber = 19,
			title = "Exercises against oral-region tics (Matija’s cards)",
			description = null,
			playbackId = "LsZwu01s9ieM6OrkSxa7014VR1SRmqyfSSw8HAOSbu029U",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("4f35b444-4eda-4ba8-807b-673d5823b540"),
			weekNumber = 19,
			title = "“Figure eights” between the fingers",
			description = null,
			playbackId = "4z901fxvVAutDWkhGpT01P02WwFxJVKzptLDN83KRXiN01c",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("4c8305df-2d61-4b77-a9ed-27819aa67690"),
			weekNumber = 19,
			title = "Progressive relaxation",
			description = null,
			playbackId = "I7vrQFR3jmznTZ9CZFuQS7zlP6mU7012OyaX4Mufk02Mg",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 20
		ProgramExercise(
			id = UUID.fromString("0c102e16-88b2-42be-9d03-af3a809d776e"),
			weekNumber = 20,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "B1obsuWimEHW6uIhzGC901uhkXPXsPjJk00fjftcRwS9I",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("94274f77-99c7-400d-bb1b-3c23588d2c93"),
			weekNumber = 20,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "LvHJFO9R2h32fqrsjSXxazHCBp2QUzZB4uSh6rbOzTM",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("68d3fc81-22e0-44c1-a470-1ba90f08af89"),
			weekNumber = 20,
			title = "Exercises against oral-region tics (Matija’s cards)",
			description = null,
			playbackId = "JXyQRfI5yvlRoZMqyndubMNQ2egbF5mpKTQ2zgoAFO00",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("44c60cd4-d6e8-4c5e-90f7-fe4de1d3aa36"),
			weekNumber = 20,
			title = "“Figure eights” between the fingers",
			description = null,
			playbackId = "BiIGHvUJceaXRoKPsiAP01c9S9h101y76hJVxBmM2eQm4",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("7bc84b4d-bcfa-4b7f-bcb0-707b8547c1f9"),
			weekNumber = 20,
			title = "Progressive relaxation",
			description = null,
			playbackId = "TR01Wzl7nGbPYFIfZp007E5IidgX9rxp7Ei8j1qTod00jE",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 21
		ProgramExercise(
			id = UUID.fromString("5d68b9ff-2a19-4322-9bd6-86fe5b9c73db"),
			weekNumber = 21,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "hWE01jv8k8s02EKuFB02wiSN01HyN02rXm5v00jcNy01SkTv9M",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("a1f2e249-8f7d-4178-bf53-ea133261cdbe"),
			weekNumber = 21,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "znjJ5KKU4VgpKPNdlY2iP13DNxhyFg1AR3qWKpxAJqw",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("4e1b1d9f-94ae-44a4-ad44-70b30310c80e"),
			weekNumber = 21,
			title = "Exercises against oral-region tics (Matija’s cards)",
			description = null,
			playbackId = "rLnaAE01HpokKf5PFKdYrd9L3RnKFXdgvdjX3y6ARAWY",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("78c14b68-176d-4d96-8bec-0e99f53d4fe6"),
			weekNumber = 21,
			title = "“Piano” exercise",
			description = null,
			playbackId = "f4oj8F6289GAhuA1NHUNvyZ4CTwrJHB75vY00qbXIy6s",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("fbe0e944-d2a3-419e-ac39-519d0d37774a"),
			weekNumber = 21,
			title = "Progressive relaxation",
			description = null,
			playbackId = "uLFdOp02Y6OKhOVjpgJO7pLPsVJlZFSqEW6n1HZA024VA",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 22
		ProgramExercise(
			id = UUID.fromString("b512831d-812f-4eff-8a77-40b9877742c5"),
			weekNumber = 22,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "f66WyzCxATwFzldyUif3abJytJmKbnUb1ZmHDHfJu02M",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("44c63035-897d-4ea4-b697-93b7dc6e98c9"),
			weekNumber = 22,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "wHdkCkgJXEpEvmW9VfJgpIn4102DLjEMH1z2bSNeu1bM",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("65dcd9cc-141e-4acd-96b8-8f5a697a147c"),
			weekNumber = 22,
			title = "Exercises against oral-region tics (Matija’s cards)",
			description = null,
			playbackId = "np871sGwpjr6O2CschCu1rd02dmHYK85ee7OaGsihQT4",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("e757a4fb-74bb-412e-ad54-fd8e46ed5688"),
			weekNumber = 22,
			title = "“Piano” exercise",
			description = null,
			playbackId = "40202kiLF5MR01L1KdsbEPohKLUu2CO01TFazrkmcT89nZU",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("f690121f-d4de-433c-a4ca-d3eca4318e6e"),
			weekNumber = 22,
			title = "Progressive relaxation",
			description = null,
			playbackId = "00wecuiR01bYX3275exG8RYCthOtUtEnCWG02OYBYcI7ZE",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 23
		ProgramExercise(
			id = UUID.fromString("5ea0f5f0-3d15-4ac9-96e8-2988411d765f"),
			weekNumber = 23,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "X7x6dGc93KN5WPSMt4kFhGpJzO4HW3esRQBelVQlB01k",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("1c4a6391-fbcb-4eb7-a8f7-aa6a0a6425a2"),
			weekNumber = 23,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "sZkCOn02y4vZD6BLc00npLmxZMYF6yYWdmfUtjhHFY9pc",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("113ceff6-bbd4-4ab2-8c89-942e1847e266"),
			weekNumber = 23,
			title = "Exercises against shoulder-girdle tics (winding yarn)",
			description = null,
			playbackId = "aqZjt8hpXkAt1Xc68xsZeSf801Vw501AH02iiM6NfQJq9Q",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("a59815fb-079f-4048-8b38-422302b17312"),
			weekNumber = 23,
			title = "Exercises against shoulder-girdle tics (tokens)",
			description = null,
			playbackId = "qdRU1KFLinurNlpNr4Ndoa6q01gECQXL84elHkCdQFQc",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("ff822f98-3105-4482-a625-452d09189fb6"),
			weekNumber = 23,
			title = "Progressive relaxation",
			description = null,
			playbackId = "4jVd8mt01g600sLwkSi7gEIPvt2xsPZPaUpb8fupJTiQI",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 24
		ProgramExercise(
			id = UUID.fromString("6b3311db-33e8-49bb-9c14-15eca07282c4"),
			weekNumber = 24,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "xEqpOY01EYWxh9iS7J7uuIZrOFO6ucGOuUmyeoEn4SPk",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("acb5d854-6341-42f9-9514-83cdfa96986d"),
			weekNumber = 24,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "Tvby202EJDM2X28ArBYbTn01CoXH02FaKGgwFvbgAAmqqY",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("01842ef6-7555-438d-b659-6900a0c7e9ff"),
			weekNumber = 24,
			title = "Exercises against shoulder-girdle tics (rolling pin)",
			description = null,
			playbackId = "dlL3BqxuZ01ESYk1kMZ8dpKQZiDBVnYZELn5L2Jj0002G4",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("26f3f674-7936-4a3e-b088-37f437ee7c36"),
			weekNumber = 24,
			title = "Exercises against shoulder-girdle tics (boxing)",
			description = null,
			playbackId = "LE29h4KQ1IQTBvK7bYkHbIjTOf01TQJAoyLv02vn02abGQ",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("9a907e80-9de7-4cb2-a212-0e2ed749998a"),
			weekNumber = 24,
			title = "Progressive relaxation",
			description = null,
			playbackId = "78iJ201daKdZ6czVcckiu8VHfLrJjl9duZrD01TXQBn4g",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 5,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 25
		ProgramExercise(
			id = UUID.fromString("da303a18-3465-4f4f-a1dd-5af5fb5c3f34"),
			weekNumber = 25,
			title = "Body schema differentiation exercises",
			description = null,
			playbackId = "801d3IwybZAaI5HcTfiWEGIG02rL8FAkQLEOIrhZ48sx00",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 1,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("e51e235d-a8a5-48fd-a552-69660211fd0c"),
			weekNumber = 25,
			title = "Exercises against eye-region tics",
			description = null,
			playbackId = "RxQ1nTQto21kyZHo99gwoivSswNuV41RftW6rJN2vZ00",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 2,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("47c787b2-e64c-45b1-8c46-a36fda5b60f1"),
			weekNumber = 25,
			title = "Exercise of choice",
			description = null,
			playbackId = "i201g1EQIT5hZkYRV02K02VeLeC8H9iwoa00f9wh02iDQ84Y",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 3,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		ProgramExercise(
			id = UUID.fromString("736660a3-e940-4ee6-ace8-d397f9a3f3d2"),
			weekNumber = 25,
			title = "Progressive relaxation",
			description = null,
			playbackId = "WFWz5NGdd2pmVsg94gQkSFMFxA00oEaBrcQh01rVQsBrY",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			orderInWeek = 4,
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