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
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 1
		ProgramExercise(
			id = UUID.fromString("5a5cd72d-22d7-4c50-bee7-507da87fd162"),
			weekNumber = 1,
			title = "Week 1",
			description = null,
			playbackId = "hrLiR6lbX5nhB7vkeGAq6pjLkPv4ue2rYYvfMNUVAkI",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 2
		ProgramExercise(
			id = UUID.fromString("563d5ed5-fb8b-4161-80db-29327e9925ee"),
			weekNumber = 2,
			title = "Week 2",
			description = null,
			playbackId = "bBK02sVARNWvAfL4nvRP3u7AB7AnRhrPyv3Muh7cxTBY",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 3
		ProgramExercise(
			id = UUID.fromString("086c9125-c043-4aea-8392-71d6a68b777a"),
			weekNumber = 3,
			title = "Week 3",
			description = null,
			playbackId = "WlfiT5L699SeuwyMiFPMqeR02yosBGvM95npHQ00UFKns",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 4
		ProgramExercise(
			id = UUID.fromString("85dce82f-10ec-4c57-a119-14236edc1cfb"),
			weekNumber = 4,
			title = "Week 4",
			description = null,
			playbackId = "P9mthzXwl00ZS17Afm02zsZDO02J3JqA7zQM1nM2LuuozM",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 5
		ProgramExercise(
			id = UUID.fromString("9e911c3f-55c0-42ae-a739-4976a0ddd72d"),
			weekNumber = 5,
			title = "Week 5",
			description = null,
			playbackId = "9H02da5tbO1AsMbU001qbNyZOVHHJqPrrdbUCJ02c01vnyw",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 6
		ProgramExercise(
			id = UUID.fromString("056f0279-638e-4eec-98bf-c1520d36643c"),
			weekNumber = 6,
			title = "Week 6",
			description = null,
			playbackId = "bg5wgvkfM902baaRRISJGFjz3eHSkxsi8NkNAJCO00XPQ",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 7
		ProgramExercise(
			id = UUID.fromString("b863f36d-6b97-4a2a-b03b-801a429bed54"),
			weekNumber = 7,
			title = "Week 7",
			description = null,
			playbackId = "x4sA9eVQzp5007aoyDzL01hUWzwi5ECnCS3mhketZrcG8",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 8
		ProgramExercise(
			id = UUID.fromString("8ab3a126-6952-4367-b90b-723fa9baa1c1"),
			weekNumber = 8,
			title = "Week 8",
			description = null,
			playbackId = "QrEz8UFnp01nUNItvH7hLMoYyKwDyG2Vd1g68YNt7l4Q",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 9
		ProgramExercise(
			id = UUID.fromString("66c35ea3-f0c1-43cf-bf21-0f66a5f2b916"),
			weekNumber = 9,
			title = "Week 9",
			description = null,
			playbackId = "yCzM28EhWUWq1XIDg3IN00fsm5i00wGUtBcd9Zfvvqwag",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 10
		ProgramExercise(
			id = UUID.fromString("ed650106-1cf6-4388-806a-d2cd465debf0"),
			weekNumber = 10,
			title = "Week 10",
			description = null,
			playbackId = "GFKVMmv8bXvPY6kGnWQm7U9xvXJQ1dOK4o696701g9Tw",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 11
		ProgramExercise(
			id = UUID.fromString("3e35111d-b70d-4d9e-9e1b-8d1227d49587"),
			weekNumber = 11,
			title = "Week 11",
			description = null,
			playbackId = "tKYGjH4ZhMrCHNRTwwULmlZxSSHIE1JuR1IR1cRStwg",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 12
		ProgramExercise(
			id = UUID.fromString("d405634b-fedb-44a2-be71-a67b36ca8c13"),
			weekNumber = 12,
			title = "Week 12",
			description = null,
			playbackId = "vGhoG8c02O00HXV7Y4wtc7eNJerds00NA00FquxxRwOWx5E",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 13
		ProgramExercise(
			id = UUID.fromString("5d59e18a-0fda-4145-9a0d-6cbf427c1b8e"),
			weekNumber = 13,
			title = "Week 13",
			description = null,
			playbackId = "tqYcgoPSOXxjgF9JMoVvxisYLDzIpAQnJZD01FLAF00CE",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 14
		ProgramExercise(
			id = UUID.fromString("fc6eb8d5-656b-47b0-9937-888ffdabec39"),
			weekNumber = 14,
			title = "Week 14",
			description = null,
			playbackId = "HzPM7KW023tr2OIJ025PPhlVNwDp7d02pB6MQcWeaEfJDA",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 15
		ProgramExercise(
			id = UUID.fromString("b49d5e6c-4074-4501-bc18-f83608838621"),
			weekNumber = 15,
			title = "Week 15",
			description = null,
			playbackId = "jREnOzo6Vqdqel2NVXMEq4QJJhG01E4phXHV6yYgfjOs",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 16
		ProgramExercise(
			id = UUID.fromString("a21c74b6-dee8-4710-96a2-7e35cbf0ed62"),
			weekNumber = 16,
			title = "Week 16",
			description = null,
			playbackId = "mUAEFhJ9xy2ff1UsW6hu9s6YwzWYMEqhocj6MpRggeM",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 17
		ProgramExercise(
			id = UUID.fromString("e4ef8ef2-f391-4873-858d-650cb58dfc00"),
			weekNumber = 17,
			title = "Week 17",
			description = null,
			playbackId = "XUPGMm8b10101vqXbwsPNyNMXGqIiRwFs2vVVKi4fwr34",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 18
		ProgramExercise(
			id = UUID.fromString("ef8672b6-71fb-4b47-90db-cc9da2a72007"),
			weekNumber = 18,
			title = "Week 18",
			description = null,
			playbackId = "iUMdS6AcSXFytAnayzbhO3p6fGaald4RJz4kJDI4yfA",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 19
		ProgramExercise(
			id = UUID.fromString("f47e20e1-5069-4a04-9c4c-74edae08323a"),
			weekNumber = 19,
			title = "Week 19",
			description = null,
			playbackId = "UVRDijD3yUX01WqWQbgH8HsrA4RcDTJzs01vIzIHagY8w",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 20
		ProgramExercise(
			id = UUID.fromString("0c102e16-88b2-42be-9d03-af3a809d776e"),
			weekNumber = 20,
			title = "Week 20",
			description = null,
			playbackId = "B1obsuWimEHW6uIhzGC901uhkXPXsPjJk00fjftcRwS9I",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 21
		ProgramExercise(
			id = UUID.fromString("5d68b9ff-2a19-4322-9bd6-86fe5b9c73db"),
			weekNumber = 21,
			title = "Week 21",
			description = null,
			playbackId = "hWE01jv8k8s02EKuFB02wiSN01HyN02rXm5v00jcNy01SkTv9M",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 22
		ProgramExercise(
			id = UUID.fromString("b512831d-812f-4eff-8a77-40b9877742c5"),
			weekNumber = 22,
			title = "Week 22",
			description = null,
			playbackId = "f66WyzCxATwFzldyUif3abJytJmKbnUb1ZmHDHfJu02M",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 23
		ProgramExercise(
			id = UUID.fromString("5ea0f5f0-3d15-4ac9-96e8-2988411d765f"),
			weekNumber = 23,
			title = "Week 23",
			description = null,
			playbackId = "X7x6dGc93KN5WPSMt4kFhGpJzO4HW3esRQBelVQlB01k",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 24
		ProgramExercise(
			id = UUID.fromString("6b3311db-33e8-49bb-9c14-15eca07282c4"),
			weekNumber = 24,
			title = "Week 24",
			description = null,
			playbackId = "xEqpOY01EYWxh9iS7J7uuIZrOFO6ucGOuUmyeoEn4SPk",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		),
		// Week 25
		ProgramExercise(
			id = UUID.fromString("da303a18-3465-4f4f-a1dd-5af5fb5c3f34"),
			weekNumber = 25,
			title = "Week 25",
			description = null,
			playbackId = "801d3IwybZAaI5HcTfiWEGIG02rL8FAkQLEOIrhZ48sx00",
			thumbnailURL = null,
			visibility = Visibility.SIGNED,
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