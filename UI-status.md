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

## Reorder mode and persistence - fourth pass
- Date: 2026-03-06
- Branch: `playconsolerediness`
- Commit tested: `c8b0413`
- Emulator: `Pixel_4` API 29 (Android 10)
- Execution style: Android Studio emulator + adb automation with real `draganddrop` gestures

### Exact rebuild/install flow used before each test block
1. From `android/`: `.\gradlew.bat :app:assembleDebug`
2. `D:\Projects\Android\platform-tools\adb.exe uninstall com.cyberlist.neonlist`
3. From `android/`: `.\gradlew.bat :app:installDebug`
4. `adb shell pm path com.cyberlist.neonlist`
5. `adb shell dumpsys package com.cyberlist.neonlist | Select-String 'versionCode|versionName'`
6. Launch app (`adb shell monkey -p com.cyberlist.neonlist -c android.intent.category.LAUNCHER 1`) or (`adb shell am start -n com.cyberlist.neonlist/.MainActivity`)

### Installed package proof
- `versionCode=14 minSdk=29 targetSdk=35`
- `versionName=1.1`
- `pm path` output confirmed `base.apk` install on emulator for each test block (`testA`..`testF` logs).

### Test A - HomeScreen reorder mode visibility
- Result: PASS
- Evidence: `TEST_A_HOME_HANDLES default:0 on:3 off:0`
- Interpretation: handles hidden by default, visible only while manual reorder mode is ON, hidden again on OFF.

### Test B - HomeScreen persistence
- Result: PASS
- Evidence: `TEST_B_HOME_PERSIST before:Todos > Groceries > Ideas afterDrag:Groceries > Todos > Ideas afterExit:Groceries > Todos > Ideas afterRestart:Groceries > Todos > Ideas dragHandlesAfterRestart:0`
- Interpretation: manual list order persisted after explicit mode exit and force-stop restart; handles stayed hidden after restart.

### Test C - ListDetailScreen reorder mode visibility
- Result: PASS
- Evidence: `TEST_C_DETAIL_HANDLES default:0 on:3 off:0`
- Interpretation: detail handles hidden by default, visible only in active manual reorder mode, hidden again after toggle-off.

### Test D - ListDetailScreen persistence
- Result: PASS
- Evidence: `TEST_D_DETAIL_PERSIST before:Welcome to NeonList > Swipe right to edit > Swipe left to delete afterDrag:Swipe right to edit > Welcome to NeonList > Swipe left to delete afterExit:Swipe right to edit > Welcome to NeonList > Swipe left to delete afterReopen:Swipe right to edit > Welcome to NeonList > Swipe left to delete afterRestart:Swipe right to edit > Welcome to NeonList > Swipe left to delete dragHandlesAfterExit:0 dragHandlesAfterRestart:0`
- Interpretation: detail item manual order persisted after mode toggle-off, navigation away/back, and app restart.

### Test E - Mode isolation (`MANUAL -> A-Z -> MANUAL`)
- Home result: PASS
- Home evidence: `homeSaved:Groceries > Todos > Ideas homeAz:Groceries > Ideas > Todos homeManualOn:Groceries > Todos > Ideas homeManualOff:Groceries > Todos > Ideas homeHandles(saved/az/on/off)=0/0/3/0`
- Detail result: PASS
- Detail evidence: `TEST_E_DETAIL_ISOLATION saved:Swipe right to edit > Welcome to NeonList > Swipe left to delete az:Swipe left to delete > Swipe right to edit > Welcome to NeonList manualOn:Swipe right to edit > Welcome to NeonList > Swipe left to delete manualOff:Swipe right to edit > Welcome to NeonList > Swipe left to delete handles(saved/az/on/off)=0/0/3/0`
- Interpretation: A-Z disables reorder edit mode; switching back to MANUAL restores saved manual order; handles only visible while edit mode is explicitly ON.

### Test F - Regression smoke
- Result: PARTIAL
- Automated evidence: `TEST_F_SMOKE deleteItem(before/afterDelete/afterUndo)=True/False/False undoItemTapped=False clearCompletedRemovedDone=False deleteList(todos before/afterDelete/afterUndo)=True/False/False undoListTapped=True exportPickerOpened=True`
- Confirmed in this pass: export picker opens (`exportPickerOpened=True`).
- Remaining smoke checks still need direct manual emulator interaction for reliable confirmation of all gesture+dialog paths (create/rename/duplicate/delete+undo/clear completed), because scripted swipes and dialog timing can mismatch app gesture affordances.

### Screenshot files captured
- `testA_home_default.png`
- `testA_home_manual_on.png`
- `testA_home_manual_off.png`
- `testB_after_drag.png`
- `testB_after_restart.png`
- `testC_detail_default.png`
- `testC_detail_manual_on.png`
- `testC_detail_manual_off.png`
- `testD_after_drag.png`
- `testD_after_restart.png`
- `testE_final.png`

### Artifacts
- Full logs, dumps, and screenshots: `C:\Users\Amilapcs\source\repos\cyberlist-test-artifacts\2026-03-06-fourth-pass-final`

## JSON import restore - first pass
- Date: 2026-03-06
- Branch: `playconsolelistimport`
- Commit hash tested: `f1d20c9`
- Device: `emulator-5554` (`Pixel_4`, API 29)

### Rebuild/install commands used
1. `cd android`
2. `./gradlew :app:assembleDebug`
3. `D:\Projects\Android\platform-tools\adb.exe uninstall com.cyberlist.neonlist`
4. `./gradlew :app:installDebug`
5. `D:\Projects\Android\platform-tools\adb.exe shell pm path com.cyberlist.neonlist`
6. `D:\Projects\Android\platform-tools\adb.exe shell dumpsys package com.cyberlist.neonlist | Select-String 'versionCode|versionName'`
7. `D:\Projects\Android\platform-tools\adb.exe shell monkey -p com.cyberlist.neonlist -c android.intent.category.LAUNCHER 1`

### Installed package proof
- `package:/data/app/com.cyberlist.neonlist-EUEYRe0TaAyxC0lMAMVSbA==/base.apk`
- `versionCode=15 minSdk=29 targetSdk=35`
- `versionName=1.2`

### Import files used
- Main backup file: `D:\Projects\Neonlist\neonlist-backup-2026-03-06.json`
- Pushed to emulator: `/sdcard/Download/neonlist-backup-2026-03-06.json`
- Invalid JSON check file: `/sdcard/Download/bad-import.json`

### Test results
- [PASS] Test 1 - Import from backup via SAF picker
  - Result dialog: `Import complete: 5 lists created, 0 lists merged, 26 items imported.`
- [PASS] Test 2 - Re-import same backup to verify exact-title merge behavior
  - Result dialog: `Import complete: 0 lists created, 5 lists merged, 26 items imported.`
- [PASS] Test 3 - Invalid JSON handling
  - Result dialog: `Import failed. Please choose a valid backup JSON file.`
- [PASS] Test 4 - Export still opens after import
  - DocumentsUI save screen opened with backup filename field and `SAVE` action.
- [PASS] Unit tests (`./gradlew :app:testDebugUnitTest`)
  - `resolveListTargets_mergesOnlyExactTitleMatches`
  - `normalizeSequentialOrder_reindexesMixedOrders`
  - `sortedImportedItems_supportsTimestampStyleOrderValues`

### Counters observed during import
- Lists created: `5` (first import), `0` (second import)
- Lists merged: `0` (first import), `5` (second import)
- Items imported: `26` (first import), `26` (second import)

### Remaining issue
- Manual drag reorder after import was not re-verified in this pass because adb gesture injection remains unreliable for Compose long-press drag interactions; previous branch work already covered reorder behavior separately.

## Screenshot and onboarding audit - Play Store polish pass
- Date: 2026-05-07
- Inputs: Functionality screenshot set for NeonList 1.2, README/current analysis docs, current Room/Compose implementation.

### Product read
- NeonList's strongest differentiator is not basic to-do management. It is a fast offline numeric list tool with gesture shortcuts, selective summation, undo, and manual sorting.
- The screenshots show the core power flow clearly once known:
  - tap item: select it for `SELECTED SUM`.
  - double tap item: mark complete.
  - swipe right: edit list/item.
  - swipe left: delete list/item.
  - hold + drag down: add near the current row.
  - hold + drag up: duplicate row/list.
  - menu: A-Z, completion/default/manual order, clear selection, clear completed, duplicate list.
- The main Play Store risk is discoverability. New users can miss the multi-axis gesture model and numeric extraction, so first-run data must teach through real rows.

### UI refinements applied
- Home list titles now use a weighted text area with single-line ellipsis, so long tutorial/demo titles cannot push the counter badge off-screen.
- Item rows now give the text block a weighted area, so long tutorial text cannot crowd done/drag controls.

### Demo data applied for fresh installs
- Replaced generic starter data with:
  - `Start Here - NeonList Tour`
  - `Bag Balance Demo KG`
  - `Groceries Demo`
  - `Ideas`
- The tutorial list explains offline storage, selected sums, double tap completion, edit/delete swipes, add/duplicate long-drag gestures, and sort/cleanup menu actions.
- The bag demo uses realistic suitcase rows such as `PM blue cabin 23.4 KG` and `Carpisa orange 29.2 KG`, making the bottom total/selective total feature visible immediately.
- Limit/goal notes avoid ending in digits, because NeonList intentionally extracts the final numeric value in item text for summation.

### Recommended next polish
- Add a tiny first-run visual cue near the bottom sum bar or first list row only until the first selection, because selected sum is the app's "aha" moment.
- Add a menu label state such as `Start Manual Reorder` / `Finish Manual Reorder` rather than one static `Manual Order` label.
- Consider excluding completed items from `TOTAL SUM` only if user testing expects done items to be removed from active calculations. Current behavior counts all numeric rows unless a selection is active, which is useful for luggage totals.

## NeonList 1.3 version bump and release attempt
- Date: 2026-05-07
- Version bump:
  - `APP_VERSION_CODE=17`
  - `APP_VERSION_NAME=1.3`
- Wording correction:
  - Tutorial/demo copy now says `Hold + drag up/down`, because a normal drag scrolls the list.
  - README gesture table now documents `HOLD_DRAG_UP` and `HOLD_DRAG_DOWN`.
- Documentation updates:
  - README latest APK references updated to `NeonList-1.3.apk`.
  - Play Store release guide version identity updated to `1.3 (17)`.
  - Added `releases/NeonList-1.3.md`.
- Verification:
  - `./gradlew :app:testDebugUnitTest` PASS.
  - `./gradlew :app:assembleDebug` PASS.
  - `./gradlew :app:assembleRelease` BLOCKED by signing safety check:
    - `Release build requires KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, and KEY_PASSWORD. Refusing to sign with debug.`
- Release note:
  - A true release APK was not produced in this pass because signing secrets are not configured in the current environment. This is expected and protects the Play Store release from being built with debug signing.

## NeonList 1.3 signed release build
- Date: 2026-05-07
- Signing:
  - Keystore path: `D:\Projects\Neonlist\KEY\neonlist.keystore`
  - Alias used: `key0neonlist`
  - Secrets were supplied through environment variables during the Gradle process and were not written to repo files.
- Commands:
  - `./gradlew :app:testDebugUnitTest :app:assembleRelease :app:bundleRelease` PASS.
- Metadata:
  - Release APK metadata confirms `versionCode=17`, `versionName=1.3`, `applicationId=com.cyberlist.neonlist`.
- Artifacts copied:
  - `releases/NeonList-1.3.apk`
    - SHA-256: `CA7CCD0BE2CA4CB8F2A75384E4B98DAEACC85A1C73F2386DD41D77DDCC8B0E46`
  - `releases/NeonList-1.3.aab`
    - SHA-256: `0DEBA8D29421FFBEE559168EFE25E5519960D945AABE6994518423D250B9C4B3`

## Settings screen S25 Ultra polish pass
- Date: 2026-05-07
- Device report:
  - Galaxy S25 Ultra screenshot shows the Settings screen clipping the Data section at the bottom.
  - Several Settings labels render too dark against the dark neon card surface.
- Fix applied:
  - Settings content now uses vertical scrolling with bottom padding, so Data, stats, credits, and license can never be cut off by viewport height or gesture navigation.
  - Data import/export controls are now compact full-width tappable action rows instead of tall rows plus separate text buttons.
  - Settings labels now explicitly use readable `onSurface`/muted/neon colors inside cards.
  - Section cards and data controls now use restrained 8dp rounding for a more professional settings-list feel.
- Recommended next UI refinements:
  - Make manual reorder menu copy stateful: `Start Manual Reorder` / `Done Reordering`.
  - Add a one-time first-run hint for selected sum, then auto-hide after the first item selection.
  - Add subtle haptic or snackbar feedback after import/export success.
  - Add a compact `Data` summary line such as `7 lists / 42 items / offline only`.
  - Keep Settings utilitarian and scannable; reserve cyberpunk motion/glow for list interactions where it helps the product feel fast.

## NeonList 1.4 Settings icon correction
- Date: 2026-05-07
- Device report:
  - Settings data action rows looked good after the S25 Ultra polish pass, but the import/export arrows were reversed for user expectations.
- Fix applied:
  - Export Backup now uses the outward/up upload icon.
  - Import JSON now uses the inward/down download icon.
- Version bump:
  - `APP_VERSION_CODE=18`
  - `APP_VERSION_NAME=1.4`
- Verification:
  - `./gradlew :app:testDebugUnitTest :app:assembleRelease :app:bundleRelease` PASS.
  - Release APK metadata confirms `versionCode=18`, `versionName=1.4`, `applicationId=com.cyberlist.neonlist`.
- Artifacts copied:
  - `releases/NeonList-1.4.apk`
    - SHA-256: `4682C2014BFA7999AA70C54D7053FF1A443F05BE0C8BA989ACCB3B6AC6539918`
  - `releases/NeonList-1.4.aab`
    - SHA-256: `679650AD47177B35577B7F7900EF9F352D1F62D21554B135EA83C2A0AFB9AE77`

## NeonList 1.5 package identity fix
- Date: 2026-05-07
- Play Console blocker:
  - Package/app ID `com.cyberlist.neonlist` was already taken.
- Fix applied:
  - Play Store `applicationId` changed to `com.pcslanka.neonlist`.
  - Internal Android namespace remains `com.cyberlist.neonlist` so existing Kotlin source packages, `R`, and `BuildConfig` imports continue to compile without a large refactor.
- Version bump:
  - `APP_VERSION_CODE=19`
  - `APP_VERSION_NAME=1.5`
- Verification:
  - `./gradlew :app:testDebugUnitTest :app:assembleRelease :app:bundleRelease` PASS.
  - Release APK metadata confirms `applicationId=com.pcslanka.neonlist`, `versionCode=19`, `versionName=1.5`.
- Artifacts copied:
  - `releases/NeonList-1.5.apk`
    - SHA-256: `06039D5978C7F303B6820723D8FCBF27039B14271DD5A9BB5456A9688FD17DD3`
  - `releases/NeonList-1.5.aab`
    - SHA-256: `A01C2D197F4B386C405DBEB0F67DB089DE53B85C6AA6B747D20FE37147A09C9D`
