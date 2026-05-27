# Assignments Chronometer

🇧🇷 [Português](README.pt.md)

An Android app for timing and recording assignments in Jehovah's Witnesses meetings — both weekend (Public Talk and Watchtower Study) and midweek (Treasures from God's Word, Spiritual Gems, and other *Christian Life and Ministry* parts).

---

## Features

### Chronometer
- Real-time stopwatch with **Start / Pause / Resume / Reset** states
- Visual overtime indicator (red background when the planned duration is exceeded)
- Floating overlay that stays visible over other apps
- Simplified overlay mode: tap to pause/resume
- Comment countdown for the Spiritual Gems part (30-second slots)

### Weekly Part Records
- Automatic import via **OCR** from a camera photo or PDF/image file
- Smart parser that identifies date, title, assignee, room and duration for each part
- Manual add and edit via dialog
- Records realized time and automatically calculates delay
- Parts grouped by week with a date sticky header
- Export and import records as `.acdata` files (JSON)
- Share individual parts as plain text (WhatsApp, etc.)

### Quick Assignments
- One-tap shortcuts for Public Talk, Watchtower Study, Treasures and Spiritual Gems with preset durations

### Settings
- Theme: System / Light / Dark
- Dynamic colors (Material You — Android 12+)
- Overlay opacity, width and height controls (8 levels each)
- Quick presets: Compact, Default, Large
- Show/hide comment count in overlay
- Enable/disable simplified overlay
- Data management: export, import and clear records
- Overlay permission shortcut from the settings screen
- Open-source licenses screen

---

## Architecture

The project follows **MVVM** with clear layer separation:

```
app/
├── data/
│   ├── model/          # Domain models (Assignment, WeeklyPart)
│   └── repository/     # Data access (Settings, Records, PdfOcr)
├── overlay/            # Overlay service and lifecycle owner
├── navigation/         # NavHost and route definitions
├── ui/
│   ├── components/     # Reusable composables (cards, dialogs, etc.)
│   ├── screens/        # App screens
│   └── theme/          # Material3 theme, colors and typography
├── util/               # Utilities (OCR parser, date formatters)
├── viewmodel/          # ViewModels shared via Application
├── App.kt              # Application with global ViewModelStore
└── MainActivity.kt     # Entry point, deep links and overlay control
```

### Data flow

```
UI (Compose) ──► ViewModel ──► Repository ──► DataStore / ContentResolver
                     ▲
                     │ state (Compose State / StateFlow)
```

The ViewModels (`SharedViewModel`, `WeeklyPartsViewModel`, `SettingsViewModel`) are instantiated in the `App` class and shared between `MainActivity` and `ChronometerOverlayService`, keeping state consistent even when the overlay is shown over other apps.

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material3 |
| Navigation | Navigation Compose |
| State | Compose State + StateFlow |
| Persistence | DataStore Preferences |
| OCR | ML Kit Text Recognition (Latin) |
| PDF → Bitmap | PdfRenderer (Android native) |
| Serialization | Kotlinx Serialization JSON |
| Concurrency | Kotlin Coroutines |
| Overlay | WindowManager + custom ComposeView |

---

## Key Classes

### `App.kt`
Global instance of the three ViewModels via `ViewModelStoreOwner`. Allows both `MainActivity` and `ChronometerOverlayService` to share the same state.

### `SharedViewModel`
Controls the chronometer state (time, running, paused) and the currently active assignment/part. Uses `SystemClock.elapsedRealtime()` for accuracy independent of the system clock.

### `WeeklyPartsViewModel`
Manages the weekly parts list. Coordinates OCR import (camera and PDF), `.acdata` export/import, and UI actions (pending navigation, toast/snackbar feedback).

### `SettingsViewModel`
Exposes `SettingsUiState` via `StateFlow` by combining `SettingsRepository` preferences with overlay dimension validation messages.

### `OcrParser`
Interprets OCR lines extracted by ML Kit, identifies parts via regex (`^\d+\.\s*.+\(\d+\s*min\)`), maps assignees to the right column of the page, and filters structural programme terms.

### `ChronometerOverlayService`
Service that displays the overlay using `WindowManager` + `ComposeView` with its own lifecycle (`OverlayLifecycleOwner`). Starts when the user leaves the app while the timer is running; stops on return.

### `OverlaySizeRules`
Cross-validation rules between overlay width and height: certain height levels require a minimum width level to guarantee readability.

---

## Navigation

```
Home (Chronometer)
├── Assignments (Quick assignments) ──► Home
├── Record (Weekly records) ──► Home
└── Settings ──► Licenses
```

Navigation uses `NavHost` with no transitions (`EnterTransition.None`) for instant responses. Deep links via the `chronometer://` scheme enable Android home-screen shortcuts:

| URI | Action |
|---|---|
| `chronometer://start` | Start the timer |
| `chronometer://import-media` | Open PDF/image import |
| `chronometer://scan` | Open camera |
| `chronometer://import-acdata` | Open records file import |

---

## `.acdata` File Format

JSON file with the following structure:

```json
{
  "version": 1,
  "parts": [
    {
      "uid": "generated-uuid",
      "id": "3",
      "title": "Spiritual Gems",
      "durationInMinutes": 10,
      "room": "Main Room",
      "assignees": "John Smith",
      "dateText": "5 de junho de 2025",
      "realizedTimeOnSeconds": 623
    }
  ]
}
```

---

## Localization

The app fully supports two locales:

- **`values/strings.xml`** — English (default fallback)
- **`values-pt/strings.xml`** — Brazilian Portuguese

The date parser (`DateUtils.parseOcrDate`) recognizes Portuguese month names (jan, fev, mar…) and the `DD/MM` format.

---

## Permissions

| Permission | Purpose |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Display the floating overlay |
| `CAMERA` | Photograph the meeting schedule |
| `READ_EXTERNAL_STORAGE` | Import PDF/image from gallery |

---

## Open Source Libraries

- Jetpack Compose 1.6+ — Apache 2.0
- Material3 for Compose 1.2+ — Apache 2.0
- Navigation Compose 2.7+ — Apache 2.0
- Lifecycle ViewModel Compose 2.7+ — Apache 2.0
- DataStore Preferences 1.1+ — Apache 2.0
- ML Kit Text Recognition 16+ — Apache 2.0
- Kotlinx Serialization JSON 1.6+ — Apache 2.0
- Kotlin Coroutines 1.8+ — Apache 2.0
- AndroidX Core KTX 1.13+ — Apache 2.0
- AndroidX Activity Compose 1.9+ — Apache 2.0
- AndroidX SavedState 1.2+ — Apache 2.0