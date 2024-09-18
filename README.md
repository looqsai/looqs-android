# Android Project Scaffold (Jeminai)
[![AndroidCI](https://github.com/SeanZoR/jeminai-modern-android-scaffold/actions/workflows/Build.yaml/badge.svg)](https://github.com/SeanZoR/jeminai-modern-android-scaffold/actions/workflows/Build.yaml)

The **Jeminai Project** provides a modern Android architecture scaffold, making it easier to kickstart projects with Kotlin, Jetpack, Hilt, and reusable core modules. This repository includes essential components such as data management, user preferences using AndroidX DataStore, theme management, and unit testing, all structured using a modular approach.

## Table of Contents
- [Introduction](#introduction)
- [Features](#features)
- [Getting Started](#getting-started)
- [Modules](#modules)
- [HiltCoroutines Integration](#hiltcoroutines-integration)
- [Running Tests](#running-tests)
- [Contributing](#contributing)
- [License](#license)

## Introduction

The Jeminai project is inspired by the [Now in Android](https://github.com/android/nowinandroid) architecture but aims to serve as a scaffold for developing Android projects. It provides a modular setup with reusable components, making it suitable for large-scale projects that require separation of concerns, testability, and ease of maintenance.

## Features
- **Jetpack Compose** for UI development.
- **Hilt** for dependency injection.
- **AndroidX DataStore** with Protocol Buffers for user preferences.
- **Theme Management** supporting light and dark themes.
- **Coroutine support** using Kotlin Coroutines.
- **Reusable Core Modules** for data, testing, and model management.
- **Preconfigured unit and instrumentation tests**.
- **Modular Architecture** for scalability and separation of concerns.

## Getting Started

### Fork or Copy

To start using this project for your own development:

1. **Fork the Repository**
    - Visit the [Jeminai GitHub page](https://github.com/seanzor/jeminai-modern-android-scaffold).
    - Click "Fork" to create a copy under your GitHub account.

2. **Clone the Forked Repository**
   ```bash
   git clone https://github.com/yourusername/jeminai-modern-android-scaffold.git
   cd jeminai
   ```

3. **Start Building**
    - Modify or extend the existing modules to fit your app's needs.
    - Add new features or reuse core modules to speed up development.

4. **Build the Project**
   ```bash
   ./gradlew clean build
   ```

5. **Run the App**
   ```bash
   ./gradlew installDebug
   ```

## Modules

### 1. **:app**
This module is the main entry point of the application. It includes:
- `MainActivity.kt`: Handles the app’s lifecycle and sets up the user interface.
- Theme management with Jetpack Compose for light/dark theme support.
- A settings dialog to manage user preferences (e.g., theme settings).

### 2. **:core:data**
Handles data-related operations and acts as the repository layer, managing user data preferences.

### 3. **:core:datastore**
Persistent storage using AndroidX DataStore with Protocol Buffers.

### 4. **:core:model**
Defines data models like `UserData` used across the app.

### 5. **:core:testing**
Contains reusable test utilities and mock repositories for easier testing.

### 6. **:feature:settings**
UI components for managing app settings, including theme configuration.

## HiltCoroutines Integration

The Jeminai project integrates **HiltCoroutines**, a library created by the author to simplify coroutine management with Hilt dependency injection. It reduces boilerplate by injecting common coroutine dispatchers and pre-configured scopes.

- **Inject Dispatchers** like `IO`, `Main`, and `Default`.
- **Pre-configured Scopes** for background tasks, UI updates, and strict execution flows.

Add it to your project:
```kotlin
dependencies {
    implementation("com.sean8.core:hilt-coroutines:1.0.0")
}
```

For more, visit [HiltCoroutines](https://github.com/seanzor/hiltcoroutines).

## Running Tests

To run tests, execute:
```bash
./gradlew testDebugUnitTest
```

For instrumented tests:
```bash
./gradlew connectedDebugAndroidTest
```

## Contributing

Contributions are welcome! To contribute:
1. Fork the repository.
2. Create a new branch.
3. Implement your changes and run tests.
4. Submit a pull request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.
