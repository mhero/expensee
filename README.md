# Expensee

An offline-first Android expense tracker, built as a reference implementation of clean,
testable, modular Android architecture.

## Features

- Local account creation, login, and session management
- Add, edit, and delete expenses with categories, notes, and receipt photos
- Custom categories with reference-safe deletion (a category in use can't be deleted out from
  under an expense)
- Monthly dashboard: total spend, category breakdown, a simple spending chart, recent activity
- Currency, theme, and notification preferences; optional biometric unlock
- Daily expense-reminder notifications
- Sync-ready data model (see [Data model & sync](#data-model--sync) below) — no backend is
  required to run the app today

## Tech stack

- Kotlin, Jetpack Compose, Material 3
- Koin for dependency injection
- Room for local persistence, Preferences DataStore for settings
- WorkManager for background jobs (sync, cleanup, reminders)
- Coroutines & Flow throughout
- JUnit, Turbine, MockK, Robolectric, and Compose UI testing

**SDK levels:** compileSdk / targetSdk 36, minSdk 26 (Android 8.0+). 26 is the floor because it's
the first version with reliable JobScheduler-backed WorkManager guarantees and consistent Keystore
attestation/StrongBox support, while still covering the large majority of active devices.

## Architecture

```
app/                     Application, MainActivity, root NavHost, DI wiring

core/
  common/                DispatcherProvider, Result types, Money, sync status, ID generation
  database/              Room: entities, DAOs, database, default-category seeding
  network/               Retrofit/OkHttp, DTOs, API interfaces
  security/               Password hashing, Keystore-backed encrypted storage, biometric prompt
  datastore/              Preferences DataStore wrapper (currency, theme, notifications)
  ui/                     Material 3 theme, shared Compose components
  testing/                Shared test utilities (dispatcher rules, Flow test helpers)

feature/
  auth/                  Account creation, login, session, logout
  expenses/               Expense CRUD, receipts, filtering
  categories/             Category CRUD
  dashboard/               Monthly summary, category breakdown, chart
  settings/                Preferences, biometric toggle, logout
```

Dependencies flow one way: `app -> feature -> core`, and within each feature module,
`presentation -> domain -> data`. No feature module depends on another feature module — shared
concerns live in `core`. For example:

```
LoginScreen (Compose)
  -> LoginViewModel
    -> LoginUseCase (domain)
      -> AuthRepository (domain interface)
        -> LocalAuthRepository (data)
          -> UserDao (core:database), PasswordHasher (core:security)
```

Domain layers never import Android framework or networking types directly (`androidx.room.*`,
`retrofit2.*`, etc.) — those stay behind repository interfaces.

## Data model & sync

Every syncable entity (`ExpenseEntity`, `CategoryEntity`) carries the fields a sync engine needs,
even though no backend exists yet:

- `localId` (client-generated) / `remoteId` (server-assigned once synced)
- `createdAt` / `updatedAt`
- `syncStatus`: `PENDING_UPLOAD`, `SYNCED`, `PENDING_DELETE`, `SYNC_FAILED`
- `deletedAt` — soft delete, so an offline deletion can still be pushed later
- `version` — incremented on every edit, for last-write-wins conflict resolution

`SyncManager` orchestrates push/pull sync and is fully wired up, but is a safe no-op until
`core:network`'s `ApiConfig.isConfigured` is true — `API_BASE_URL` points at a placeholder host by
default. Once a real API exists, sync starts working with no schema changes and no changes to the
UI or domain layer, which only ever see the mapped domain model. `ExpenseSyncGateway` handles
sync-specific bookkeeping (pending uploads/deletions, applying remote snapshots, purging
tombstones) separately from `ExpenseRepository`, which is what the UI actually uses. Conflict
resolution is last-write-wins by `updatedAt`. `SyncWorker` runs every 6 hours when network is
available; `CleanupWorker` purges confirmed-synced tombstones daily; `BootCompletedReceiver`
reschedules both (and reminders) after a device reboot.

Category sync isn't wired up yet — the entity and API payload already support it, so it's a small,
mechanical follow-up rather than new architecture.

## Security

| What | Mechanism | Reversible? |
|---|---|---|
| Password | PBKDF2WithHmacSHA256, 120k iterations, random salt | No (hashed) |
| Session token | Android Keystore-backed encrypted storage | Yes (encrypted) |
| Expenses / categories | Room (SQLite), app-sandboxed | Yes (plaintext at rest) |
| Preferences | Plain DataStore | N/A (not sensitive) |
| Receipt images | App-private file storage, referenced by URI | Yes (plaintext at rest) |

Expense and category data isn't separately encrypted at rest — full-database encryption would mean
prompting for a passphrase on every cold start, which isn't warranted for this threat model. If
that changes, it's a localized change to `core:database`'s database builder; no other module
assumes the database is unencrypted.

Biometric unlock is implemented via a narrow `BiometricUnlockRepository` interface in
`core:security`, so `feature:settings` (which owns the toggle) doesn't need to depend on
`feature:auth`. Enabling the toggle requires a successful biometric prompt first, so a user can't
enable an unlock method that doesn't actually work on their device.

## Testing

- Unit tests for use cases and repositories against fakes (no Room/Android dependency where
  avoidable)
- Room DAO tests (Robolectric, in-memory database) covering queries, soft delete, tombstone
  purging, and foreign-key constraints
- ViewModel tests for every feature, using shared dispatcher/Flow test utilities
- `SettingsRepositoryImpl` tested against a real on-disk DataStore (Robolectric)
- `SyncWorker`/`CleanupWorker` tested with `androidx.work:work-testing`'s
  `TestListenableWorkerBuilder`
- Instrumented Compose UI tests for representative screens (`LoginScreen`,
  `AddEditExpenseScreen`), driving the composables directly with fixed state rather than through
  a real ViewModel

```
./gradlew testDebugUnitTest
./gradlew connectedAndroidTest   # requires a device or emulator
```

## Building

Standard Gradle Kotlin DSL project.

```
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

Open in Android Studio, or build from the command line — no backend is required.