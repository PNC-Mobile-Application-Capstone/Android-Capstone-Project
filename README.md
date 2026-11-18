# AdventureWorks Customer Companion

An Android app for discovering AdventureWorks products, saving favorites, viewing recently viewed products, and checking customer orders. Built with Kotlin, Jetpack Compose, Retrofit, Hilt, and Room.

## Setup

1. Open the project in Android Studio with JDK 17 or Android Studio's embedded JDK.
2. Allow Gradle sync to complete.
3. Run the `app` configuration on an Android 8.0 (API 26) or newer emulator or device.

## Configuration

- **Demo customer:** Daniel Davis, customer ID `11711`, configured in `core/session/DemoCustomerSession.kt`. 
- **API base URL:** `https://api.bootcampcentral.com/`, configured as `BuildConfig.BASE_URL` in `app/build.gradle.kts`.

## Build and test

Run from the project root in PowerShell:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat connectedDebugAndroidTest  # Requires a connected device or running emulator
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

Unit tests cover catalog behavior, saved and recently viewed products, orders, error handling, and data mapping. The instrumented `SavedProductMigrationTest` checks the Room database migration from version 2 to 3.

## Known limitations

- Product details and orders require an API connection. Saved and recently viewed product summaries are available offline.
- Saved products migrated from database version 2 show an unavailable state until a successful catalog refresh updates their information.
- Order amounts use `Double`, and the Orders screen does not yet support pull-to-refresh.
- Saved and recently viewed prices pass through floating-point storage before being rounded to two decimal places.
