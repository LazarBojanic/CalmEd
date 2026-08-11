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
		PaymentTable
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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
			id = UUID.randomUUID(),
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