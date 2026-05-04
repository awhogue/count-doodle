# CLAUDE.md — guidance for working in this repo

Project-specific notes captured during the initial build. Optimize for **fast feedback loops**, **TDD**, and **avoiding gotchas with Glance widgets**.

## Workflow

- **TDD is the default.** Write a failing test, then the smallest implementation that passes, then refactor. Pure logic (`Defaults`, `formatCountdown`, widget state builders) lives in plain Kotlin and is exercised by JVM tests under `app/src/test`.
- **Don't skip the failing-test step**, even when the change feels trivial. The pure helpers in `util/` and `data/Defaults.kt` exist precisely so domain logic stays testable on the JVM.
- **Run before committing**:
  ```
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  ./gradlew :app:testDebugUnitTest
  ./gradlew :app:assembleDebug
  ```
- **Smoke-test UI on a device** for any change touching Compose, widgets, or photo handling. Type checks alone don't catch IPC/Binder, deep-link, or layout-overflow bugs.

## Architecture conventions

- **Single Activity + Compose nav graph.** Routes: `list`, `edit/{id}`, `detail/{id}`. Deep link `countdoodle://event/<id>` opens detail directly (used by widgets).
- **Repository pattern.** `EventRepository(dao, onChanged)` exposes `Flow`s for reactive UI. The `onChanged` callback (set in `CountDoodleApp`) enqueues `WidgetUpdateScheduler.requestImmediateUpdate(...)` so widgets refresh on every mutation.
- **Pure logic out of `@Composable`.** `formatCountdown(now, target, hasTime, zone)` and the widget state builders take primitives, not framework types — they have no Android dependencies and are unit tested.
- **Defaults are deterministic.** `Defaults.emojiFor(name)` / `colorArgbFor(name)` derive from `name.hashCode()` so an event without an explicit emoji/color stays visually stable across renames-or-not.

## Glance widgets — hard-won lessons

Glance is *not* equivalent to Compose-on-the-home-screen. A few things that wasted real time during this build:

### `provideGlance` runs **once** when `stateDefinition` is set

When you `override val stateDefinition = PreferencesGlanceStateDefinition`, Glance starts a long-lived session at `provideGlance` time. After that, only the `@Composable` re-runs — and it re-runs because Glance's snapshot system observes `currentState<Preferences>()`.

**Implication:** Anything you compute *inside `provideGlance`* (like fetching the Event from the DB based on an eventId) is captured **once** and never refreshed. State changes won't update those values.

**Fix:** Read state inside the `@Composable` via `currentState<Preferences>()`, then do async work (DB lookup, bitmap load) inside `produceState(key1 = stateValue) { ... }`. The composable will recompose when state changes; `produceState` will re-fetch.

This is what `SingleEventWidget.Render()` does today — `eventId` comes from `currentState`, and `produceState` keyed on `eventId` resolves both the `Event` and its photo `Bitmap`.

### Widget bitmaps go through Binder — keep them small

Widget RemoteViews are serialized via Binder, which has a ~1MB transaction limit. A modern phone photo blows past that easily and the launcher shows **"Can't show content"**.

`loadBitmap()` in `SingleEventWidget.kt` does the right thing:
1. `inJustDecodeBounds = true` to read source dimensions
2. Pick `inSampleSize` so neither dimension exceeds `WIDGET_MAX_DIM` (512)
3. Decode as `RGB_565` (2 bytes/pixel, half of `ARGB_8888`)
4. `createScaledBitmap` for the final clamp

If you ever need to add another photo-rendering widget, copy that pattern. **Do not** pass a full-resolution bitmap to a Glance `Image`.

### `actionStartActivity` has two flavors

- `androidx.glance.action.actionStartActivity<T : Activity>()` — type-only, doesn't accept an `Intent`.
- `androidx.glance.appwidget.action.actionStartActivity(intent: Intent, …)` — takes an `Intent` (use this for deep links).

If you need a deep link from a widget, use the **appwidget** import. Mixing the two will produce confusing "argument type mismatch" errors.

### Widget config activity flow

1. The host launches `*.SingleEventWidgetConfigActivity` with `EXTRA_APPWIDGET_ID`. Default to `RESULT_CANCELED` immediately so an aborted config doesn't leave a placeholder widget.
2. On user selection, run the write off the activity's `lifecycleScope` — `finish()` cancels that scope, so use `applicationContext` and a process-rooted scope. (We use `GlobalScope.launch` here pragmatically; if the codebase grows, swap for an injected `ApplicationScope`.)
3. Resolve `glanceId = GlanceAppWidgetManager(ctx).getGlanceIdBy(appWidgetId)` and call `SingleEventWidgetConfig.write(ctx, glanceId, eventId)` (which uses `updateAppWidgetState`).
4. `setResult(RESULT_OK, …)` and `finish()`.

### Resizable widgets need explicit bounds on Android 12+

`resizeMode="horizontal|vertical"` alone isn't enough — set `minResizeWidth/Height` and `maxResizeWidth/Height` so the launcher shows resize handles. See `res/xml/single_event_widget_info.xml`.

## Compose UI gotchas

- **Long forms must be `verticalScroll`-wrapped.** `EventEditScreen` adds `Modifier.verticalScroll(rememberScrollState())` because the photo preview otherwise pushed the Save button off-screen.
- **Cap photo previews.** `heightIn(max = 200.dp)` + `ContentScale.Crop` keeps the layout sensible on every screen size.
- **Live countdown ticks adaptively.** `LiveCountdownText` uses `delay(1_000L)` for timed events and `delay(60_000L)` for all-day — no point waking up every second to recompute "Today".

## Testing setup

- Unit tests use **Robolectric** so they can spin up an in-memory Room database on the JVM (`Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), …)`). This avoids needing a connected device for DAO tests.
- For Flow assertions: **Turbine** (`flow.test { … }`) — use `cancelAndIgnoreRemainingEvents()` to clean up cleanly.
- For ViewModels: `Dispatchers.setMain(UnconfinedTestDispatcher())` in `@Before`, `Dispatchers.resetMain()` in `@After`.

## Things to avoid

- **Don't** add features, error-handling branches, or fallback paths beyond what the change at hand requires. (Cf. the global guidance in `~/.claude/CLAUDE.md`.)
- **Don't** mock the Room DAO in tests — the in-memory database is fast and catches schema/query bugs that a mock wouldn't.
- **Don't** assume widget photos can be passed at full resolution — see above.
- **Don't** put DB or network work inside `@Composable` directly; use `produceState` or a ViewModel.

## Open follow-ups (nice-to-haves)

- Replace the deprecated `Icons.Filled.ArrowBack` with `Icons.AutoMirrored.Filled.ArrowBack` in `EventDetailScreen`.
- Make the multi-event widget show more rows when resized taller (read `LocalSize.current` inside the composable).
- Consider an `@HiltAndroidApp` / DI setup if the surface area grows; the current `lazy` singletons in `CountDoodleApp` are intentional and lightweight for this scope.
- Camera-capture path is wired in the manifest (`FileProvider`) but not yet exposed in the UI — only gallery picking is currently in `EventEditScreen`.
