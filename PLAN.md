# CalmEd — Improvement Plan (final, implemented)

Status: implemented. Locked decisions: keep all existing code/dependencies/configs; no Room changes;
payment ledger rows survive account deletion; English-only (extract literals, no translations, no locale
picker); `androidApp` logs through shared `AppLog`; `LocalVideoDownloadManager` is fully Koin-injected.

## Phase 1 — Logging (done)
- Backend: single SLF4J convention (`LoggerFactory.getLogger(ThisClass::class.java)`), `{}` placeholders,
  throwable last, no `println`. `Errors.kt` (BusinessException→warn, unexpected→error+stack),
  `Monitoring.kt` (method/path/status/duration), `logback.xml` uses `CALMED_LOG_LEVEL` (default INFO).
- Frontend: `AppLog`/`LogTags` expect/actual (`android.util.Log` / `NSLog`, debug+info suppressed when
  `!isDevelopment`); Coil `DebugLogger` gated; all call sites migrated incl. `androidApp` MainActivity.

## Phase 2 (done)
- 2.3: `ProgramExerciseEntity.setFrom` persists `durationSeconds`; `HomeService` typed errors + 404;
  `ProgramConstants.TOTAL_WEEKS=25` reconciled with progress init; `findByAppleOriginalTransactionId`.
- 2.4: `RequestValidation` plugin installed with DTO validators; redundant manual checks removed.
- 2.5: Flyway migration adds unique indexes on `auth_credential(user_id,type)` and `provider_user_id`;
  N+1 removed from `getAll()`s via single user-map batch load.
- 2.6: generic client error messages + logging (no `${e.message}` to clients).
- 2.7: reflection migration-filename hack replaced by `fileVersionFormat=TIMESTAMP_ONLY` +
  `useUpperCaseDescription`.
- 2.2: intentionally skipped (AuthService kept as-is).

### 2.1 Transactions (done — service-owned, Choice 1)
- **Owner:** service/use-case layer. Repositories are transaction-agnostic (all `withTransaction`
  wrappers removed from the 10 repo impls).
- **Helpers** in `database/Database.kt`:
  - `withTransaction { }` — Exposed `suspendTransaction`, commits regardless of a returned `Failure`
    (used where a business failure must still persist, e.g. PENDING payment rows).
  - `withResultTransaction { AppResult } ` — converts a returned `AppResult.Failure` into a rollback
    (via a private `TransactionRollback` exception), then returns the Failure. Nested-aware: if already
    inside a transaction it just runs the block so the outermost boundary decides commit/rollback.
- Each public DB-touching service method is wrapped once: reads/must-persist → `withTransaction`;
  all-or-nothing writes → `withResultTransaction`.
- External I/O moved outside transactions: Google/Apple token verification, Stripe/PayPal/Apple/Play
  HTTP, Mailtrap email send, and profile-image file deletion.
- Bug fixed: `AuthService.refresh` no longer calls `logout` inside the open transaction.
- **Verified** by `TransactionSmokeTest` (Testcontainers/manual Postgres): commit, rollback-on-Failure,
  nested-service rollback — 3/3 pass. Run:
  `docker run -d --rm -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=calmed_test -p 5433:5432 postgres:18`
  then `./gradlew test --tests '*TransactionSmokeTest'`.

## Phase 3 (done)
- 3.1: `AuthViewModel` is a real `ViewModel` (Koin `viewModel {}`, `koinViewModel()`); `CancellationException`
  rethrown in all VMs.
- 3.2/3.5: `collectAsStateWithLifecycle`; validated `jwtDecode`; central `appBaseUrl`; store price via
  `BillingService.productPrice`; no `!!` in PaymentScreen; `VideoResolution` Saver; empty `LaunchedEffect`
  removed; `ExerciseCard` no longer takes scope/snackbar.
- 3.4 (English extraction only): user-visible literals across Home/Exercises/Onboarding/Profile/Payment/
  Video/Register/App/screens + component defaults + all ViewModel messages moved to
  `composeResources/values/strings.xml`. No translations, no language picker (intentionally English-only).
- 3.6: platform services injected via Koin:
  - `AndroidActivityHolder` replaces ad-hoc activity/provider globals; `Context` registered in Koin;
    removed `di.appContext` and `reminders.androidAppContext`.
  - `BillingService` → Koin (Android/iOS); `ReminderManager` is an interface + platform impls;
    `ImagePickerProvider` + platform impls; `IVideoDownloadManager` + `AndroidVideoDownloadManager`
    (now implements the interface) / `IosVideoDownloadManager`, both Koin-injected and used via
    `koinInject` at all call sites.

## Phase 4 (done)
- ProGuard/R8 keep rules for serialization, Ktor, Koin, Room, media3, Play Billing, Coil, Cast.
  `assembleRelease` passes.

## Verification (all green)
- Backend: `./gradlew compileKotlin test assemble`.
- Frontend: `:shared:compileAndroidMain`, `:androidApp:assembleDebug`, `:androidApp:assembleRelease`.
- iOS: verified by inspection only (no macOS).

## Known follow-ups
- 2.2 AuthService decomposition (skipped by request).
- Phase 5 tests beyond the transaction smoke test.
- Additional translations beyond English (only `values/` exists by design).
