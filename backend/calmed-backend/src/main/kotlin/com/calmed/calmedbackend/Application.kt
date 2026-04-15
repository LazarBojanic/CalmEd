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
			titleEs = "Introducción",
			description = null,
			playbackId = "ST6mjycO5DUVrDTuhkIPLgeuRD00zw3VDWIce008N602s00",
			playbackIdEs = "1COK4EB5RIuxYjrKS3oh1npbeuOuF7Qk3rr2028yznWg",
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
			titleEs = "Descripción general del curso",
			description = null,
			playbackId = "jsXyROFxOqJAzK2F1qA1bKjWkhJ00AwDg9OBI9S00FPLc",
			playbackIdEs = "Ay002Kztj301ZhCsVRo1see4h4ga0001A9Knz6qCHttjZYo",
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
			titleEs = "Lectura de las reglas del entrenamiento",
			description = null,
			playbackId = "hrLiR6lbX5nhB7vkeGAq6pjLkPv4ue2rYYvfMNUVAkI",
			playbackIdEs = "300wlMZGhW01l0146KzmlevfOdNVg022MQZsFiiCl02d7b4E",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "X5G01Gxzxlp2iKmISPV4W87xoQ00MFDrFMXBPRpK00EyYg",
			playbackIdEs = "zTDSazoNhrHfu0047eRrJgKupENPHuw9anhz900LWzvNE",
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
			titleEs = "Lectura de las reglas del entrenamiento con ritmo",
			description = null,
			playbackId = "bBK02sVARNWvAfL4nvRP3u7AB7AnRhrPyv3Muh7cxTBY",
			playbackIdEs = "9JbUCZA00uwQiJYIIlGOGIzcf1n1ir01LRhj1DiXAr1PA",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "gb11dpPrhcSKNjOhu9EYHjxPWDXi6RBYKEqx9VSCN3g",
			playbackIdEs = "zTXccdIgheDrWM5aen00cxjjFhwPRsuvUX7WFZnh0102004",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "GkFfW11TYcFZqqKjAj2t1WLPLf01Voeag2aoCsNSER014",
			playbackIdEs = "zs2STyMIuyQmcdKBOY61VqccU54GhWBxJUv8WYBsFzI",
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
			titleEs = "Lectura de las reglas del entrenamiento",
			description = null,
			playbackId = "WlfiT5L699SeuwyMiFPMqeR02yosBGvM95npHQ00UFKns",
			playbackIdEs = "qSUUmkMcPN6H00lJrVIMg01c7O00wETHxL83rhD1Kn01hrk",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "Waj2g8DlVw4g5dIpSE2kelkQcqn02fIvwtLg02CZiTXrw",
			playbackIdEs = "Ct8sI900wC7UNB01kjtpWTbl9BhgUC02aAtRwDk2022a02Hs",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "VDeu8h4i1xbG5wME2xzu02HKbPWh4i00fLvZEodoqHO6w",
			playbackIdEs = "752c9DaERYQJhvkaNIdu502lHQt8AdoTuLv1H026WPm5M",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "M01eloEqwOYuSIGfdtTcSKDA02AnVw8dQKKOrqD024GUIc",
			playbackIdEs = "sA3eqixYC282Y6Hh02HP5x5wVczAcRZSBeLhSobS00wBM",
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
			titleEs = "Lectura de las reglas del entrenamiento",
			description = null,
			playbackId = "P9mthzXwl00ZS17Afm02zsZDO02J3JqA7zQM1nM2LuuozM",
			playbackIdEs = "3ZwInIyWUoCH01zo01m6ia701usnkt6z9Er44h35e5HYxo",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "QwAQ9u2m3lZGzzMkWv9B2khmfjKeyvTICUkdRTn002cA",
			playbackIdEs = "MQrNIlFot6cMwh5Rb66wJ9y02ww8Pwlx7oT3F5haoWRE",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "WK0102tLGQgYEFdsCjLD28qpkWOBz8dYzZ1glCLDEt9c00",
			playbackIdEs = "tF00fQuLbZIZylTUaGDzS8n3X1q00c600LqnYxOKv6mHOA",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "y2tbv4gdMzSyfBtgtVl013KUxhQQr02u2erTTIbhKnLdM",
			playbackIdEs = "4joWFWWNfaCvGslkFuS02VtFK7Ld2yh7LBqpsH67TvbI",
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
			titleEs = "Lectura de las reglas del entrenamiento con ritmo",
			description = null,
			playbackId = "9H02da5tbO1AsMbU001qbNyZOVHHJqPrrdbUCJ02c01vnyw",
			playbackIdEs = "82vzkbvvFdUyMGy5tp6M3GL02jkeGbZdBrabD01ms5M7g",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "p4wMFl8kwa2Smalf01sQGr02TSPhkMBh3UAxT02mfjWKKs",
			playbackIdEs = "700SuNHrnu2qC2AMHfgeyMhGy00Vmfun7EKb3fnuxokxI",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "kzTwOtCNreziLGlMv5LaaXWC2ygMTzyzi8kIvEHWXJs",
			playbackIdEs = "gg9wwk68OJG7WRG6LODxsZjd02mwGMlyj01jAbDzP00WHU",
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
			titleEs = "Escritura previa a la lección",
			description = null,
			playbackId = "FlkwF3pw00XLsPFJOKr7usb92KHaFl01PoH9tGsFn2Jqs",
			playbackIdEs = "Hz2r6X4IRu3QmPx01NnG3cvO3Jyo6DogE9WcZJqjOhdY",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "JLv1q6iChlEYGITNbWQqj02FjZD01cJAhvF67s3RtKHjo",
			playbackIdEs = "kfmq6jq8bVoHzjmIEvf02XnqGC9vCh7R6ujs0125c9Dg8",
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
			titleEs = "Lectura de las reglas del entrenamiento con ritmo",
			description = null,
			playbackId = "bg5wgvkfM902baaRRISJGFjz3eHSkxsi8NkNAJCO00XPQ",
			playbackIdEs = "yUMXE0248Kr902hNtZ5j5F00f1JkocfiaZVOBZPau6kms8",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "sXBO01kJt1vp8PFbgp3YCn2tjDkdcT7hhGvc8Z5dUxck",
			playbackIdEs = "lHjHx2UJ0202241zhbcWr6ulkVdDpUEjO9b019xbbJVZVQ",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "KjpVS1P9s7RwSVL017W014Tkl00wEH9ueIwzI95ldcZAS8",
			playbackIdEs = "hk602TN28e8db00015UC813OheWrJumcKKuXesLarggcjQ",
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
			titleEs = "Escritura previa a la lección",
			description = null,
			playbackId = "aPTh5PL7qBGApGoU9myPsYHJE6pJoQl01tctCd2tEtI00",
			playbackIdEs = "002NW02TW4MHIyvVvf02QI1MqaJ2ec4700pyF1j3IZ1nfiE",
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
			titleEs = "Ejercicios de atención en los círculos de Lazarov",
			description = null,
			playbackId = "nMKPvm4GEtBEjliomgFLOxc01MRov024QLZP4n7msz4cM",
			playbackIdEs = "tXXnlInOEAS5O018uKOhwgNIJ7wgZyAm2G3rP2sq029C4",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "aXTHUGXMf7vhgtT6e5PrChZ3kI02WNlXYC02EZyIbZjd00",
			playbackIdEs = "DAeXJ5Ls700jfAZXODbQ00iO5kEU7XWLPK2xxPu5WTeC00",
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
			titleEs = "Lectura de las reglas del entrenamiento con ritmo",
			description = null,
			playbackId = "x4sA9eVQzp5007aoyDzL01hUWzwi5ECnCS3mhketZrcG8",
			playbackIdEs = "cRzMMS00szs8MA02h01XMffYtQviZDD2yCFDMKEUPUpuTo",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "9f02r3aMZrUSjUsVxWq00dwSi6X38Oo8Stp6JF9hmwLuk",
			playbackIdEs = "017nJbL3sA01VWOLQE6XPrVZdIvVoOjaqrHti004nzHwBo",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "KiYJbHmX024sdtKM100LPevtpBKccuoN02QeqNc1PdepFY",
			playbackIdEs = "NsVQZnKwpFfxy4zdFBu8ONq6nNghWVjfu62MeCmrylg",
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
			titleEs = "Ejercicios de atención en los círculos de Lazarov",
			description = null,
			playbackId = "hY9K1OhvejFTMb00THkfMQjI57epxWEXbAqm00Q83d0002A",
			playbackIdEs = "GvJYN8biFTJQEvNTatEiW8XVmP1jkRGhNyLjb7AIC3E",
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
			titleEs = "Ejercicios de imaginación (escritura de la letra A; 5x)",
			description = null,
			playbackId = "ajY12Ki87cWA0000Wsxn4ujmgGeeC88OWyv28gObllR2w",
			playbackIdEs = "6yFDdWOLpmX5Ge2If7T004x00KmgZhUj7eNY5C0002kdadM",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "sAer02tV7rIVNIw6MOFZUDviWbnFJsDhT8r2SQbEfXKM",
			playbackIdEs = "Z4h5rkfYC6o8R5sqm7WKJnifBO8W003huZT3ZzVuepZo",
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
			titleEs = "Lectura de las reglas del entrenamiento con ritmo",
			description = null,
			playbackId = "QrEz8UFnp01nUNItvH7hLMoYyKwDyG2Vd1g68YNt7l4Q",
			playbackIdEs = "n5443f6KazYoNmi2Pl01PKjupOGIAInH02BEJmKD7mI00g",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "hQgAdIp2qEEyRtWOrrXTwXS1nmgdVunKpu900SQ9pxh8",
			playbackIdEs = "xiVPY5cKNsIFdF2CWYtclWhDRyxzyumE3mlpNfeKy4s",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "Cn6AyDaJeb02lbTGXZ46iADBKY7LEyzVqtHKtw9gxYX00",
			playbackIdEs = "T6VujBhNtoSWl65hI5fmx02a5G01OchZ7BdiLsEfw02yGw",
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
			titleEs = "Ejercicios de atención (contar de un número a otro)",
			description = null,
			playbackId = "CPDsbkF5WzcqFj800DPVjng7vnmkPOE3qqE02dPtsni01k",
			playbackIdEs = "UFLN6s34iKounrhW302fPRPREImpQbXJk6pq6q00ciGUg",
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
			titleEs = "Ejercicios de imaginación (escritura de la letra E; 5x)",
			description = null,
			playbackId = "ntkjZDUbN9ZmOdXFuz5tlz8hiRc1OnER9t8vfraFIAI",
			playbackIdEs = "Juo00871g2DQPyt5h4NqytBqm01tPBRO4lIEXOjtQaNPE",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "1s2mAZmhZCi3c2iLZyRz64jj27MiS5OgeOxjjmAVWsw",
			playbackIdEs = "vwrzxKDick7Z502l00AlQ88hM6UvAdlugbhYHbO3KnN02w",
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
			titleEs = "Lectura de las reglas del entrenamiento con ritmo",
			description = null,
			playbackId = "yCzM28EhWUWq1XIDg3IN00fsm5i00wGUtBcd9Zfvvqwag",
			playbackIdEs = "lGOCbyqHj6hi00oYJpMSce9G01HaSU5v7CiF005PnDDsAo",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "33BRwJFAIJaMnMUlbFNPnCt02kMFCeNxmy01Uxeq4GgL8",
			playbackIdEs = "fpgGYgO602AO18YPyRv3qZhu66qXDcxSw00VPPxjEddf4",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "xQR9fScoUmWm01G61zCIyFbIoTDYZQz7SF9JoJpbxFCY",
			playbackIdEs = "tGag2J2ukrAQTKcSkb8i8VDUGxHWQ00MGSPDw99vpTnc",
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
			titleEs = "Ejercicios de atención (contar de un número a otro)",
			description = null,
			playbackId = "rNHjjCQ8AhU016nDTdVX3TTUoOUY7VwwRbhjhD00702AVo",
			playbackIdEs = "zVgDyFKmuNJ23Va7PtZtv1k1oIezAxySdefRz00HSI2g",
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
			titleEs = "Ejercicios de imaginación (escritura de la letra U; 3x3)",
			description = null,
			playbackId = "RrbZ4TAuDyo6iZcj02GkdzmVxyiuvaaOi02hsCXj33xWc",
			playbackIdEs = "KLASPzbUqCLktZUmbGYJJ01EnryDYRVZstgtut0000BcuQ",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "26l5bdJVIuuDF008xC02ZJwfPrVfb01zNYfSBPOJDR8IFo",
			playbackIdEs = "EQST3Ah1I2900UlxTL5Cz92kKfs5VOdclpBYCJmEIqrc",
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
			titleEs = "Lectura de las reglas del entrenamiento con ritmo",
			description = null,
			playbackId = "GFKVMmv8bXvPY6kGnWQm7U9xvXJQ1dOK4o696701g9Tw",
			playbackIdEs = "SPNbX9jIMIoE602LtQULiXUlh7vGgkVEKICW00G01KcjfI",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "Pv9BRPW01wffcMF02MIlh6fEVL2YFdokrnLawJuk00mTL00",
			playbackIdEs = "AZYHd3PeCLws02T3XaYhHfi00n3nJnKxbm02bTIqLzawPA",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "YAAN00uRRq1ZggY6dSc100aI3BbS3bKBOLRcprbP7fwoI",
			playbackIdEs = "Jrznd7RVlS2wsK7WG7HoTST4btpmSCDefPxu1IEJlZw",
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
			titleEs = "Ejercicios de atención (separar palabras en el texto)",
			description = null,
			playbackId = "8j5W8x02R4yKW02p5oPO5agk00DJvMNn8PTUiNvCKLFulI",
			playbackIdEs = "lpaYLV7g5xL00X7ZeUyBXfGzcxvR01DjjUrWSLzmSmct8",
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
			titleEs = "Ejercicios de imaginación (escritura de la letra O; 3x3)",
			description = null,
			playbackId = "HJv0288EzYkju02w5QonCGcIkN73mHvYD43XKA9Fffg01Y",
			playbackIdEs = "EyYBH5x228KvIab5NzbGjykH01rg5ZbD52dmX4g6Dz34",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "ronGkgzCgWlIWO00tlFAbqapTsTAqV3QLowSPFwwwz5k",
			playbackIdEs = "475DWLMAGm00J1Ai1lFMjBEcVAadwH9ZCxygboerGFhM",
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
			titleEs = "Lectura de las reglas del entrenamiento con ritmo",
			description = null,
			playbackId = "tKYGjH4ZhMrCHNRTwwULmlZxSSHIE1JuR1IR1cRStwg",
			playbackIdEs = "peNPGWeyC3jx01U01Bb6QC1NByAoa502M01DfIOhHtWA0200A",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "fuiciVvlDfo9LhCzfpv7vPpePEZ7wP02CoxUSFRdnHK4",
			playbackIdEs = "YbLAOKwKo1qFvAVbd1X1pIXsXuaJOthnPdn5LW00OTUs",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "8G003PdmV2JnsAV022ucbQXJkRriokNlkjTbYN8T6fwxk",
			playbackIdEs = "3zDDsAIJfue3LG1rs9k5szpkcElLVPWqNn7sWJc1FfU",
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
			titleEs = "Ejercicios de atención (separar palabras en el texto)",
			description = null,
			playbackId = "PG0102wn9J0258XHFe01aGSFyFbGTDskF8gVtNwyPsd008S00",
			playbackIdEs = "LEOH6nwWD3acqNRgayCHBZm7tbPQ7t00XRsb900jRZNnY",
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
			titleEs = "Ejercicios de imaginación (escritura de la letra I; 3x3)",
			description = null,
			playbackId = "ByR9odu3VEht3UA2NwV6ZCozzGf00DH01oXYFRuyi3hDc",
			playbackIdEs = "9HyORiCmUmviqWOZAMWmlVghaaBk01k7hZCZIXeooPLQ",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "mN6NsJcaU6s6i43ZiIJpQ4lxelQq02Zk8rZrkXK9svV8",
			playbackIdEs = "01CfCuHADoFkvBHE00oRjx2aLmf2a3CwPURwvdnLBEINc",
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
			titleEs = "Lectura de las reglas del entrenamiento con ritmo",
			description = null,
			playbackId = "vGhoG8c02O00HXV7Y4wtc7eNJerds00NA00FquxxRwOWx5E",
			playbackIdEs = "1XxjTH16vG56634ULsqiF99Qhbc01O00GoOvO5Wp02oNTg",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "UUZ0001aMWzt3bmlLn4l02AMd02HFDuejv5aew28D7zBPoo",
			playbackIdEs = "huT9jX3WoQe6urRpy7C01tIoQUH6odN9R2c31iWfyKwU",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "m0201R9eHk00PgVsJfmlVGNlvUB0045zJtNaeE2luIT802sk",
			playbackIdEs = "gbkJ8HvL01Zu6m3nfUlThXih2mjyiRtcq02FqJhjKlFOE",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "KQgzL6qd8TNB7XVkmoDJsYOs004N01Flkf009M63201HZvA",
			playbackIdEs = "01P02egGNtt0000AZ2f00o1kd6b01hfDhSSESfvncLbouYL78",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "tqYcgoPSOXxjgF9JMoVvxisYLDzIpAQnJZD01FLAF00CE",
			playbackIdEs = "I00m3VumGx02iHgZsTSpx4r4iLK5IC1JQRo01uJBffrlQ4",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "TT5tzFF7Z5hLCbe32oBT8StuNT018XBd9mbsSOAmXiQM",
			playbackIdEs = "MPKY9S4euQ4InNkdAUeV29JSS67UEQD01d6NX00Uy9FII",
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
			titleEs = "Ejercicios de atención (marcar la letra “A” en el texto)",
			description = null,
			playbackId = "11RQIp83A88GifHVSl4roHDakO00AEOt3EbhxAAhWIWw",
			playbackIdEs = "QharBMbNBVy4fhBsj7kg02702eXmZLNAkLMDaGhOnDlfE",
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
			titleEs = "Ejercicios de diferenciación e integración de mano/dedos (Bucle no. 1; 30x)",
			description = null,
			playbackId = "HHqKwbckhnbeeoa2oD8BJqFdIUnbpZUak5iNLuNQgf00",
			playbackIdEs = "KSDlR8s70002SQabpm301dWphJJ8U3RZQw14wBQGv1KWPk",
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
			titleEs = "Ejercicios contra tics generalizados (equilibrio dinámico: marchar)",
			description = null,
			playbackId = "NZTLDdR1G1D1uOSPMVLO400FQIvWSYJONPI02ThxvefOw",
			playbackIdEs = "4oWx01kFQIigoY1nR01rF02ntyIFTOV8Wtd01wvHEbbcjms",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "DmhTOHdjnGJ012h8Vyt4AmMr7lEnW9tv6PY3NFqvpKn00",
			playbackIdEs = "02oXFFZvfl9ikCToBC90102rncqUC008firDmf7FOi9simQ",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "HzPM7KW023tr2OIJ025PPhlVNwDp7d02pB6MQcWeaEfJDA",
			playbackIdEs = "61mlC00vp01XZVU7X45YZnx73Yj77cGs2fYEuVCCWewKY",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "9X7UeoHpur9zscqLxOPlZNC5su5vJhxs2Q9pTCQIVMY",
			playbackIdEs = "g4B4dqfU12029dqXpNS1QmyTQ5dqcw1YStFiMeAJLUPc",
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
			titleEs = "Ejercicios de atención (marcar la letra “E” en el texto)",
			description = null,
			playbackId = "5oODAgNEo7kIBe9YvcNuGwT6lbM4Ekc6L2N01ndqXlyA",
			playbackIdEs = "b5017qNll4ocKTliLI6UyTGy6hGfpfp4HTn3jl602oFdI",
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
			titleEs = "Ejercicios de diferenciación e integración de mano/dedos (Bucle no. 2; 30x)",
			description = null,
			playbackId = "0146ku011XO11jNJ01t02t1PLqjqDXReDaLPBoMbA01wAXHo",
			playbackIdEs = "R00lOaXQJjdaEJwKaCE96kwOJf00Vdg23ksdHAQqtWscQ",
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
			titleEs = "Ejercicios contra tics generalizados (equilibrio dinámico: marchar, caminar talón-punta)",
			description = null,
			playbackId = "l6YZGNXYa5vsZR9Ojqk3KukNGHnoFfHgMfJEVr6WzGc",
			playbackIdEs = "bfcIh901EJiPhTaxHEBnU3gKQzYjMzUH01doZEzrLVJEM",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "dviNgfm017ioCa6jirh02GkJv4602200r4pfcW02ftQ942UQ",
			playbackIdEs = "1tfwh8KIPs00YkIH5AxTS6ajiEyk9r6lRkpmhx4Xf7oE",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "jREnOzo6Vqdqel2NVXMEq4QJJhG01E4phXHV6yYgfjOs",
			playbackIdEs = "7G8jdmh8YeXLeVzwQC7tiCYhAj00cnQdUetvPsy9DHY8",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "DWNpF00c01YcR00KnFdP6rztaYgkYWqqq9RVb02z5lzMRCo",
			playbackIdEs = "LhI3uCXEFzCCXbQ3aF5ETU1f5ny00pnCmJnBRdy6CZQU",
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
			titleEs = "Ejercicios de diferenciación e integración de mano/dedos (Bucle no. 3; 30x)",
			description = null,
			playbackId = "rh3iqiYMHa5Yjgwftd6M2BsRdk7OB2F4lcBa3vnm1t00",
			playbackIdEs = "X4pwSdku3X2bjwuTyW11zIM8Aa5zNHWQezoy01tqVxeo",
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
			titleEs = "Ejercicios contra tics generalizados (equilibrio dinámico: marchar, caminar talón-punta y cruzar piernas)",
			description = null,
			playbackId = "kbpd9wtfrYzE01ONGYvgN9QM402loAnXsRY4Qb9GYlUJ00",
			playbackIdEs = "FmV9vwuuXV2eQ9UvcmLE9t4xLhwjpu7r02ubISLxZagQ",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "l2D1nGQDd1EgSPANFgIzRTho7L5E8ms7av9jE7TKiQY",
			playbackIdEs = "WoWBkwY8iNuiY0023yPxcw5jcN2jFWmaBgYHsaeIbu8U",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "mUAEFhJ9xy2ff1UsW6hu9s6YwzWYMEqhocj6MpRggeM",
			playbackIdEs = "qdsdBBv01o00tj7yFLvjaWo2Vjup9Q9PKfEI8wcg9ZCoQ",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "SnxjtorufdxV7LpXZ2qaZG4c6haM78qEIyjVbJ38C02g",
			playbackIdEs = "17geTldxnVvgYAf8wKhIQZ8d1m6lCM5V3TZCESv101kY",
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
			titleEs = "Ejercicios de diferenciación e integración de mano/dedos (Bucle no. 4; 30x)",
			description = null,
			playbackId = "h029LpS5ODfxw38RpzXZZHnK2X00PqgnQhUt3Kt4Jsuxg",
			playbackIdEs = "8fGlMcD1YKc8z4YkIszTLx65VL01RRzvg54O4J1Xvaxw",
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
			titleEs = "Ejercicios contra tics generalizados (equilibrio estático: postura de “balanza”)",
			description = null,
			playbackId = "mr005cW4GhaFYJuhdH02JzX54j9EwUKTxyFP75jGoxFS8",
			playbackIdEs = "E4FtCJrI6cpZP59hMsRyhx5lOScJE02djJc8TD4Gdj44",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "I1MCkQqoEyeqcH57EjVVaybkI2udxZk00Z9ejqA00wLTw",
			playbackIdEs = "fzu1W6bPBdLOhT00cQBnkH9h9tuMV9CxdsLQaZhu7dJg",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "XUPGMm8b10101vqXbwsPNyNMXGqIiRwFs2vVVKi4fwr34",
			playbackIdEs = "ZWKTbnRLxEoSRZA7x6IKEPwDN2OPBSfz00ANRm01R2WMM",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "LjmfziDKALNhqq6XJyqrgoPcGq2DKo00WzPHfDm1Klco",
			playbackIdEs = "dKaIrRnCOJhNxDbtSYmLY600zNwwriv01ViujdTL02rCN4",
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
			titleEs = "Ejercicios de atención (pasar un lápiz por un círculo 10x)",
			description = null,
			playbackId = "3M3L2m6f1t94VOcSpSby6LDC1ocEP2lFl00TloQZfAV8",
			playbackIdEs = "NodLlmtnH6azPvNBkDQzwQMQCybWi7I6A2livXnkl300",
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
			titleEs = "Ejercicios contra tics generalizados (equilibrio estático: postura de “balanza”, de puntillas hasta 10)",
			description = null,
			playbackId = "AvqGrvicOrHrFYDUP59P8wXYRph6qC5VleVopgZ02hZI",
			playbackIdEs = "uVbd7ZlnW02Bep6ZYq8mOWT95rCfItCNEoVkSBa1qmiM",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "xcP8aDRfnlWuLi53K7nWzACn75AtR1anOLNAJ6AnmJI",
			playbackIdEs = "c6YvBqLcsVsmk6JEv1vUATzyrdgxRbMvZic01NfX801ss",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "iUMdS6AcSXFytAnayzbhO3p6fGaald4RJz4kJDI4yfA",
			playbackIdEs = "6nkmUTf0111AUXjMULn4LUgcTpb2ql502uj8cZ62Qo5tA",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "t7g9SHR8fPj00rZNcfVlZfSghjFKJsk1ELfFjsWo9uLY",
			playbackIdEs = "T42bkH8RqSJjxZbRCwz9ALuJ51AxiAxQaz7LqKxR2dY",
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
			titleEs = "Ejercicios de atención (pasar un lápiz por un círculo 10x)",
			description = null,
			playbackId = "Neu026gMxoMjPw00DSApQcojc3K3Ri9lMExCvx7FoOAD8",
			playbackIdEs = "Wjgdhdl3u01bZF2CpdUSoWNc4700avsJWbhBPPhYRmbJA",
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
			titleEs = "Ejercicios contra tics generalizados (equilibrio estático: postura de “balanza”, de puntillas hasta 10, sobre talones hasta 10)",
			description = null,
			playbackId = "XzgqylTOOe3alIYjDEdg700EYoUYA7WrzTehkR1ZU00RM",
			playbackIdEs = "HGdNG00ig7aDAxyOLk01HXly9Ifs8R3ZFZeCVeFWdJOMc",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "Flfh9eeYDhmNX2lZQ9cIPNRdseaXSYX5wGnoHJAtCCg",
			playbackIdEs = "oxBUwAv7SkM01iglSosdFrpK900018leRUpaYTjVzDO9hw",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "UVRDijD3yUX01WqWQbgH8HsrA4RcDTJzs01vIzIHagY8w",
			playbackIdEs = "8vNcF8FP006ltiTqqzGeDMe6SFHaep02E01dkHWx01J2qfo",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "UmuE01yxWpcujqNFWpPaejAzU9gwMCjNN4PHiOsomWls",
			playbackIdEs = "yAzp5X7102OBmALsku5ZylaEd7CpwmaXgiXZXgUfVeME",
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
			titleEs = "Ejercicios contra los tics de la región oral (Tarjetas de Matija)",
			description = null,
			playbackId = "LsZwu01s9ieM6OrkSxa7014VR1SRmqyfSSw8HAOSbu029U",
			playbackIdEs = "s3KCwC3UuzkPGugqf9heOOp7wT6eT5guX7enwOQU01028",
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
			titleEs = "“Ochos” entre los dedos",
			description = null,
			playbackId = "4z901fxvVAutDWkhGpT01P02WwFxJVKzptLDN83KRXiN01c",
			playbackIdEs = "IB2R702c00ZSexkSV0101W003a00MMXdZWmFfm4J5WX8e6sC8",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "I7vrQFR3jmznTZ9CZFuQS7zlP6mU7012OyaX4Mufk02Mg",
			playbackIdEs = "Ceh9C01rRr1fvfN00BvTCrs01802yAFkYQEvD16uNXNLKBU",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "B1obsuWimEHW6uIhzGC901uhkXPXsPjJk00fjftcRwS9I",
			playbackIdEs = "gEZi2zfT00yQoLvFbk02HXUDvORrOi3JFcr7G2cmwIy1o",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "LvHJFO9R2h32fqrsjSXxazHCBp2QUzZB4uSh6rbOzTM",
			playbackIdEs = "Tc2ii6xIC02M00L2y8tnHRg4vF1JTtLJwQjLSfjxOrHeY",
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
			titleEs = "Ejercicios contra los tics de la región oral (Tarjetas de Matija)",
			description = null,
			playbackId = "JXyQRfI5yvlRoZMqyndubMNQ2egbF5mpKTQ2zgoAFO00",
			playbackIdEs = "vc53GStKI3UsvniVbH6E11ZRmX02vqaZ9fuDqFWk9oIE",
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
			titleEs = "“Ochos” entre los dedos",
			description = null,
			playbackId = "BiIGHvUJceaXRoKPsiAP01c9S9h101y76hJVxBmM2eQm4",
			playbackIdEs = "IW02ED9WkvwKywwm3x85yzpZ8Mix57gMKIlLwCDxqDmM",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "TR01Wzl7nGbPYFIfZp007E5IidgX9rxp7Ei8j1qTod00jE",
			playbackIdEs = "LxrzeiyguNBRNQnT4iAuile3Rc8vl4009SkWaCKefxSo",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "hWE01jv8k8s02EKuFB02wiSN01HyN02rXm5v00jcNy01SkTv9M",
			playbackIdEs = "SkoAdKHwbwzxo4AOVkbwdg0253WXkKDpYDikexMZ02ztg",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "znjJ5KKU4VgpKPNdlY2iP13DNxhyFg1AR3qWKpxAJqw",
			playbackIdEs = "dT7wdMO8ffMzhFrpJN1Qd8015ZwFD00XuUddmd201MxyZ4",
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
			titleEs = "Ejercicios contra los tics de la región oral (Tarjetas de Matija)",
			description = null,
			playbackId = "rLnaAE01HpokKf5PFKdYrd9L3RnKFXdgvdjX3y6ARAWY",
			playbackIdEs = "CFPackemZEpno4cHsKN76do1wN8E3JEd8tlmE028DP1o",
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
			titleEs = "Ejercicio de “Piano”",
			description = null,
			playbackId = "f4oj8F6289GAhuA1NHUNvyZ4CTwrJHB75vY00qbXIy6s",
			playbackIdEs = "6Z4syJxiyYC8cnAaEZgszcNG7ZqyxzDOnBq6oqtTEqw",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "uLFdOp02Y6OKhOVjpgJO7pLPsVJlZFSqEW6n1HZA024VA",
			playbackIdEs = "qEpGAcc01VWf7bJAN47nZypt5SMgcsiqMvGn00MIOvQzc",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "f66WyzCxATwFzldyUif3abJytJmKbnUb1ZmHDHfJu02M",
			playbackIdEs = "02l7LUz3i02kra023h02v2701UtVGIiQalPCPAWxGcBErawM",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "wHdkCkgJXEpEvmW9VfJgpIn4102DLjEMH1z2bSNeu1bM",
			playbackIdEs = "eQ8ZqRYJNChWLpcGFxwYr02TzUZotEeeiFrzlEfxbk3Y",
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
			titleEs = "Ejercicios contra los tics de la región oral (Tarjetas de Matija)",
			description = null,
			playbackId = "np871sGwpjr6O2CschCu1rd02dmHYK85ee7OaGsihQT4",
			playbackIdEs = "wFkLEQGY77by5zdfTwcXpHhrCxItL1MD91EtyzG01YzU",
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
			titleEs = "Ejercicio de “Piano”",
			description = null,
			playbackId = "40202kiLF5MR01L1KdsbEPohKLUu2CO01TFazrkmcT89nZU",
			playbackIdEs = "02Ti4Ud6JuKKmAKFSG4MS2hVLc4U3QGn4q81bfkfXgTg",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "00wecuiR01bYX3275exG8RYCthOtUtEnCWG02OYBYcI7ZE",
			playbackIdEs = "2gYg9Xeczdi3INuov7Cu44Z201owSE71WZStYyI9QiUs",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "X7x6dGc93KN5WPSMt4kFhGpJzO4HW3esRQBelVQlB01k",
			playbackIdEs = "mWEfyczodVVRP01NufG9kKT2jYqgP02R6J1T8R7ZXFdRM",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "sZkCOn02y4vZD6BLc00npLmxZMYF6yYWdmfUtjhHFY9pc",
			playbackIdEs = "DvGD6Myk4bb7OcgEaY98KG17P00AECeow2vmLc02PZI5o",
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
			titleEs = "Ejercicios contra tics de la cintura escapular (enrollar lana)",
			description = null,
			playbackId = "aqZjt8hpXkAt1Xc68xsZeSf801Vw501AH02iiM6NfQJq9Q",
			playbackIdEs = "ZvlyOxtP00GUrf800lKAK56Jq6Y017MIdMS02lV88PzVAWo",
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
			titleEs = "Ejercicios contra tics de la cintura escapular (fichas)",
			description = null,
			playbackId = "qdRU1KFLinurNlpNr4Ndoa6q01gECQXL84elHkCdQFQc",
			playbackIdEs = "xAKEXdpIYwj8fR8d6kN4AMnhkr3C901pICIc02no00NZS4",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "4jVd8mt01g600sLwkSi7gEIPvt2xsPZPaUpb8fupJTiQI",
			playbackIdEs = "t3cHftUug02zUGOgDrvrOk7Zr2TDzhlCC7nKY1h402WkE",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "xEqpOY01EYWxh9iS7J7uuIZrOFO6ucGOuUmyeoEn4SPk",
			playbackIdEs = "U1LXOzGDtrPzZdglHFnENM4Ya6qwHTn01pFA9FcDeNX4",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "Tvby202EJDM2X28ArBYbTn01CoXH02FaKGgwFvbgAAmqqY",
			playbackIdEs = "l00ioMlZpmSmZnNfkwVYoWOByc59j3WzL01TiBXUyQyrU",
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
			titleEs = "Ejercicios contra tics de la cintura escapular (rodillo)",
			description = null,
			playbackId = "dlL3BqxuZ01ESYk1kMZ8dpKQZiDBVnYZELn5L2Jj0002G4",
			playbackIdEs = "013O01opENY1MmMPPWSL01fXdM1uja8k7dNBvRkx7fU5Fc",
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
			titleEs = "Ejercicios contra tics de la cintura escapular (boxeo)",
			description = null,
			playbackId = "LE29h4KQ1IQTBvK7bYkHbIjTOf01TQJAoyLv02vn02abGQ",
			playbackIdEs = "67500rvRK3ngxhVv4dQVIYMcfYdv1jPK2DPK802imy5E4",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "78iJ201daKdZ6czVcckiu8VHfLrJjl9duZrD01TXQBn4g",
			playbackIdEs = "Z802DTDIdQ7jMlEaNNOcC00agXfB7hRK004yTjjXX97xqA",
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
			titleEs = "Ejercicios de diferenciación del esquema corporal",
			description = null,
			playbackId = "801d3IwybZAaI5HcTfiWEGIG02rL8FAkQLEOIrhZ48sx00",
			playbackIdEs = "Ywcukgg7Sb4xjxz00d00vfywA1yn266K3llXneQuGS1J00",
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
			titleEs = "Ejercicios contra los tics de la región ocular",
			description = null,
			playbackId = "RxQ1nTQto21kyZHo99gwoivSswNuV41RftW6rJN2vZ00",
			playbackIdEs = "vj02Yb601ZE7v3Hezr00gliDTIIMp5kTa1DxICbLKE02dEg",
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
			titleEs = "Ejercicio de elección",
			description = null,
			playbackId = "i201g1EQIT5hZkYRV02K02VeLeC8H9iwoa00f9wh02iDQ84Y",
			playbackIdEs = "E0000euZ9701dZu13jIE88RuWEw5hU01QeCZjSD02N8JdX0100",
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
			titleEs = "Relajación progresiva",
			description = null,
			playbackId = "WFWz5NGdd2pmVsg94gQkSFMFxA00oEaBrcQh01rVQsBrY",
			playbackIdEs = "8z7eQlYdAj1dagmt1rGnDezduclaL5sHEo02QAu1mSqQ",
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