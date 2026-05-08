# Task 1: Lower minSdk to API 29

**Priority:** P0 — Must Do  
**Estimated Time:** 5 minutes  
**Risk Level:** Low  
**Branch:** `android10`

---

## Objective

Change the `minSdk` from `31` (Android 12) to `29` (Android 10) so the app can be installed on Android 10 devices.

---

## Prerequisites

- Branch `android10` checked out and clean
- Android Studio / Gradle environment configured

---

## Steps

### Step 1: Edit `build.gradle.kts` (app-level)

**File:** `android/app/build.gradle.kts`  
**Line:** 15

**Current Code:**
```kotlin
minSdk = 31
```

**Change To:**
```kotlin
minSdk = 29
```

### Step 2: Sync Gradle

Run Gradle sync to ensure the project recognizes the new minSdk:

```bash
cd android
./gradlew --info tasks
```

### Step 3: Check for Compilation Errors

Build the project to verify there are no compile-time errors:

```bash
cd android
./gradlew assembleDebug
```

**Expected:** The build should succeed. Since all libraries in use support API 21+, and all Compose APIs are bundled in the APK (not OS-dependent), there should be no compilation issues.

### Step 4: Check for Lint Warnings

Run lint to identify any API-level warnings:

```bash
cd android
./gradlew lintDebug
```

**Expected Warnings:**
- `android:windowSplashScreenBackground` and related attributes in `themes.xml` — requires API 31. **This is expected and will be fixed in Task 2.**
- Any `NewApi` warnings about splash screen attributes.

**Note:** Do NOT try to fix lint warnings about splash screen here. That's handled in Task 2.

---

## Verification

- [ ] `build.gradle.kts` shows `minSdk = 29`
- [ ] Project compiles successfully (`assembleDebug` passes)
- [ ] No unexpected compilation errors (splash screen lint warnings are expected and OK)

---

## Files Changed

| File | Change |
|------|--------|
| `android/app/build.gradle.kts` | `minSdk = 31` → `minSdk = 29` |

---

## Rollback

If anything goes wrong, simply revert the line back to `minSdk = 31`.
