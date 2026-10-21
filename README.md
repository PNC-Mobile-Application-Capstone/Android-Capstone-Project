# AdventureWorks Customer Companion

Android app for browsing Adventure Works products, viewing product details, and saving products.

## Current features

- Product catalog loaded from the Adventure Works API
- Search, category filters, and name or price sorting
- Product detail screen
- Saved-product and recently-viewed persistence with Room
- Light and dark Compose themes
- Catalog, Orders, and Saved navigation

The Orders and Saved screens currently show empty states. Their full lists have not been implemented yet.

## Demo customer

`DefaultDemoCustomerSession` contains the assigned customer:

- Customer ID: `11711`
- Display name: `Daniel Davis`

## API

The base URL is defined as `BuildConfig.BASE_URL` in `app/build.gradle.kts`:

```text
https://api.bootcampcentral.com/
```

## Open and run

1. Open the repository root in Android Studio.
2. Use JDK 17 or Android Studio's embedded JDK.
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

## Source layout

```text
com.pnc.adventureworksmobile
|-- core/session       Demo customer session
|-- data/local         Room database and DAOs
|-- data/remote        Retrofit API and DTOs
|-- data/repository    Repository implementation
|-- di                 Hilt modules
|-- domain             Product models and repository contract
|-- presentation/home  Catalog and product details
`-- presentation/navigation  App navigation
```
