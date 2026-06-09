# Bilal - Islamic Daily Companion

Bilal is a cross-platform Islamic application built with **Kotlin Multiplatform** and **Compose Multiplatform**. It is designed to help Muslims manage their daily worship with ease and efficiency across Android, iOS, and Desktop.

## Features

- 🕋 **Prayer Times**: Accurate prayer schedules based on your location.
- 📖 **Al-Quran**: Read the Holy Quran with a clean and modern interface, featuring last read tracking and favorites.
- 🧭 **Qibla Finder**: Find the direction of the Kaaba easily using your device's compass.
- 🕌 **Mosque Finder**: Locate the nearest mosques around you.

## Tech Stack

This project leverages modern Android and Multiplatform development tools:

- **Kotlin Multiplatform (KMP)**: Shared business logic across Android, iOS, and Desktop.
- **Compose Multiplatform**: Shared UI for all platforms.
- **Koin**: Dependency Injection.
- **Navigation Compose**: Type-safe navigation between screens.
- **Kotlinx Serialization**: JSON parsing and data handling.
- **Kotlinx Datetime**: Date and time manipulation for prayer schedules.
- **Compose Resources**: Unified resource management (images, strings, fonts).

## Project Structure

- `/composeApp`: Contains the shared UI and logic.
    - `commonMain`: Shared code for all targets.
    - `androidMain`: Android-specific implementations.
    - `iosMain`: iOS-specific implementations.
    - `desktopMain`: Desktop (JVM)-specific implementations.
- `/iosApp`: Native iOS entry point.

## Getting Started

1. Clone this repository.
2. Open the project in **Android Studio** (with the Kotlin Multiplatform plugin installed).
3. Select your target (Android, iOS, or Desktop) and run.

---
*Developed with ❤️ using Kotlin Multiplatform.*
