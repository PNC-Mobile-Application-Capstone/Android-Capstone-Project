# AdventureWorks Customer Companion

Android capstone application for customer product discovery, saved products, recently viewed products, and order review.

## Project foundation

- Kotlin and Jetpack Compose with Material 3
- Navigation Compose with centralized top-level routes
- ViewModels exposing immutable `StateFlow` UI state
- Hilt dependency injection
- Retrofit, OkHttp, and kotlinx.serialization networking
- Room persistence for saved and recently viewed product IDs
- JUnit and coroutine test dependencies
- Clean Architecture package boundaries for presentation, domain/session, data, and dependency injection

The starter navigation contains Discover, Orders, and Saved destinations. These screens intentionally remain setup placeholders; product and order behavior should be delivered as vertical feature slices.

## Demo customer

The assigned team customer is configured once in `DefaultDemoCustomerSession`:

- Customer ID: `11711`
- Display name: `Daniel Davis`

Do not copy this ID into composables, ViewModels, or API calls. Inject `DemoCustomerSession` wherever customer context is needed.

## API configuration

The default base URL is defined as `BuildConfig.BASE_URL` in `app/build.gradle.kts`:

```text
https://api.bootcampcentral.com/
```

Endpoint paths and DTOs should be added only after confirming them against the API's Swagger documentation. Network DTOs, Room entities, and domain models must remain separate.

## Open and run

1. Open this repository root in Android Studio.
2. Use JDK 17 or Android Studio's embedded JDK for Gradle.
3. Allow Gradle sync to finish.
4. Select the `app` run configuration and an API 26+ emulator or device.
5. Run the app.

## Build and test

From PowerShell at the repository root:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Initial package map

```text
com.pnc.adventureworksmobile
|-- core/session       Centralized demo customer context
|-- data/local         Room database, entities, and DAOs
|-- di                 Hilt bindings and providers
|-- presentation/home  Starter immutable StateFlow ViewModel
`-- presentation/navigation  Top-level routes and app navigation
```

As features are added, keep these responsibilities separate:

- Presentation: screens, route composables, ViewModels, UI state, and user events
- Domain: models, repository contracts, use cases, and customer-facing business rules
- Data: repository implementations, services, DTOs, entities, DAOs, and mapping

## Required quality checks

Before merging a feature, verify loading, success, empty, failure, timeout, accessibility, and responsive-layout behavior where applicable. Never commit credentials, `local.properties`, generated build output, or verbose production HTTP logs.
