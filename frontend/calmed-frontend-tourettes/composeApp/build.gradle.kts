import org.jetbrains.compose.desktop.application.dsl.TargetFormat
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
	kotlin("native.cocoapods")
}
dependencies {
	debugImplementation(compose.uiTooling)
}
compose {
	resources {
		packageOfResClass = "com.calmed.calmedfrontendtourettes"
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
}

kotlin {
	androidTarget {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_21)
		}
	}

	iosArm64()
	iosSimulatorArm64()
	iosX64()

	cocoapods {
		summary = "Some description for the Shared Module"
		homepage = "Link to the Shared Module homepage"
		version = "1.0"
		ios.deploymentTarget = "15.0"
		podfile = project.file("../iosApp/Podfile")
		framework {
			baseName = "ComposeApp"
			isStatic = true
		}
		pod("GoogleSignIn") {
			version = "~> 7.0.0"
		}
	}

	targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
		binaries.all {
			linkerOpts("-lsqlite3")
		}
	}

	sourceSets {
		commonMain.dependencies {
			implementation(compose.runtime)
			implementation(compose.foundation)
			implementation(compose.material3)
			implementation(compose.materialIconsExtended)
			implementation(compose.ui)
			implementation(compose.components.resources)
			implementation(compose.components.uiToolingPreview)

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
			implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
			implementation("media.kamel:kamel-image:0.9.3")




		}
		androidMain.dependencies {
			implementation(compose.preview)

			implementation(libs.androidxActivityCompose)

			implementation(libs.androidxCoreKtx)
			implementation(libs.androidxAppcompat)

			implementation(libs.androidxDatastore)
			implementation(libs.androidxDatastorePreferences)

			implementation(libs.androidxCredentials)
			implementation(libs.androidxCredentialsPlayServicesAuth)
			implementation(libs.googleAndroidLibrariesIdentityGoogleId)
			implementation(libs.googleAndroidGmsPlayServicesAuth)

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
			implementation(libs.androidxBrowser)


		}
		iosMain.dependencies {
			implementation(libs.roomRuntime)
			implementation(libs.sqliteBundled)
			implementation(libs.ktorClientDarwin)
		}
		iosArm64Main.dependencies {
			implementation(libs.kotlinxCoroutinesCoreIosArm64)
			implementation(libs.koinCoreIosArm64)
			implementation(libs.roomRuntimeIosArm64)
		}
		iosSimulatorArm64Main.dependencies {
			implementation(libs.kotlinxCoroutinesCoreIosSimulatorArm64)
			implementation(libs.koinCoreIosSimulatorArm64)
			implementation(libs.roomRuntimeIosSimulatorArm64)
		}
		iosX64Main.dependencies {
			implementation(libs.kotlinxCoroutinesCoreIosX64)
			implementation(libs.koinCoreIosX64)
			implementation(libs.roomRuntimeIosX64)
		}
	}
}

android {
	namespace = "com.calmed.calmedfrontendtourettes"
	compileSdk = libs.versions.androidCompileSdk.get().toInt()

	buildFeatures {
		buildConfig = true
	}

	defaultConfig {
		applicationId = "com.calmed.calmedfrontendtourettes"
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
	add("kspIosX64", libs.roomCompiler)

	add("kspAndroid", libs.koinKspCompiler)
	add("kspIosArm64", libs.koinKspCompiler)
	add("kspIosSimulatorArm64", libs.koinKspCompiler)
	add("kspIosX64", libs.koinKspCompiler)
}

