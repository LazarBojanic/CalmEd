import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
		versionCode = 12
		versionName = "0.0.1"
	}
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
	buildTypes {
		getByName("release") {
			isMinifyEnabled = false
			isShrinkResources = false
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}
}
dependencies{
	implementation(projects.shared)
}