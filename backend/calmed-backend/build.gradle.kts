import org.jetbrains.exposed.v1.gradle.plugin.GenerateMigrationsTask
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.ktor)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.exposed)
}

group = "com.calmed"
version = "0.0.1"

application {
	mainClass = "io.ktor.server.netty.EngineMain"
}
java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

dependencies {
	// Ktor Server & Client Bundles
	implementation(libs.bundles.ktor.server)
	implementation(libs.bundles.ktor.client)
	implementation(libs.ktor.serialization.kotlinx.json)

	// Database Stack Bundle & Drivers
	implementation(libs.bundles.exposed)
	implementation(libs.postgresql)
	implementation(libs.hikaricp)
	implementation(libs.flyway.postgres)

	// Dependency Injection
	implementation(libs.koin.ktor)
	implementation(libs.koin.logger.slf4j)

	// Email Utilities
	implementation(libs.commons.email)
	implementation(libs.commons.validator)
	implementation(libs.mailtrap)

	// Security & Crypto
	implementation(libs.nimbus.jwt)
	implementation(libs.auth0.jwt)
	implementation(libs.auth0.jwks)
	implementation(libs.bcrypt)

	// Payments
	implementation(libs.stripe.java)

	// Logging
	implementation(libs.logback.classic)

	// Testing
	testImplementation(libs.ktor.server.test.host)
	testImplementation(libs.junit.api)
	testImplementation(libs.mockk)
	testRuntimeOnly(libs.junit.engine)
	testRuntimeOnly(libs.junit.launcher)
}

exposed{
	migrations {
		tablesPackage.set("com.calmed.calmedbackend.model.raw")
		testContainersImageName.set("postgres:18")
	}
}

tasks.named<GenerateMigrationsTask>("generateMigrations") {
	doFirst {
		val filename = "V${
			LocalDateTime.now().format(
				DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
			)
		}__MIGRATION.sql"

		val getter = javaClass.getMethod("getFullFileName")
		val setter = javaClass.getMethod("setFullFileName", String::class.java)

		val currentFilename = getter.invoke(this) as? String

		if (currentFilename.isNullOrBlank()) {
			setter.invoke(this, filename)
		}
	}
}

tasks.test {
	useJUnitPlatform()
	failOnNoDiscoveredTests = false
}