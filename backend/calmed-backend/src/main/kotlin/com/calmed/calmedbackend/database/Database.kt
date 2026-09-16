package com.calmed.calmedbackend.database

import com.calmed.calmedbackend.config.DatabaseConfig
import com.calmed.calmedbackend.config.KtorConfig
import com.calmed.calmedbackend.model.MapMode
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialTable
import com.calmed.calmedbackend.model.raw.exercisegroup.ExerciseGroup
import com.calmed.calmedbackend.model.raw.exercisegroup.ExerciseGroupEntity
import com.calmed.calmedbackend.model.raw.exercisegroup.ExerciseGroupTable
import com.calmed.calmedbackend.model.raw.payment.PaymentTable
import com.calmed.calmedbackend.model.raw.storeentitlement.StoreEntitlementTable
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExercise
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExerciseEntity
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExerciseTable
import com.calmed.calmedbackend.model.raw.programexercise.Visibility
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshTokenTable
import com.calmed.calmedbackend.model.raw.user.UserTable
import com.calmed.calmedbackend.model.raw.userexerciseprogress.UserExerciseProgressTable
import com.calmed.calmedbackend.model.raw.userinfo.tics.UserInfoTicsTable
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.raw.userprogram.UserProgramTable
import com.calmed.calmedbackend.model.setFrom
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.ktor.ext.inject
import java.time.Instant
import java.util.UUID

fun seed() {
	val seedGroups = listOf(
		ExerciseGroup(id = 0, name = "Getting Started", description = null),
		ExerciseGroup(id = 1, name = "Group 1", description = "Week 1-4"),
		ExerciseGroup(id = 2, name = "Group 2", description = "Week 5-12"),
		ExerciseGroup(id = 3, name = "Group 3", description = "Week 13-18"),
		ExerciseGroup(id = 4, name = "Group 4", description = "Week 19-24"),
		ExerciseGroup(id = 5, name = "Final Group", description = "Week 25+")
	)

	transaction {
		for (group in seedGroups) {
			val existing = ExerciseGroupEntity.findById(group.id)
			if (existing == null) {
				ExerciseGroupEntity.new(group.id) {
					name = group.name
					description = group.description
				}
			} else {
				existing.name = group.name
				existing.description = group.description
			}
		}
	}
	val seedExercises = listOf(
		ProgramExercise(
			id = UUID.fromString("3a420f83-c314-4731-b319-310c94e55752"),
			weekNumber = 0,
			title = "Introduction",
			description = null,
			playbackId = "ST6mjycO5DUVrDTuhkIPLgeuRD00zw3VDWIce008N602s00",
			previewPlaybackId = "ST6mjycO5DUVrDTuhkIPLgeuRD00zw3VDWIce008N602s00",
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
			previewPlaybackId = "jsXyROFxOqJAzK2F1qA1bKjWkhJ00AwDg9OBI9S00FPLc",
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
			playbackId = "nr00548DGIXJq9ek9IuABQ4oV7hdIenQRvsWYA7DKGk4",
			previewPlaybackId = "hrLiR6lbX5nhB7vkeGAq6pjLkPv4ue2rYYvfMNUVAkI",
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
			previewPlaybackId = "bBK02sVARNWvAfL4nvRP3u7AB7AnRhrPyv3Muh7cxTBY",
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
			previewPlaybackId = "WlfiT5L699SeuwyMiFPMqeR02yosBGvM95npHQ00UFKns",
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
			previewPlaybackId = "P9mthzXwl00ZS17Afm02zsZDO02J3JqA7zQM1nM2LuuozM",
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
			previewPlaybackId = "9H02da5tbO1AsMbU001qbNyZOVHHJqPrrdbUCJ02c01vnyw",
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
			previewPlaybackId = "bg5wgvkfM902baaRRISJGFjz3eHSkxsi8NkNAJCO00XPQ",
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
			previewPlaybackId = "x4sA9eVQzp5007aoyDzL01hUWzwi5ECnCS3mhketZrcG8",
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
			previewPlaybackId = "QrEz8UFnp01nUNItvH7hLMoYyKwDyG2Vd1g68YNt7l4Q",
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
			previewPlaybackId = "yCzM28EhWUWq1XIDg3IN00fsm5i00wGUtBcd9Zfvvqwag",
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
			previewPlaybackId = "GFKVMmv8bXvPY6kGnWQm7U9xvXJQ1dOK4o696701g9Tw",
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
			previewPlaybackId = "tKYGjH4ZhMrCHNRTwwULmlZxSSHIE1JuR1IR1cRStwg",
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
			previewPlaybackId = "vGhoG8c02O00HXV7Y4wtc7eNJerds00NA00FquxxRwOWx5E",
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
			previewPlaybackId = "tqYcgoPSOXxjgF9JMoVvxisYLDzIpAQnJZD01FLAF00CE",
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
			previewPlaybackId = "HzPM7KW023tr2OIJ025PPhlVNwDp7d02pB6MQcWeaEfJDA",
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
			previewPlaybackId = "jREnOzo6Vqdqel2NVXMEq4QJJhG01E4phXHV6yYgfjOs",
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
			previewPlaybackId = "mUAEFhJ9xy2ff1UsW6hu9s6YwzWYMEqhocj6MpRggeM",
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
			previewPlaybackId = "XUPGMm8b10101vqXbwsPNyNMXGqIiRwFs2vVVKi4fwr34",
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
			previewPlaybackId = "iUMdS6AcSXFytAnayzbhO3p6fGaald4RJz4kJDI4yfA",
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
			previewPlaybackId = "UVRDijD3yUX01WqWQbgH8HsrA4RcDTJzs01vIzIHagY8w",
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
			previewPlaybackId = "B1obsuWimEHW6uIhzGC901uhkXPXsPjJk00fjftcRwS9I",
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
			previewPlaybackId = "hWE01jv8k8s02EKuFB02wiSN01HyN02rXm5v00jcNy01SkTv9M",
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
			previewPlaybackId = "f66WyzCxATwFzldyUif3abJytJmKbnUb1ZmHDHfJu02M",
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
			previewPlaybackId = "X7x6dGc93KN5WPSMt4kFhGpJzO4HW3esRQBelVQlB01k",
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
			previewPlaybackId = "xEqpOY01EYWxh9iS7J7uuIZrOFO6ucGOuUmyeoEn4SPk",
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
			previewPlaybackId = "801d3IwybZAaI5HcTfiWEGIG02rL8FAkQLEOIrhZ48sx00",
			visibility = Visibility.SIGNED,
			createdAt = Instant.now(),
			updatedAt = Instant.now()
		)
	)

	transaction {
		for (ex in seedExercises) {
			val groupId = groupIdForWeek(ex.weekNumber)
			val existing = ProgramExerciseEntity.findById(ex.id)
			if (existing == null) {
				ProgramExerciseEntity.new(ex.id) {
					setFrom(ex.copy(groupId = groupId), MapMode.CREATE)
				}
			} else if (existing.groupId == null) {
				existing.groupId = groupId
			}
		}
	}
}

private fun groupIdForWeek(week: Int): Int {
	return when {
		week <= 0 -> 0
		week <= 4 -> 1
		week <= 12 -> 2
		week <= 18 -> 3
		week <= 24 -> 4
		else -> 5
	}
}

val databaseTables: Array<Table> = arrayOf(
	UserTable,
	AuthCredentialTable,
	RefreshTokenTable,
	UserInfoTicsTable,
	UserProgramTable,
	ProgramExerciseTable,
	UserExerciseProgressTable,
	PaymentTable,
	StoreEntitlementTable,
	ExerciseGroupTable
)

fun Application.configureDatabase() {
	val databaseConfig by inject<DatabaseConfig>()
	val ktorConfig by inject<KtorConfig>()
	val dataSource = hikariDataSource(databaseConfig)

	Database.connect(
		dataSource
	)
	if (databaseConfig.recreate) {
		check(ktorConfig.development) {
			"DATABASE_RECREATE is enabled but ktor.development is false. Refusing to drop all tables outside development."
		}
		dropAllTables()
	}

	if (databaseConfig.useFlyway) {
		val flyway = Flyway.configure()
			.dataSource(dataSource)
			.load()
		flyway.migrate()
	} else {
		transaction {
			SchemaUtils.createMissingTablesAndColumns(*databaseTables)
		}
	}
	if (databaseConfig.recreate) {
		seed()
	}
}

fun dropAllTables() {
	transaction {
		exec("DROP SCHEMA IF EXISTS public CASCADE;")
		exec("CREATE SCHEMA public;")
	}
}

private fun hikariDataSource(databaseConfig: DatabaseConfig): HikariDataSource {
	val config = HikariConfig().apply {
		driverClassName = databaseConfig.databaseDriver
		jdbcUrl = databaseConfig.databaseUrl

		username = databaseConfig.databaseUsername
		password = databaseConfig.databasePassword

		maximumPoolSize = 5
		minimumIdle = 1

		isAutoCommit = false
		transactionIsolation = "TRANSACTION_REPEATABLE_READ"

		connectionTimeout = 10_000
		initializationFailTimeout = 10_000
	}

	return HikariDataSource(config)
}

suspend fun <T> withTransaction(block: suspend () -> T): T {
	return suspendTransaction { block() }
}

private class TransactionRollback(val result: AppResult.Failure) : RuntimeException()

suspend fun <T> withResultTransaction(block: suspend () -> AppResult<T>): AppResult<T> {
	if (TransactionManager.currentOrNull() != null) {
		return block()
	}
	try {
		return suspendTransaction {
			val result = block()
			if (result is AppResult.Failure) throw TransactionRollback(result)
			result
		}
	} catch (e: TransactionRollback) {
		return e.result
	}
}
