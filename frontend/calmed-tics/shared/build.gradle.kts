import com.android.build.api.dsl.LibraryExtension
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
	alias(libs.plugins.androidKotlinMultiplatformLibrary)
	alias(libs.plugins.composeMultiplatform)
	alias(libs.plugins.composeCompiler)
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.kotlinSerialization)
	alias(libs.plugins.ksp)
	alias(libs.plugins.koinCompiler)
	alias(libs.plugins.room)
	alias(libs.plugins.buildConfig)
}

kotlin{
	android{
		namespace = "com.calmed.calmedtics.shared"
		compileSdk = libs.versions.androidCompileSdk.get().toInt()
		minSdk = libs.versions.androidMinSdk.get().toInt()

		compilerOptions {
			jvmTarget = JvmTarget.JVM_11
			freeCompilerArgs = listOf("-Xexpect-actual-classes")
		}
		androidResources {
			enable = true
		}
		withHostTest {
			isIncludeAndroidResources = true
		}
	}
	listOf(
		iosArm64(),
		iosSimulatorArm64()
	).forEach { iosTarget ->
		iosTarget.binaries.framework {
			baseName = "Shared"
			isStatic = true
			linkerOpts("-lsqlite3")
		}
	}
	room {
		schemaDirectory("$projectDir/schemas")
	}
	sourceSets {
		commonMain.dependencies {
			implementation(libs.composeRuntime)
			implementation(libs.composeFoundation)
			implementation(libs.composeMaterial3)
			implementation(libs.composeMaterialIconsExtended)
			implementation(libs.composeUi)
			implementation(libs.composeComponentsResources)
			implementation(libs.composeComponentsUiToolingPreview)

			implementation(libs.kotlinxCoroutinesCore)
			implementation(libs.kotlinxSerializationJson)

			implementation(libs.androidxLifecycleViewModelSavedState)
			implementation(libs.androidxLifecycleViewmodelCompose)
			implementation(libs.androidxLifecycleRuntimeCompose)
			implementation(libs.androidxNavigationCompose)
			implementation(libs.androidxDatastore)
			implementation(libs.androidxDatastorePreferences)

			api(libs.koinCore)
			api(libs.koinAnnotations)
			implementation(libs.koinCompose)
			implementation(libs.koinComposeViewModel)

			implementation(libs.roomRuntime)
			implementation(libs.sqliteBundled)

			implementation(libs.ktorSerializationKotlinxJson)
			implementation(libs.ktorClientCore)
			implementation(libs.ktorClientContentNegotiation)
			implementation(libs.ktorClientLogging)
			implementation(libs.squareupOkio)

			implementation(libs.oAuthJavaJwt)
			implementation(libs.multiplatformSettings)
			implementation(libs.kotlinxDatetime)
			implementation(libs.coilCompose)
			implementation(libs.coilNetworkKtor)
		}
		androidMain.dependencies {
			implementation(libs.composeUiTooling)
			implementation(libs.androidxActivityCompose)
			implementation(libs.androidxCoreKtx)
			implementation(libs.androidxAppcompat)
			implementation(libs.androidxDatastore)
			implementation(libs.androidxDatastorePreferences)
			implementation(libs.androidxCredentials)
			implementation(libs.androidxCredentialsPlayServicesAuth)
			implementation(libs.googleAndroidLibrariesIdentityGoogleId)
			implementation(libs.googleAndroidGmsPlayServicesAuth)
			implementation(libs.googlePlayServicesCastFramework)
			implementation(libs.kotlinxCoroutinesAndroid)
			implementation(libs.roomRuntimeAndroid)
			implementation(libs.sqliteWrapper)
			implementation(libs.ktorClientOkHttp)
			implementation(libs.media3Exoplayer)
			implementation(libs.media3UI)
			implementation(libs.media3Common)
			implementation(libs.media3ExoplayerHLS)
			implementation(libs.media3ExoplayerDash)
			implementation(libs.media3Database)
			implementation(libs.media3Datasource)
			implementation(libs.media3ExoplayerWorkmanager)
			implementation(libs.androidxBrowser)
			implementation(libs.billingClient)
		}
		iosMain.dependencies {
			implementation(libs.roomRuntime)
			implementation(libs.sqliteBundled)
			implementation(libs.ktorClientDarwin)
		}
	}
}
dependencies {
	androidRuntimeClasspath(libs.composeUiTooling)
	add("kspAndroid", libs.roomCompiler)
	add("kspIosArm64", libs.roomCompiler)
	add("kspIosSimulatorArm64", libs.roomCompiler)
}

fun Project.localProps(): Properties {
	val props = Properties()
	val propFile = rootProject.file("local.properties")
	if (propFile.exists()) {
		propFile.inputStream().use { props.load(it) }
	}
	return props
}

val local = localProps()

buildConfig{
	// Ensure fields always exist with sensible defaults, then override from local.properties when available
	buildConfigField("development", (local.getProperty("DEVELOPMENT") ?: "false").toBoolean())
	buildConfigField("adbReverse", (local.getProperty("ADB_REVERSE") ?: "false").toBoolean())
	buildConfigField("googleWebClientId", local.getProperty("GOOGLE_WEB_CLIENT_ID") ?: "")
	buildConfigField("googleIosClientId", local.getProperty("GOOGLE_IOS_CLIENT_ID") ?: "")
	buildConfigField("googleAndroidClientId", local.getProperty("GOOGLE_ANDROID_CLIENT_ID") ?: "")
	buildConfigField("appleIosBundleId", local.getProperty("APPLE_IOS_BUNDLE_ID") ?: "")
	buildConfigField("appleWebClientId", local.getProperty("APPLE_WEB_CLIENT_ID") ?: "")
	buildConfigField("appleCallbackURI", local.getProperty("APPLE_CALLBACK_URI") ?: "")
	buildConfigField("notificationDebug", (local.getProperty("NOTIFICATION_DEBUG") ?: "false").toBoolean())
}

