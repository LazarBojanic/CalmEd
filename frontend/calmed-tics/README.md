This is a Kotlin Multiplatform project targeting Android and iOS.

Project modules:

* [/androidApp](./androidApp) contains the Android application module and Android-specific app configuration.

* [/shared](./shared) contains shared Kotlin Multiplatform code used by both platforms.
  Key source sets include:
	- [commonMain](./shared/src/commonMain/kotlin) for code shared across all targets.
	- Platform-specific source sets (for example [androidMain](./shared/src/androidMain/kotlin) and [iosMain](./shared/src/iosMain/kotlin)) for target-specific implementations.

* [/iosApp](./iosApp) contains the iOS app project (`iosApp.xcodeproj`) and iOS entry-point code.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:

- on macOS/Linux
  ```shell
  ./gradlew :androidApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :androidApp:assembleDebug
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open [/iosApp/iosApp.xcodeproj](./iosApp/iosApp.xcodeproj) in Xcode and run it from there.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…