# Task 2: Splash Screen Failsafe for API 29-30

**Priority:** P0 — Must Do  
**Estimated Time:** 15 minutes  
**Risk Level:** Medium  
**Branch:** `android10`  
**Depends On:** Task 1

---

## Objective

The current `themes.xml` uses Android 12 (API 31) splash screen attributes that will crash on API 29-30. Create a version-qualified resource to ensure splash screen attributes only apply on API 31+ while API 29-30 gets a safe fallback.

---

## Problem Analysis

**File:** `android/app/src/main/res/values/themes.xml`

The following attributes are **API 31+ only**:
- `android:windowSplashScreenBackground`
- `android:windowSplashScreenAnimatedIcon`
- `android:windowSplashScreenIconBackgroundColor`
- `android:windowSplashScreenAnimationDuration`

On API 29 devices, these attributes will cause an `InflateException` or be silently ignored (depending on the ROM), but best practice is to separate them.

---

## Steps

### Step 1: Create API 31+ specific theme file

**Create NEW file:** `android/app/src/main/res/values-v31/themes.xml`

```xml
<resources>
  <style name="Theme.NeonList" parent="Theme.Material3.DayNight.NoActionBar">
    <item name="android:windowBackground">#12121C</item>
    <item name="android:windowSplashScreenBackground">#000000</item>
    <item name="android:windowSplashScreenAnimatedIcon">@drawable/splash_icon</item>
    <item name="android:windowSplashScreenIconBackgroundColor">@android:color/transparent</item>
    <item name="android:windowSplashScreenAnimationDuration">0</item>
    <item name="android:statusBarColor">@android:color/transparent</item>
    <item name="android:navigationBarColor">@android:color/black</item>
    <item name="android:windowLightStatusBar">false</item>
    <item name="android:windowLightNavigationBar">false</item>
  </style>
</resources>
```

This is the **exact same file** as the current `themes.xml` — it preserves all existing behavior for API 31+ devices.

### Step 2: Simplify the base theme file

**Edit EXISTING file:** `android/app/src/main/res/values/themes.xml`

**Replace with:**
```xml
<resources>
  <style name="Theme.NeonList" parent="Theme.Material3.DayNight.NoActionBar">
    <item name="android:windowBackground">#12121C</item>
    <item name="android:statusBarColor">@android:color/transparent</item>
    <item name="android:navigationBarColor">@android:color/black</item>
    <item name="android:windowLightStatusBar">false</item>
    <item name="android:windowLightNavigationBar">false</item>
  </style>
</resources>
```

**What changed:** Removed the 4 `windowSplashScreen*` attributes. These are NOT safe on API 29.

**What stays:** 
- `android:windowBackground` — API 1+ ✅
- `android:statusBarColor` — API 21+ ✅
- `android:navigationBarColor` — API 21+ ✅
- `android:windowLightStatusBar` — API 23+ ✅
- `android:windowLightNavigationBar` — API 27+ ✅

### Step 3: Create the directory (if needed)

```bash
mkdir -p android/app/src/main/res/values-v31
```

### Step 4: Verify resource resolution

Android resource resolution works as follows:
- **API 29 device** → uses `values/themes.xml` (no splash attributes, safe)  
- **API 31+ device** → uses `values-v31/themes.xml` (splash attributes present, same as before)

### Step 5: Build and verify

```bash
cd android
./gradlew assembleDebug
```

**Expected:** Build succeeds. The lint warning about splash screen attributes should be resolved because they are now only in `values-v31/`.

---

## Verification

- [ ] `values-v31/themes.xml` exists with all splash screen attributes
- [ ] `values/themes.xml` has NO `windowSplashScreen*` attributes
- [ ] Both files define the same theme name: `Theme.NeonList`
- [ ] Build succeeds with no splash-related lint errors
- [ ] API 31+ devices still see the splash screen as before (no regression)

---

## Files Changed

| File | Action | Description |
|------|--------|-------------|
| `res/values/themes.xml` | MODIFIED | Removed API 31+ splash attributes |
| `res/values-v31/themes.xml` | **CREATED** | Full theme with splash attributes |

---

## Rollback

To revert:
1. Delete `res/values-v31/themes.xml`
2. Restore the original `res/values/themes.xml` with all splash attributes

---

## Technical Notes

### Why not use `tools:targetApi` instead?

While `tools:targetApi="31"` would suppress lint warnings, it does NOT prevent runtime crashes on older devices if the attribute is actually read. The `values-vXX` folder approach is the **official Android resource-qualifier mechanism** and guarantees the attributes are never loaded on incompatible devices.

### Alternative: AndroidX SplashScreen library

If a splash screen experience is desired on API 29, you can optionally add:

```kotlin
implementation("androidx.core:core-splashscreen:1.0.1")
```

And use its `installSplashScreen()` in `MainActivity.onCreate()`. This is **optional** and NOT required for basic compatibility.
