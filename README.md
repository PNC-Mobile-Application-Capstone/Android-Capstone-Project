# AdventureWorks Customer Companion: PNC Mobile Applications Bootcamp Android Capstone

An Android app for AdventureWorks customers. They browse products, save favorites, see recently viewed products, and check their orders. Built with Kotlin, Jetpack Compose, Retrofit, Hilt, and Room.

## Contents

1. [Supported Versions](#supported-versions)
2. [Setup](#setup)
3. [Configuration](#configuration)
4. [Credentials Procedure](#credentials-procedure)
5. [Architecture Summary](#architecture-summary)
6. [Build and Test](#build-and-test)
7. [Known Limitations](#known-limitations)

## Supported Versions

| Item | Value |
| --- | --- |
| Minimum Android version | Android 8.0, API 26 (`minSdk = 26`) |
| Target and compile SDK | API 37 (`targetSdk = 37`, `compileSdk = 37`) |
| Android Gradle Plugin | 9.4.1 |
| Gradle | 9.6.0 (through the included wrapper, no install needed) |
| Kotlin | 2.2.10 |
| JDK to run Gradle | JDK 17 or newer. Android Studio's embedded JDK works |
| Java bytecode level | Java 11 (`sourceCompatibility` and `targetCompatibility`) |
| Android Studio | A release that supports Android Gradle Plugin 9.4 |
| App ID | `com.pnc.adventureworksmobile`, version 1.0 (`versionCode 1`) |
| Device type | Phones. Tablets and landscape are allowed but were not a design target |
| Emulator tested | [Fill in: the shared emulator, for example Pixel 9, API 37] |

**Main libraries**

| Library | Version |
| --- | --- |
| Compose BOM | 2026.02.01 (Material 3) |
| Navigation Compose | 2.10.2 |
| Hilt | 2.60.1 |
| Retrofit | 2.11.0, with the kotlinx.serialization converter |
| OkHttp | 5.3.0 |
| kotlinx.serialization JSON | 1.9.0 |
| Room | 2.8.5 |
| Coroutines | 1.10.2 |
| Tests | JUnit 4.13.2, Turbine 1.2.1, coroutines-test, Room testing |

All versions live in `gradle/libs.versions.toml`.

## Setup

**Before you start**
- Android Studio, with the Android SDK Platform for API 37 installed. Add it in Settings, Languages & Frameworks, Android SDK.
- An emulator or phone on Android 8.0 (API 26) or newer.
- Access to the team GitHub repository.

**Steps**
1. Clone the repository and switch to the branch you want to run. `dev` is the newest tested code. `main` is the release branch.
   ```bash
   git clone <repository URL>
   cd Android-Capstone-Project
   git checkout dev
   ```
2. Open the project folder in Android Studio.
3. If Android Studio asks, set the Gradle JDK to JDK 17 or newer. The embedded JDK is fine.
4. Let Gradle sync finish. Android Studio creates `local.properties` with your SDK path. That file is in `.gitignore`. Do not commit it.
5. Pick the `app` run configuration and your emulator or phone.
6. Press Run. The app opens on the Catalog screen. No sign-in is needed.

## Configuration

The app has one environment and one build setup. There are no secrets files, API keys, or `google-services.json` to add.

| Setting | Value | Where to change it |
| --- | --- | --- |
| API base URL | `https://api.bootcampcentral.com/` | `app/build.gradle.kts`, `buildConfigField("String", "BASE_URL", ...)`. Read in code as `BuildConfig.BASE_URL` |
| Demo customer | Daniel Davis, customer ID `11711` | `core/session/DemoCustomerSession.kt`, `DefaultDemoCustomerSession` |
| Network timeouts | Connect 10 s, read 20 s, write 20 s, whole call 30 s | `di/ApplicationModule.kt`, `NetworkTimeouts` |
| HTTP logging | `BASIC` (method, URL, status) in debug builds. Off in release | `di/ApplicationModule.kt`, `provideOkHttpClient()` |
| Low stock threshold | 100 units or fewer shows "Low stock." 0 shows "Unavailable." Missing or negative shows "Unknown" | `domain/model/ProductAvailability.kt`, `LOW_STOCK_THRESHOLD` |
| Recently viewed limit | Keeps the 10 most recent products | `data/local/RecentlyViewedProductDao.kt`, `trimToLatestTen()` |
| Local database | Room file `adventure-works.db`, schema version 3 | `data/local/AdventureWorksDatabase.kt`. Schemas are exported to `app/schemas/` |

All source paths above are under `app/src/main/java/com/pnc/adventureworksmobile/`.

**Changing the demo customer.** Edit the `id` and `displayName` in `DefaultDemoCustomerSession`. That is the only place the ID is set. Hilt provides it to every screen through `di/SessionModule.kt`. Do not copy the ID into a composable or ViewModel.

**Changing the base URL.** Edit the `BASE_URL` line in `app/build.gradle.kts`, then sync Gradle. The URL must end with a `/`, or Retrofit will refuse to build.

## Credentials Procedure

This app has no sign-in, by design. The requirements put authentication and account registration out of scope. The app runs as one demo customer instead.

**What the app uses**
- No username, password, API key, or token is stored in the app or the repository.
- The API calls the app makes (`GET /api/Product`, `GET /api/Product/{id}`, `GET /api/Order/customer`, `GET /api/Order/customer/{id}`) are sent without an `Authorization` header.
- The demo customer ID and name are given to the team by the instructor. They are a test profile, not a login. The Catalog screen greets the customer by first name ("Welcome back, Daniel").

**If the instructor assigns a different demo customer**
1. Update `DefaultDemoCustomerSession` as shown in [Configuration](#configuration).
2. Run the unit tests. `DefaultDemoCustomerSessionTest` checks the configured customer.
3. Open a pull request so a teammate reviews the change.

**If the API starts asking for a token**
- Every call would fail with a 401 or 403. The app maps both to `AppError.Unauthorized` and shows "The app doesn't have access to this right now. Please try again later."
- The fix is to add an OkHttp interceptor in `provideOkHttpClient()` that adds the token. The token must come from a secure source, not from source code or `build.gradle.kts`.

**Rules**
- Never commit credentials, tokens, keystores (`*.jks`, `*.keystore`), or `local.properties`. The `.gitignore` already blocks these.
- Keep HTTP logging at `BASIC` or lower. `BODY` logging must stay out of release builds.

## Architecture Summary

The app follows a layered design: presentation, domain, and data. Hilt wires the layers together. Each screen has one ViewModel that exposes a single `uiState` as a `StateFlow`.

### How a request gets to the screen

```
Compose Screen  (collects uiState, sends user actions)
   |
   v
ViewModel       (viewModelScope, one uiState: loading / content / empty / error)
   |
   v
Use case        (FilterAndSortProductsUseCase, GetRecentOrdersUseCase)
   |
   v
Repository      (ProductRepository, OrderRepository interfaces in domain/)
   |   \
   |    Room DAOs: saved products, recently viewed products
   v
Retrofit API    (ProductApi, OrderApi)
   |
   v
OkHttp (timeouts, debug logging)  -->  AdventureWorks API
```

Every repository call is wrapped in `safeCall`. It turns any exception into a `Result` with a typed `AppError` (no connection, timeout, unauthorized, not found, server error, bad data, local storage, unknown). Screens map each `AppError` to a plain message with a Try again button.

### Folder layout (`app/src/main/java/com/pnc/adventureworksmobile/`)

| Folder | What lives there |
| --- | --- |
| `core/session/` | `DemoCustomerSession`, the one place the demo customer is set |
| `di/` | Hilt modules. `ApplicationModule` builds OkHttp, Retrofit, Room, and the repositories. `SessionModule` binds the demo customer |
| `data/remote/` | Retrofit interfaces, DTOs, and a `BigDecimal` serializer for prices |
| `data/local/` | Room database, DAOs, entities, migrations (1 to 2, 2 to 3), and money column helpers |
| `data/repository/` | `DefaultProductRepository` and `DefaultOrderRepository` |
| `data/error/` | `ErrorMapper` and `safeCall` |
| `domain/` | Models (`Product`, `Order`, `ProductAvailability`, `ProductCategory`, `ProductSort`), repository interfaces, use cases, and `AppError` |
| `presentation/` | One folder per screen (`catalog`, `product`, `orders`, `saved`, `recent`) plus `shared/` parts: error view, availability chip, product image, money formatting |
| `navigation/` | `AdventureWorksApp`: the bottom bar and the nav graph |
| `ui/theme/` | Material 3 colors and theme |

### Key design choices

- **One demo customer, one place.** The customer ID comes from `DemoCustomerSession` through Hilt. No screen has the number written in it.
- **Offline-friendly lists.** Saved and recently viewed products are stored in Room as summaries. They show without a network connection and survive an app restart.
- **Prices as `BigDecimal`.** Product prices are decoded as `BigDecimal` to avoid rounding errors.
- **Navigation.** A bottom bar with four tabs: Catalog, Orders, Saved, Recents. Product and order details open as their own routes (`product/{productId}`, `order/{orderId}`). Order lines link to their products.
- **Timeouts on every call.** One OkHttp client covers every API call, so no spinner waits forever.

## Build and Test

Run from the project root.

**Windows (PowerShell)**
```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat connectedDebugAndroidTest  # Needs a connected device or running emulator
```

**macOS or Linux**
```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew connectedDebugAndroidTest  # Needs a connected device or running emulator
```

The debug APK is built at `app/build/outputs/apk/debug/app-debug.apk`.

**What the tests cover**
- **Unit tests** (`app/src/test`, 167 tests): catalog filter and sort, product and order DTO mapping, both repositories, every ViewModel, saved and recently viewed products, error mapping and error messages, the demo customer session, and the OkHttp timeouts. They use fake APIs, fake DAOs, and fake repositories, so they need no network.
- **Instrumented tests** (`app/src/androidTest`, 5 tests): `SavedProductMigrationTest` checks the Room migration from version 2 to 3. `RecentlyViewedProductDaoTest` checks the recently viewed table on a real database.

## Known Limitations

Status as of 11/25/26, `dev` at `f5d72a2`.

**Setup and configuration**
- There is one environment. Pointing the app at a different API means editing `build.gradle.kts` and rebuilding.
- Only the debug build is set up. There is no release signing config and no code shrinking.

**Credentials and access**
- There is no sign-in. The app always runs as the one demo customer.
- The screen does not say "Demo customer" yet. It only shows "Welcome back, Daniel." The requirements ask the app to clearly show this is a demo context, not a real sign-in.
- API calls send no token. If the API starts requiring one, every screen will show the "doesn't have access" error until an interceptor is added.

**Orders**
- The app downloads all customer orders, then keeps the ones that match the demo customer ID on the phone. This uses more data than a server-side filter would.
- Order details are fetched one call per order line, so large orders take longer to load.
- Order amounts use `Double`, not `BigDecimal`.
- The Orders screen does not support pull-to-refresh. Only the Catalog does.

**Offline**
- Product details and orders need an API connection. Saved and recently viewed product summaries work offline.
- Saved products migrated from database version 2 show an unavailable state until a successful catalog refresh updates their information.
- Saved and recently viewed prices are stored as `Double` in Room, then rounded to two decimal places when read.

**Devices**
- Tablets and landscape are not blocked, but layouts were designed and checked on phones in portrait.
