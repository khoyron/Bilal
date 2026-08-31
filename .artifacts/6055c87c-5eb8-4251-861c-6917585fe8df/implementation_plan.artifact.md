# Implementation Plan - Mosque Finder Integration

Integrate the Mosque Finder API from `khoyron/list_mosque` GitHub repository into the `Bilal` app. The app will fetch mosque data based on the user's current location (Country, Province, and City) and display the 10 closest mosques.

## User Review Required

> [!IMPORTANT]
> The `slugify` logic in Kotlin Multiplatform `commonMain` may not perfectly handle all Unicode normalization (accents) without additional dependencies. I will implement a robust regex-based version that covers most cases.

## Proposed Changes

### [Component] Data Layer

#### [NEW] [MosqueRepository.kt](file:///Users/moehammadkhoyron/DriverApps/Bilal/composeApp/src/commonMain/kotlin/org/khoyron/bilal/domain/repository/MosqueRepository.kt)
Define the interface for fetching mosques.

#### [NEW] [MosqueRepositoryImpl.kt](file:///Users/moehammadkhoyron/DriverApps/Bilal/composeApp/src/commonMain/kotlin/org/khoyron/bilal/data/repository/MosqueRepositoryImpl.kt)
Implement the repository using Ktor to fetch data from GitHub.
- Fetch `index.json` to resolve paths.
- Fetch city-specific JSON files.
- Implement `slugify` logic matching the Node.js version.

#### [MODIFY] [AppModule.kt](file:///Users/moehammadkhoyron/DriverApps/Bilal/composeApp/src/commonMain/kotlin/org/khoyron/bilal/di/AppModule.kt)
Register the new repository in the Koin module.

### [Component] UI Layer

#### [MODIFY] [MosqueFinderViewModel.kt](file:///Users/moehammadkhoyron/DriverApps/Bilal/composeApp/src/commonMain/kotlin/org/khoyron/bilal/ui/mosquefinder/MosqueFinderViewModel.kt)
- Integrate `MosqueRepository`.
- Use `Geocoder` to get Country, Province, and City from coordinates.
- Fetch mosques from the repository and filter for the 10 closest ones.
- Update UI state with the results.

## Verification Plan

### Automated Tests
- I will create a unit test for the `slugify` function to ensure it matches expected outputs.

### Manual Verification
- Deploy the app to an Android emulator/device.
- Verify that mosques are loaded based on the current (mocked) location.
- Verify that the "closest mosque" is correctly identified and up to 10 mosques are shown.
