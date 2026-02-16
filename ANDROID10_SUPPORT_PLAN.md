# 🛡️ NeonList – Android 10 (API 29) Backward Compatibility Plan

## Executive Summary

NeonList currently targets `minSdk = 31` (Android 12). This plan adds support for **Android 10 (API 29)** devices using a **failsafe strategy**: the existing app remains untouched for modern devices, but Android 10 devices receive graceful fallbacks where API incompatibilities exist.

**Goal:** Lower `minSdk` to `29` while keeping 100% of current functionality intact on API 31+ devices.

---

## 📊 Current Configuration

| Property       | Current Value       | Target Value        |
|----------------|---------------------|---------------------|
| `minSdk`       | 31 (Android 12)     | 29 (Android 10)     |
| `targetSdk`    | 35                  | 35 (unchanged)      |
| `compileSdk`   | 35                  | 35 (unchanged)      |
| Kotlin         | 2.2.10              | 2.2.10 (unchanged)  |
| Compose BOM    | 2025.08.00          | 2025.08.00 (unchanged) |
| AGP            | 9.0.0               | 9.0.0 (unchanged)   |
| Gradle         | 9.1.0               | 9.1.0 (unchanged)   |

---

## 🔍 Incompatibility Analysis

### 1. 🔴 HIGH — Splash Screen API (`android:windowSplashScreen*`)

**File:** `res/values/themes.xml`

**Problem:** The `android:windowSplashScreenBackground`, `android:windowSplashScreenAnimatedIcon`, `android:windowSplashScreenIconBackgroundColor`, and `android:windowSplashScreenAnimationDuration` attributes were introduced in **API 31**. Using them on API 29-30 will cause a runtime crash or resource inflation error.

**Failsafe Solution:**
- Move splash screen attributes into `res/values-v31/themes.xml` (API 31+ only)
- Create a base `res/values/themes.xml` without splash screen attributes
- Android 10/11 devices get a simple colored background during cold start (still themed to match)
- Optionally add the `androidx.core:core-splashscreen` library for backward-compatible splash screens

### 2. 🔴 HIGH — `android:windowLightNavigationBar` (API 27+ OK, but verify)

**File:** `res/values/themes.xml`

**Problem:** `android:windowLightNavigationBar` was added in API 27, so it's actually safe for API 29. ✅ No action required.

### 3. 🟡 MEDIUM — `SharedTransitionLayout` / `ExperimentalSharedTransitionApi`

**Files:** `NeonListApp.kt`, `HomeScreen.kt`, `ListDetailScreen.kt`

**Problem:** `SharedTransitionLayout` and `SharedTransitionScope.sharedElement()` are part of the Compose animation library. Since this app uses Compose BOM `2025.08.00`, these APIs are available regardless of Android version because **Compose runtime is bundled with the APK**, not the OS. ✅ No breaking incompatibility expected.

**Verification Required:** Confirm at runtime on an API 29 emulator that shared element transitions work correctly. Create a failsafe wrapper if they crash.

### 4. 🟡 MEDIUM — `Modifier.systemBarsPadding()`

**File:** `NeonListApp.kt` (line 45)

**Problem:** `systemBarsPadding()` relies on `WindowInsets` APIs which on API 29 may behave differently because edge-to-edge mode is opt-in. On API 30+, system bars insets are well-defined. On API 29, the traditional status/navigation bar APIs are used instead.

**Failsafe Solution:**
- Verify behavior on API 29 emulator
- If padding is incorrect, add a version-gated wrapper:
  ```kotlin
  if (Build.VERSION.SDK_INT >= 30) Modifier.systemBarsPadding()
  else Modifier.padding(top = statusBarHeight, bottom = navBarHeight)
  ```

### 5. 🟡 MEDIUM — `ActivityResultContracts.CreateDocument`

**File:** `SettingsScreen.kt` (line 79)

**Problem:** `ActivityResultContracts.CreateDocument("application/json")` — the constructor accepting a MIME type string was added in `activity-compose:1.6.0+`. The contract itself works on API 19+. ✅ Should be safe for API 29.

**Verification Required:** Test export on API 29 emulator.

### 6. 🟢 LOW — Room Database (v2.8.4)

**Files:** `NeonDatabase.kt`, `Daos.kt`, `Entities.kt`

**Problem:** Room 2.8.4 supports API 21+. ✅ Fully compatible with API 29.

### 7. 🟢 LOW — Compose Material 3 (`material3:1.2.1`)

**Problem:** Material 3 Compose is bundled in the APK, not dependent on the OS version. ✅ Fully compatible.

### 8. 🟢 LOW — `DataStore Preferences` (v1.1.1)

**Problem:** DataStore supports API 21+. ✅ Fully compatible.

### 9. 🟢 LOW — `Reorderable` library (`sh.calvin.reorderable:3.0.0`)

**Problem:** This is a pure Compose library. ✅ No OS version dependency.

### 10. 🟢 LOW — SharedPreferences usage

**File:** `Repository.kt`

**Problem:** SharedPreferences has been available since API 1. ✅ No issues.

### 11. 🟢 LOW — `Modifier.blur()` / Canvas drawing

**File:** `NeonScaffold.kt` (ScanlineOverlay)

**Problem:** The `blur` modifier import exists but is not directly used in the scanline overlay. `Canvas` works on all API levels via Compose. ✅ No issues.

---

## 📋 Task Breakdown

The work is organized into **6 sequential tasks** to be executed in order. Each task has its own dedicated markdown file with step-by-step instructions.

### Task 1: `TASK_01_GRADLE_MINSDK.md`
**Lower minSdk to 29**
- Change `minSdk = 31` → `minSdk = 29` in `build.gradle.kts`
- Verify the project compiles without errors

### Task 2: `TASK_02_SPLASH_SCREEN_FAILSAFE.md`
**Create splash screen failsafe for API 29-30**
- Create `values-v31/themes.xml` with current splash attributes
- Simplify base `values/themes.xml` to remove API 31+ splash attributes
- Ensure both API 29 and 31+ get proper theming

### Task 3: `TASK_03_WINDOW_INSETS_FAILSAFE.md`
**Verify and fix window insets behavior on API 29**
- Test `systemBarsPadding()` on API 29 emulator
- Add version-gated fallback if needed
- Ensure edge-to-edge display works correctly

### Task 4: `TASK_04_SHARED_TRANSITIONS_VERIFY.md`
**Verify shared element transitions on API 29**
- Test `SharedTransitionLayout` on API 29 emulator
- Add try-catch or version-gated fallback if crashes occur
- Ensure navigation animations degrade gracefully

### Task 5: `TASK_05_EXPORT_VERIFY.md`
**Verify JSON export on API 29**
- Test `ActivityResultContracts.CreateDocument` on API 29
- Ensure file picker and content resolver work correctly

### Task 6: `TASK_06_EMULATOR_TESTING.md`
**Full integration testing on API 29 emulator**
- Create/run API 29 AVD
- Run full test suite of all features
- Document any remaining issues

---

## 🏗️ Architecture: Failsafe Pattern

The recommended pattern for version-gated code:

```kotlin
// Example: Version-gated feature
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    // API 31+ behavior (current behavior, unchanged)
} else {
    // API 29-30 fallback (safe, simpler version)
}
```

**Rules:**
1. **Never modify existing behavior for API 31+ devices** — all current code paths are preserved
2. **Use `values-vXX` resource qualifiers** for XML-level version gating
3. **Use `Build.VERSION.SDK_INT` checks** for Kotlin-level version gating  
4. **Wrap experimental APIs in try-catch** where version checking is insufficient
5. **Test on both API 29 and API 35 emulators** to verify no regressions

---

## 📁 File Impact Summary

| File | Change Required | Risk |
|------|----------------|------|
| `android/app/build.gradle.kts` | ✅ Change minSdk 31→29 | Low |
| `res/values/themes.xml` | ✅ Remove splash attrs | Medium |
| `res/values-v31/themes.xml` | ✅ **NEW FILE** — splash attrs | Low |
| `ui/NeonListApp.kt` | ⚠️ Possibly add insets fallback | Medium |
| `ui/screens/HomeScreen.kt` | ⚠️ Verify shared transitions | Low |
| `ui/screens/ListDetailScreen.kt` | ⚠️ Verify shared transitions | Low |
| `ui/screens/SettingsScreen.kt` | ⚠️ Verify export contract | Low |
| All other files | ✅ No changes needed | None |

---

## 📅 Estimated Effort

| Task | Estimated Time | Priority |
|------|---------------|----------|
| Task 1: Gradle minSdk | 5 min | P0 — Must Do |
| Task 2: Splash Screen Failsafe | 15 min | P0 — Must Do |
| Task 3: Window Insets Failsafe | 20 min | P1 — Should Do |
| Task 4: Shared Transitions Verify | 15 min | P1 — Should Do |
| Task 5: Export Verify | 10 min | P2 — Nice to Have |
| Task 6: Emulator Testing | 30 min | P0 — Must Do |
| **Total** | **~1.5 hours** | |

---

## ✅ Success Criteria

1. App compiles with `minSdk = 29`
2. App installs and launches on an Android 10 (API 29) emulator
3. All core features work: create/edit/delete lists and items, swipe actions, search, settings
4. Splash screen doesn't crash on API 29 (graceful fallback)
5. JSON export works on API 29
6. No regressions on API 31+ devices
7. Theme and UI styling look correct on both API 29 and API 35
