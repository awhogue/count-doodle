# Count Doodle

A simple Android countdown app, modeled loosely on the iOS Countdown app.

Add events with a name, date, and optional time. Each event gets a deterministic emoji and background color (or pick your own). Optional photo from gallery. Two home-screen widgets are included.

## Features

- **Event list**: rounded color cards with emoji, name, and live countdown.
- **Event detail**: full-screen view with photo background (or solid color), large name, and countdown ticking each second.
- **Add / edit / delete**: date picker, optional time picker, emoji picker, color picker, photo picker (`PickVisualMedia`).
- **All-day vs. timed countdowns**:
  - Timed: `12d 4h 3m 12s` (collapsing leading zero units)
  - All-day: days only — `12 days`, `1 day`, `Today`, `Happened 3 days ago`
- **Single-event widget**: configurable on placement, shows photo + name + countdown, taps deep-link to event detail. Resizable.
- **Multi-event widget**: shows the next 4 upcoming events, sorted ascending. Each row taps to that event's detail. Resizable.

## Tech stack

- Kotlin, Jetpack Compose (Material 3), single-Activity nav graph
- Min SDK 31, Target SDK 35 (compile SDK 35)
- Room (SQLite) + Kotlin Flow for reactive lists
- Coil for in-app image loading
- Jetpack Glance for the widgets
- WorkManager for periodic widget refresh

## Project layout

```
app/src/main/java/com/awhogue/countdoodle/
  CountDoodleApp.kt           Application: DB + repo singletons, WorkManager config
  MainActivity.kt             Single-activity host + Compose nav graph
  data/                       Event, EventDao, AppDatabase, EventRepository, Defaults
  util/Countdown.kt           Pure formatCountdown(now, target, hasTime, zone)
  ui/list/                    EventListScreen + ViewModel
  ui/edit/                    EventEditScreen + ViewModel
  ui/detail/                  EventDetailScreen + ViewModel
  ui/components/              LiveCountdownText
  widget/
    SingleEventWidget.kt      Glance widget; reads state via currentState<Preferences>()
    SingleEventWidgetConfigActivity.kt
    SingleEventWidgetConfig.kt   Glance state read/write helpers
    MultiEventWidget.kt
    WidgetUpdateScheduler.kt  Periodic + on-mutation work
    WidgetUpdateWorker.kt
    WidgetState.kt            Pure state builders (unit-testable)
```

## Build

```sh
# Use the JDK shipped with Android Studio
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

./gradlew :app:assembleDebug          # APK at app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:installDebug           # install to attached device/emulator
./gradlew :app:testDebugUnitTest      # JVM unit tests
```

## Tests

The project is built test-first. Unit tests run on the JVM via Robolectric:

| Suite | Coverage |
|---|---|
| `DefaultsTest` | Deterministic emoji/color from name hash |
| `CountdownTest` | `formatCountdown` for timed and all-day events, including local-midnight boundary |
| `EventDaoTest` | In-memory Room: insert/update/delete, ordering, `observeUpcoming(now, limit)` |
| `EventRepositoryTest` | Mutations notify `onChanged` callback (used to refresh widgets) |
| `EventEditViewModelTest` | Form validation, save with/without time, load, delete |
| `WidgetStateBuilderTest` | Pure builders for both widget views |

## License

MIT — see [LICENSE](LICENSE).
