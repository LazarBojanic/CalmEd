import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
	alias(libs.plugins.androidApplication)
	alias(libs.plugins.composeMultiplatform)
	alias(libs.plugins.composeCompiler)
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.kotlinSerialization)
	alias(libs.plugins.ksp)
	alias(libs.plugins.room)
	alias(libs.plugins.buildConfig)
}
dependencies {
	debugImplementation(libs.composeUiTooling)
}
compose {
	resources {
		packageOfResClass = "com.calmed.calmedtics"
		generateResClass = auto
	}
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

kotlin {
	androidTarget {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_21)
		}
	}

	iosArm64()
	iosSimulatorArm64()

	targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
		binaries {
			framework {
				baseName = "ComposeApp"
				isStatic = true
			}
		}
		binaries.all {
			linkerOpts("-lsqlite3")
		}
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
			implementation(libs.composeUiToolingPreview)

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

			implementation(libs.koinAndroid)
			implementation(libs.koinAndroidXCompose)

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

configure<ApplicationExtension> {
	namespace = "com.calmed.calmedtics"
	compileSdk = libs.versions.androidCompileSdk.get().toInt()

	buildFeatures {
		buildConfig = true
	}

	defaultConfig {
		applicationId = "com.calmed.calmedtics"
		minSdk = libs.versions.androidMinSdk.get().toInt()
		targetSdk = libs.versions.androidTargetSdk.get().toInt()
		versionCode = 1
		versionName = "1.0"
	}
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
	buildTypes {
		getByName("release") {
			isMinifyEnabled = false
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}
}

room {
	schemaDirectory("$projectDir/schemas")
}

dependencies {
	add("kspAndroid", libs.roomCompiler)
	add("kspIosArm64", libs.roomCompiler)
	add("kspIosSimulatorArm64", libs.roomCompiler)

	add("kspAndroid", libs.koinKspCompiler)
	add("kspIosArm64", libs.koinKspCompiler)
	add("kspIosSimulatorArm64", libs.koinKspCompiler)
}
