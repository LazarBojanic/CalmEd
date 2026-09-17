import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
	alias(libs.plugins.androidApplication)
	alias(libs.plugins.composeMultiplatform)
	alias(libs.plugins.composeCompiler)
}

kotlin{
	target{
		compilerOptions{
			jvmTarget.set(JvmTarget.JVM_21)
		}
	}
	dependencies{
		implementation(projects.shared)
		implementation(libs.composeUiTooling)
		implementation(libs.androidxActivityCompose)
		implementation(libs.androidxCoreKtx)
		implementation(libs.androidxAppcompat)
		implementation(libs.kotlinxCoroutinesAndroid)
		implementation(libs.koinAndroid)
	}
}

android {
	namespace = "com.calmed.calmedtics"
	compileSdk = libs.versions.androidCompileSdk.get().toInt()

	buildFeatures {
		buildConfig = true
	}

	defaultConfig {
		applicationId = "com.calmed.calmedtics"
		minSdk = libs.versions.androidMinSdk.get().toInt()
		targetSdk = libs.versions.androidTargetSdk.get().toInt()
		versionCode = 26
		versionName = "0.0.1"
	}
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
	val keystoreProps = Properties().apply {
		val propFile = rootProject.file("keystore.properties")
		if (propFile.exists()) propFile.inputStream().use { load(it) }
	}
	signingConfigs {
		create("release") {
			if (keystoreProps.isNotEmpty()) {
				storeFile = file(keystoreProps.getProperty("storeFile"))
				storePassword = keystoreProps.getProperty("storePassword")
				keyAlias = keystoreProps.getProperty("keyAlias")
				keyPassword = keystoreProps.getProperty("keyPassword")
			}
		}
	}
	buildTypes {
		getByName("release") {
			isMinifyEnabled = true
			isShrinkResources = true
			if (keystoreProps.isNotEmpty()) {
				signingConfig = signingConfigs.getByName("release")
			}
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}
}