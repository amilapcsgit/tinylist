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

## Manual reorder persistence fix - second pass
- Date: 2026-03-06
- Branch: `playconsolerediness`
- Commit range: `11f39ed` (home sync) and `2db28e4` (detail sync)
- API used: `29` (`Pixel_4`, Android 10)

### Root cause found
- `HomeScreen` manual state was recreated from upstream by `remember(lists)` and then re-seeded on every Room emission while in manual mode.
- `ListDetailScreen` manual state was also re-seeded on each upstream item emission.
- Result: drag-local order could be overwritten before debounce persistence stabilized.

### Files changed
- `android/app/src/main/java/com/cyberlist/neonlist/ui/screens/HomeScreen.kt`
- `android/app/src/main/java/com/cyberlist/neonlist/ui/screens/ListDetailScreen.kt`

### Fix applied
- Keep `manualLists`/`manualItems` as stable local state.
- Add guarded sync from upstream:
  - re-seed only when local manual state is empty, or ID set changed.
  - otherwise merge upstream entity updates by ID while preserving local manual order.
- Keep debounce + distinct ID flow persistence; write sequential `order` values only when order is actually out of sync.

### Exact test commands run
- `./gradlew :app:assembleDebug` (PASS)
- `./gradlew :app:assembleDebug :app:installDebug` (PASS)
- `adb shell monkey -p com.cyberlist.neonlist -c android.intent.category.LAUNCHER 1` via SDK adb (PASS)
- `adb shell pidof com.cyberlist.neonlist` (PASS, app process running)
- `adb logcat -d | rg "FATAL EXCEPTION|AndroidRuntime: FATAL|Process: com\\.cyberlist\\.neonlist"` (no app crash traces)
- Attempted strict drag automation for manual reorder:
  - `adb shell input draganddrop 500 780 500 330 1200`
  - `adb shell input draganddrop 500 780 500 330 3000`
  - plus `uiautomator dump` before/after to compare visible order

### Results
- HomeScreen manual reorder persistence: `INCONCLUSIVE BY ADB AUTOMATION`
  - Raw adb drag did not trigger Compose long-press reorder in this setup, so scripted before/after order stayed unchanged.
  - Code path now preserves local manual order against upstream emissions and persists debounced order updates.
- ListDetailScreen manual reorder persistence: `INCONCLUSIVE BY ADB AUTOMATION`
  - Same long-press drag limitation with adb event injection.
  - Guarded local/upstream sync fix is applied analogously to items.

### Follow-up human verification required in Android Studio
- TEST 1 (Home manual): reorder lists, force-stop/reopen, confirm order persists.
- TEST 2 (Detail manual): reorder items, force-stop/reopen, confirm order persists.
- TEST 3 (Mode isolation): switch `MANUAL -> AZ -> MANUAL`, confirm saved manual order returns.

## Manual reorder fix - third pass
- Date: 2026-03-06
- Branch: `playconsolerediness`
- Commit tested: `01c336b` (latest code commit before this report update)
- Emulator/device: `Pixel_4` API 29 (Android 10)
- Execution style: adb-driven emulator automation (not manual mouse/keyboard interaction in Studio UI)

### Exact rebuild/install flow used before each test
1. `./gradlew :app:assembleDebug` (from `android/`)
2. `adb uninstall com.cyberlist.neonlist` (non-zero ignored when absent)
3. `./gradlew :app:installDebug`
4. `adb shell pm path com.cyberlist.neonlist`
5. `adb shell dumpsys package com.cyberlist.neonlist | Select-String 'versionCode|versionName'`
6. `adb shell monkey -p com.cyberlist.neonlist -c android.intent.category.LAUNCHER 1`

Automation helper committed:
- `scripts/fresh-debug-install-check.ps1`

Representative device proof captured repeatedly:
- `package:/data/app/com.cyberlist.neonlist-.../base.apk`
- `versionCode=14 minSdk=29 targetSdk=35`
- `versionName=1.1`

### Test 1 — Home manual reorder UI exists
- Result: PASS
- Evidence:
  - `DRAG_HANDLE_COUNT=3`
  - `VISIBLE_SEED_LIST_COUNT=3`
- Screenshot: `test1_home_handles.png`

### Test 2 — Home manual reorder persistence
- Result: Could not complete drag via adb (long-press drag gesture not triggered by `input draganddrop/swipe` in this emulator automation path).
- Observed order snapshots:
  - before: `Todos > Ideas > Groceries`
  - after attempted drag: unchanged
  - after force-stop/relaunch: unchanged
- Important: Home now has visible drag handles and manual-mode rendering from local manual state; persistence path code is updated, but reorder persistence still requires human drag confirmation in Studio UI.
- Screenshots: `test2_after_drag.png`, `test2_after_restart.png`

### Test 3 — Item manual reorder persistence
- Result: Could not complete drag via adb (same long-press drag limitation).
- Detail screen manual mode verification:
  - manual mode entered successfully (drag handle count visible in detail manual dumps)
- Order snapshots across attempted drag/restart remained unchanged due non-triggered drag.
- Screenshots: `test3_after_drag.png`, `test3_after_restart.png`

### Test 4 — Mode isolation (Manual -> A-Z -> Manual)
- HomeScreen: PASS
  - Drag-handle counts: `manual1:3`, `az:0`, `manual2:3`
- ListDetailScreen: PASS
  - Drag-handle counts: `manual1:3`, `az:0`, `manual2:3`
- Screenshots: `test4_home_manual_back.png`, `test4_detail_manual_back.png`

### Test 5 — Regression smoke
- Build/install/launch remained PASS across all fresh installs.
- Full gesture-heavy regression set (swipe/drag dependent actions) is blocked by the same adb gesture limitation in this environment.
- No app crash signatures observed during the above runs.

### Artifact folder (not committed)
- `C:\Users\Amilapcs\source\repos\cyberlist-test-artifacts\2026-03-06-third-pass`

### Remaining issue
- Human Studio drag verification is still required for end-to-end persistence confirmation of reordered list/item positions because adb gesture injection does not reliably trigger Compose long-press drag handles in this setup.
