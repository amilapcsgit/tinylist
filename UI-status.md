# UI Status Report

## Baseline
- Date: 2026-03-02
- Branch: `playconsolerediness`
- Baseline commit tested: `dc393d4`
- Baseline build: `./gradlew :app:assembleDebug` from `android/` (PASS)
- Baseline runtime on API 29:
  - Home launched (PASS)
  - Create list / add item / export picker opened (PASS)
  - Delete + undo initially exposed a restore-path crash risk during automation; fixed in subsequent commits by deterministic list upsert + transaction-safe delete path.

## 1) Environment
- Repo: `tinylist` (NeonList)
- Branch: `playconsolerediness`
- Commit under final verification: `5a17c53`
- Android Studio: `AI-252.28238.7.2523.14688667` (from `C:\Program Files\Android\Android Studio\product-info.json`)
- AGP: `9.0.0` (from `android/build.gradle.kts`)
- Kotlin plugin: `2.2.10` (from `android/build.gradle.kts`)
- Gradle wrapper: `9.1.0` (from `android/gradle/wrapper/gradle-wrapper.properties`)
- Compose BOM: `2025.08.00` (from `android/app/build.gradle.kts`)

## 2) AVD Devices Used
- `Pixel_4`
- API: `29` (Android 10)
- ABI: `x86`
- Device info via adb: `model=Android_SDK_built_for_x86 product=sdk_gphone_x86`
- API 35/36 AVD: not available in this local SDK/AVD setup during this run.

## 3) Test Checklist (API 29)
- [PASS] App launches to Home
- [PASS] Create list
- [FAIL] Rename list
  - Edit dialog opens, but adb-driven text replacement did not persist a renamed title in repeated attempts.
  - Needs manual confirmation in Studio UI interaction.
- [PASS] Add items
- [PASS] Toggle done
  - Verified indirectly: toggled item disappeared after `Clear completed`.
- [PASS] Clear completed
- [PARTIAL] Reorder items (MANUAL) persistence + smoothness
  - Long-press drag cannot be triggered reliably with raw adb gestures; order remained unchanged in automated attempts.
  - No crash/jank observed during attempts.
- [PARTIAL] Reorder lists (MANUAL) persistence + smoothness
  - Same automation limitation as items; no order change observed in scripted drag attempts.
- [PASS] Delete item + undo
  - Item removed, Undo surfaced, item restored.
- [PASS] Delete list + undo (list + items)
  - `FlowList` deleted, Undo surfaced, `FlowList` restored with `0/1` count.
- [PASS] Duplicate list
  - `FlowList Copy` appears in Home.
- [PASS] Export JSON (SAF)
  - Picker opened with `neonlist-backup-2026-03-02.json` and `SAVE` button.
- [PASS] Settings: language toggle
  - UI switched to Italian labels (e.g., `LISTA NEON`, `ASPETTO`, `DATI`).
- [PASS] Settings: theme toggle
  - Theme state text changed from `Scuro` to `Chiaro`.
- [PASS] Sort modes (Manual / A-Z / Completion)
  - All mode switches executed without crash.

## 4) Evidence
- Key logcat outcome:
  - `NO_CRASH_FOUND` for `com.cyberlist.neonlist` during final verification sequence.
- Screenshots captured (not committed):
  - `C:\Users\Amilapcs\source\repos\neonlist-test-artifacts\2026-03-02\ss_home.png`
  - `C:\Users\Amilapcs\source\repos\neonlist-test-artifacts\2026-03-02\ss_settings.png`
  - `C:\Users\Amilapcs\source\repos\neonlist-test-artifacts\2026-03-02\ss_export_picker.png`
- Gradle tasks executed (representative):
  - `./gradlew :app:assembleDebug` (PASS)
  - `./gradlew :app:installDebug` (PASS on API 29 AVD)
  - Release safety check previously validated: `./gradlew :app:bundleRelease` fails fast without keystore variables (expected behavior).

## 5) Known Issues / Follow-ups
- Manual drag reorder verification still needs a true human drag test in Studio (adb cannot reliably emulate long-press drag handles for this Compose/reorderable setup).
- Rename-list automation was inconsistent via adb text injection; perform one manual rename confirmation.
- API 35/36 verification pending availability of a corresponding local AVD/system image.
