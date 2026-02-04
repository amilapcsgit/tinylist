# NeonList - Cyberpunk Multilingual List Manager

NeonList is a modern Jetpack Compose app for fast list and item tracking with a cyber‑neon UI, rich gestures, and multilingual support. It is no longer a copy of the original tiny‑list app: it’s a fully evolved, feature‑rich native experience built for current Android devices.

## What It Is
- Modern, neon‑styled list manager for Android 12+.
- Multilingual UI (with language selection in Settings).
- Offline‑first: data stays on device.
- Gesture‑forward UX with selection sums, quick search, and export.

## Core Features
- Create, edit, delete lists and items
- Manual reorder of lists
- Gesture actions (edit/delete/duplicate) and quick selection
- Double‑tap item to mark done
- Selection mode with numeric sum extracted from item text
- Search across lists/items
- Multilingual settings
- Export JSON backup

## Safety & Privacy
- No network calls: data is stored locally with Room.
- Exports are explicit and require user action (Storage Access Framework).
- No analytics or background uploads.

## Project Layout
- `android/` - Native Android app (Compose, Room)
- `source/` - Original web app source (preserved for reference)
- `screenshots/` - App screenshots used in this README

## Screenshots
<p align="center">
  <img src="screenshots/home_lists_overview.jpg" alt="Home overview" width="30%" />
  <img src="screenshots/home_action_edit.jpg" alt="Home edit action" width="30%" />
  <img src="screenshots/home_action_delete.jpg" alt="Home delete action" width="30%" />
</p>
<p align="center">
  <img src="screenshots/home_action_duplicate.jpg" alt="Home duplicate action" width="30%" />
  <img src="screenshots/list_detail_items.jpg" alt="List detail items" width="30%" />
  <img src="screenshots/list_detail_menu.jpg" alt="List detail menu" width="30%" />
</p>
<p align="center">
  <img src="screenshots/settings_screen.jpg" alt="Settings screen" width="30%" />
</p>

## Getting Started (Contributors)
1. Install JDK 17 and Android Studio.
2. Open the `android/` folder in Android Studio and let Gradle sync.
3. Run the `app` configuration on an Android 12+ emulator/device.
4. If you change UI code, run:
   ```bash
   ./gradlew :app:compileDebugKotlin
   ```
5. Keep UI updates consistent with the neon/tiny-list visual style.

## Build & Run
### Requirements
- JDK 17
- Android Studio (recommended)
- Android SDK (compileSdk 34, minSdk 31)

### Build (CLI)
From repo root:
```bash
cd android
./gradlew assembleDebug
```

### Run (Android Studio)
1. Open the `android/` folder in Android Studio.
2. Let Gradle sync.
3. Run the `app` configuration on a device or emulator (Android 12+).

## Notes
NeonList focuses on a polished, cyber‑neon experience with responsive gestures, offline‑first storage, and a multilingual UI designed for real‑world use.
