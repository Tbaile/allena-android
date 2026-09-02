# Allena for Android

Android client for Allena, a fitness planner. Trainers build programmes and browse a shared
exercise library; the app talks to a REST API and keeps a local cache so most
screens work offline.

## Stack

**UI**
- Jetpack Compose with Material 3
- Navigation Compose, single Activity
- Coil for image loading, Vico for charts

**Data and networking**
- Retrofit + OkHttp
- Room for local persistence
- Paging 3 for paged lists
- DataStore Preferences for tokens and settings

**Tooling and tests**
- Hilt for dependency injection
- Timber for logging

## Architecture

Clean Architecture in three layers, dependencies pointing inward:

```
ui/       Compose screens, ViewModels, navigation, theme
domain/   models and repository contracts — no Android or network types
data/     repository implementations, Retrofit APIs and DTOs, Room DAOs,
          paging sources, sync, session storage
di/       Hilt modules
util/     shared helpers
```

Repositories return domain models. Room is the source of truth for cached lists; the network
layer refreshes it and Paging reads from it.

## Configuration

The API base URL is a `BuildConfig` field set per build type in `app/build.gradle.kts`:

| Build type | `API_BASE_URL` |
| --- | --- |
| debug | `http://10.0.2.2/api/v1/` (emulator loopback to the host) |
| release | `https://api.allena.example/api/v1/` |

Point the debug URL at your own backend if you are not running one on the host machine.
